// cart-page-dynamic.js - Handles dynamic functionality for cart page

/**
 * Enhanced utility function for cart API calls with robust error handling
 * Handles session timeouts, HTML responses, and provides better error messages
 */
async function safeCartApiCall(
  url,
  method = "POST",
  data = null,
  retryCount = 0
) {
  try {
    // Get CSRF token from meta tag
    const csrfToken = document
      .querySelector('meta[name="_csrf"]')
      ?.getAttribute("content");
    const csrfHeader =
      document
        .querySelector('meta[name="_csrf_header"]')
        ?.getAttribute("content") || "X-CSRF-TOKEN";

    // Log request for debugging
    console.log(
      `Making ${method} request to ${url}${
        retryCount > 0 ? ` (retry ${retryCount})` : ""
      }`
    );

    // Make the fetch request
    const response = await fetch(url, {
      method: method,
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken,
      },
      ...(data ? { body: JSON.stringify(data) } : {}),
      credentials: "same-origin",
    });

    // Check if response is OK
    if (!response.ok) {
      // Handle authentication errors
      if (
        response.status === 401 ||
        response.status === 403 ||
        response.status === 302
      ) {
        handleSessionTimeout();
        throw new Error("Session expired");
      }

      throw new Error(`API Error: ${response.status}`);
    }

    // Check content type to detect if we got HTML instead of JSON
    const contentType = response.headers.get("content-type");
    if (!contentType || !contentType.includes("application/json")) {
      console.error("Unexpected response format:", contentType);

      // Try to get text to see what was returned
      const text = await response.text();
      console.error(
        "Response was not JSON:",
        text.substring(0, 150) + (text.length > 150 ? "..." : "")
      );

      // Check if HTML response (session timeout)
      if (text.includes("<!DOCTYPE") || text.includes("<html")) {
        console.warn("Received HTML instead of JSON - likely session timeout");
        handleSessionTimeout();
        throw new Error("Session expired");
      }

      // Try to parse as JSON anyway (server might have wrong content type)
      try {
        const data = JSON.parse(text);
        console.log("Successfully parsed text response as JSON:", data);
        return data;
      } catch (e) {
        throw new Error("Server returned non-JSON response");
      }
    }

    // Parse JSON response
    const data = await response.json();
    return data;
  } catch (error) {
    console.error("API call failed:", error);

    // Retry on network errors or server errors
    const shouldRetry =
      retryCount < 2 &&
      (error.message.includes("NetworkError") ||
        error.message.includes("API Error: 5") ||
        error.message.includes("Failed to fetch") ||
        error.name === "TypeError" ||
        error.message.includes("network"));

    if (shouldRetry) {
      console.log(`Retrying API call (${retryCount + 1}/2)...`);

      // Wait with exponential backoff before retry
      await new Promise((resolve) =>
        setTimeout(resolve, 1000 * Math.pow(2, retryCount))
      );

      // Retry with incremented count
      return safeCartApiCall(url, method, data, retryCount + 1);
    }

    throw error;
  }
}

/**
 * Handle session timeouts by redirecting to login page
 */
function handleSessionTimeout() {
  // Show message to user
  showToast(
    "Session Expired",
    "Your session has expired. Please log in again.",
    "error"
  );

  // Wait a moment to ensure toast is visible, then redirect
  setTimeout(() => {
    const currentUrl = encodeURIComponent(window.location.pathname);
    window.location.href = `/login?redirect=${currentUrl}`;
  }, 2000);
}

document.addEventListener("DOMContentLoaded", function () {
  loadCartItems();

  // Event delegation for quantity changes and removals
  const cartContainer = document.querySelector(".cart-items-container");
  if (cartContainer) {
    cartContainer.addEventListener("click", function (e) {
      // Handle remove item button
      if (e.target.closest(".cart-item-remove")) {
        const removeBtn = e.target.closest(".cart-item-remove");
        const productId = removeBtn.dataset.productId;
        removeFromCart(productId);
        return;
      }

      // Handle quantity buttons
      if (e.target.closest(".qty-btn")) {
        const qtyBtn = e.target.closest(".qty-btn");
        const productId = qtyBtn.dataset.productId;
        const isIncrease = qtyBtn.classList.contains("increase");
        const qtyInput = qtyBtn
          .closest(".cart-item-quantity")
          .querySelector("input");
        const currentQty = parseInt(qtyInput.value);

        if (isIncrease) {
          updateCartItemQuantity(productId, currentQty + 1);
        } else if (currentQty > 1) {
          updateCartItemQuantity(productId, currentQty - 1);
        }
        return;
      }

      // Handle direct quantity input
      if (e.target.matches(".cart-item-quantity input")) {
        const qtyInput = e.target;
        qtyInput.addEventListener("change", function () {
          const productId = qtyInput.dataset.productId;
          let newQty = parseInt(qtyInput.value);
          if (isNaN(newQty) || newQty < 1) {
            newQty = 1;
            qtyInput.value = 1;
          }
          updateCartItemQuantity(productId, newQty);
        });
        return;
      }
    });
  }

  // Load cart items from API
  async function loadCartItems() {
    const cartContainer = document.querySelector(".cart-items-container");
    const cartSummary = document.querySelector(".cart-summary");
    if (!cartContainer) return;

    cartContainer.innerHTML =
      '<div class="loading-spinner">Loading your cart...</div>';

    try {
      // Use the safe API call utility with GET method
      const data = await safeCartApiCall("/cart/items", "GET");

      if (!data.success) {
        throw new Error(data.message || "Failed to load cart items");
      }

      if (!data.data || data.data.items.length === 0) {
        cartContainer.innerHTML = `
                      <div class="cart-empty">
                          <div class="cart-empty-icon">
                              <ion-icon name="cart-outline"></ion-icon>
                          </div>
                          <h3>Your cart is empty</h3>
                          <p>Looks like you haven't added any products to your cart yet.</p>
                          <a href="/products" class="btn continue-shopping">Continue Shopping</a>
                      </div>
                  `;

        if (cartSummary) {
          cartSummary.style.display = "none";
        }
        document
          .querySelector(".checkout-btn-container")
          ?.classList.add("hidden");
        return;
      }

      const items = data.data.items;
      const cartHtml = items.map((item) => renderCartItem(item)).join("");
      cartContainer.innerHTML = cartHtml;

      // Update summary
      if (cartSummary) {
        cartSummary.querySelector(
          ".subtotal-value"
        ).textContent = `$${data.data.subtotal.toFixed(2)}`;
        cartSummary.querySelector(
          ".tax-value"
        ).textContent = `$${data.data.tax.toFixed(2)}`;
        cartSummary.querySelector(".shipping-value").textContent =
          data.data.shipping > 0 ? `$${data.data.shipping.toFixed(2)}` : "Free";
        cartSummary.querySelector(
          ".total-value"
        ).textContent = `$${data.data.total.toFixed(2)}`;
        cartSummary.style.display = "block";
      }

      document
        .querySelector(".checkout-btn-container")
        ?.classList.remove("hidden");
    } catch (error) {
      console.error("Error loading cart:", error);
      cartContainer.innerHTML = `
                  <div class="cart-error">
                      <ion-icon name="alert-circle-outline"></ion-icon>
                      <p>Failed to load your cart. Please try again later.</p>
                      <button class="retry-btn" onclick="loadCartItems()">Retry</button>
                  </div>
              `;
    }
  }

  // Render a single cart item
  function renderCartItem(item) {
    const salePrice = item.salePrice
      ? `<span class="item-sale-price">$${item.salePrice.toFixed(2)}</span>`
      : "";
    const itemTotal = (item.price * item.quantity).toFixed(2);

    return `
            <div class="cart-item" data-product-id="${item.productId}">
                <div class="cart-item-image">
                    <a href="/products/${item.productId}">
                        <img src="${item.imageUrl}" alt="${item.name}">
                    </a>
                </div>
                <div class="cart-item-details">
                    <h4 class="cart-item-name">
                        <a href="/products/${item.productId}">${item.name}</a>
                    </h4>
                    <div class="cart-item-price">
                        <span class="item-price">$${item.price.toFixed(
                          2
                        )}</span>
                        ${salePrice}
                    </div>
                    <div class="cart-item-actions">
                        <div class="cart-item-quantity">
                            <button class="qty-btn decrease" data-product-id="${
                              item.productId
                            }">-</button>
                            <input type="number" value="${
                              item.quantity
                            }" min="1" data-product-id="${item.productId}">
                            <button class="qty-btn increase" data-product-id="${
                              item.productId
                            }">+</button>
                        </div>
                        <button class="cart-item-remove" data-product-id="${
                          item.productId
                        }">
                            <ion-icon name="trash-outline"></ion-icon>
                        </button>
                        <button class="move-to-wishlist" data-product-id="${
                          item.productId
                        }">
                            <ion-icon name="heart-outline"></ion-icon>
                            Save for Later
                        </button>
                    </div>
                </div>
                <div class="cart-item-total">
                    <span>$${itemTotal}</span>
                </div>
            </div>
        `;
  }

  // Remove item from cart
  async function removeFromCart(productId) {
    if (!confirm("Are you sure you want to remove this item from your cart?")) {
      return;
    }

    try {
      const data = await safeCartApiCall("/cart/remove-ajax", "POST", {
        productId,
      });

      if (!data.success) {
        throw new Error(data.message || "Failed to remove item");
      }

      showToast("Success", "Item removed from cart", "success");
      // Reload cart to update UI
      loadCartItems();
      // Update cart count in header
      if (window.updateCartCount && data.cartCount !== undefined) {
        window.updateCartCount(data.cartCount);
      }
    } catch (error) {
      console.error("Error removing item:", error);
      showToast("Error", "Failed to remove item from cart", "error");
    }
  }

  // Update cart item quantity
  async function updateCartItemQuantity(productId, quantity) {
    try {
      const data = await safeCartApiCall("/cart/update", "POST", {
        productId,
        quantity,
      });

      if (!data.success) {
        throw new Error(data.message || "Failed to update quantity");
      }

      // Update item total price
      const cartItem = document.querySelector(
        `.cart-item[data-product-id="${productId}"]`
      );
      if (cartItem) {
        const qtyInput = cartItem.querySelector(".cart-item-quantity input");
        qtyInput.value = quantity;

        const price = parseFloat(
          cartItem.querySelector(".item-price").textContent.replace("$", "")
        );
        const newTotal = (price * quantity).toFixed(2);
        cartItem.querySelector(
          ".cart-item-total span"
        ).textContent = `$${newTotal}`;
      }

      // Update cart summary
      if (data.data) {
        const summary = document.querySelector(".cart-summary");
        if (summary) {
          summary.querySelector(
            ".subtotal-value"
          ).textContent = `$${data.data.subtotal.toFixed(2)}`;
          summary.querySelector(
            ".tax-value"
          ).textContent = `$${data.data.tax.toFixed(2)}`;
          summary.querySelector(
            ".total-value"
          ).textContent = `$${data.data.total.toFixed(2)}`;
        }
      }

      // Update cart count if provided
      if (window.updateCartCount && data.cartCount !== undefined) {
        window.updateCartCount(data.cartCount);
      }
    } catch (error) {
      console.error("Error updating quantity:", error);
      showToast("Error", "Failed to update quantity", "error");
      // Reset to previous value
      loadCartItems();
    }
  }

  // Move item to wishlist with safeCartApiCall
  const moveToWishlistButtons = document.querySelectorAll(".move-to-wishlist");
  moveToWishlistButtons.forEach((button) => {
    button.addEventListener("click", async function () {
      const productId = this.dataset.productId;

      try {
        // First add to wishlist
        const wishlistData = await safeCartApiCall("/wishlist/add", "POST", {
          productId,
        });

        if (!wishlistData.success) {
          throw new Error(wishlistData.message || "Failed to add to wishlist");
        }

        // Then remove from cart
        const cartData = await safeCartApiCall("/cart/remove-ajax", "POST", {
          productId,
        });

        if (!cartData.success) {
          throw new Error(cartData.message || "Failed to remove from cart");
        }

        showToast("Success", "Item moved to wishlist", "success");

        // Reload cart
        loadCartItems();

        // Update counts
        if (window.updateCartCount && cartData.cartCount !== undefined) {
          window.updateCartCount(cartData.cartCount);
        }
        if (
          window.updateWishlistCount &&
          cartData.wishlistCount !== undefined
        ) {
          window.updateWishlistCount(cartData.wishlistCount);
        }
      } catch (error) {
        console.error("Error moving to wishlist:", error);
        showToast("Error", "Failed to move item to wishlist", "error");
      }
    });
  });

  // Show toast notification
  function showToast(title, message, type = "success") {
    if (window.showToast) {
      window.showToast(title, message, type);
    } else {
      const toastContainer =
        document.querySelector(".toast-container") ||
        document.createElement("div");
      if (!document.querySelector(".toast-container")) {
        toastContainer.className = "toast-container";
        document.body.appendChild(toastContainer);
      }

      const toast = document.createElement("div");
      toast.className = `toast ${type}`;
      toast.innerHTML = `
                <div class="toast-icon">
                    <ion-icon name="${
                      type === "success" ? "checkmark-circle" : "alert-circle"
                    }"></ion-icon>
                </div>
                <div class="toast-content">
                    <div class="toast-title">${title}</div>
                    <div class="toast-message">${message}</div>
                </div>
                <button class="toast-close" onclick="this.parentElement.remove()">
                    <ion-icon name="close"></ion-icon>
                </button>
            `;

      toastContainer.appendChild(toast);
      setTimeout(() => {
        toast.style.opacity = "0";
        setTimeout(() => toast.remove(), 300);
      }, 3000);
    }
  }
});
