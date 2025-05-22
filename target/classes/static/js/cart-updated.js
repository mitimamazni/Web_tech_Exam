/**
 * Cart Management System
 *
 * This script provides dynamic cart functionality for e-commerce applications.
 * Key features:
 *
 * 1. Dynamic cart operations (add, remove, update) with optimistic UI updates
 * 2. Cross-browser cart count synchronization
 * 3. Animated UI feedback for user actions
 * 4. Error handling and recovery
 * 5. Cart total recalculation without page refreshes
 *
 * USAGE EXAMPLES:
 *
 * 1. Remove an item from cart:
 *    removeFromCart(productId);
 *
 * 2. Update item quantity:
 *    updateCartItem(productId, newQuantity);
 *
 * 3. Update cart count (for use after any operation that changes cart item count):
 *    updateCartCount(newCount);
 *
 * 4. Show a notification to the user:
 *    showToast("Title", "Message", "success|error");
 *
 * 5. Update cart summary totals after cart changes:
 *    updateCartSummary();
 *
 * Dependencies:
 * - counter-sync.js: For cart count synchronization across tabs
 * - cart-removal-animation.css: For animations and visual feedback
 */

// Enhanced utility function for API calls with robust error handling and JSON validation
async function safeCartApiCall(url, method, options = {}, onRetry = null) {
  try {
    const { headers = {}, body = null, retryCount = 0 } = options;

    // Get CSRF token from meta tag if not provided in headers
    if (!headers["X-CSRF-TOKEN"]) {
      const csrfToken = document
        .querySelector('meta[name="_csrf"]')
        ?.getAttribute("content");
      if (csrfToken) {
        headers["X-CSRF-TOKEN"] = csrfToken;
      }
    }

    // Log the request for debugging
    console.log(
      `Making ${method} request to ${url}${
        retryCount > 0 ? ` (retry ${retryCount})` : ""
      }`
    );

    const response = await fetch(url, {
      method,
      headers,
      ...(body && { body: JSON.stringify(body) }),
      credentials: "same-origin",
    });

    // Check if response is OK
    if (!response.ok) {
      console.error(`API Error: ${response.status} ${response.statusText}`);

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

    // Check if response is JSON by examining content type
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
      options.retryCount < 2 &&
      (error.message.includes("NetworkError") ||
        error.message.includes("API Error: 5") ||
        error.message.includes("Failed to fetch") ||
        error.name === "TypeError" ||
        error.message.includes("network"));

    if (shouldRetry) {
      console.log(`Retrying API call (${options.retryCount + 1}/2)...`);

      // Wait with exponential backoff before retry
      await new Promise((resolve) =>
        setTimeout(resolve, 1000 * Math.pow(2, options.retryCount))
      );

      // Call onRetry callback if provided
      if (onRetry && typeof onRetry === "function") {
        onRetry();
      }

      // Retry with incremented count
      return safeCartApiCall(
        url,
        method,
        {
          ...options,
          retryCount: (options.retryCount || 0) + 1,
        },
        onRetry
      );
    }

    throw error;
  }
}

document.addEventListener("DOMContentLoaded", function () {
  fetchCartWithSessionFallback();

  // Add event listener for continue shopping button
  const continueShoppingBtn = document.querySelector(".continue-shopping");
  if (continueShoppingBtn) {
    continueShoppingBtn.addEventListener("click", function () {
      window.location.href = "/products";
    });
  }

  // Close toast messages
  document.querySelectorAll(".toast-close-btn").forEach((btn) => {
    btn.addEventListener("click", function () {
      this.closest(".notification-toast").classList.add("hiding");
      setTimeout(() => {
        this.closest(".notification-toast").remove();
      }, 300);
    });
  });
});

// Fetch cart data with session fallback
function fetchCartWithSessionFallback() {
  if (!username) {
    username = localStorage.getItem("username");
  }
  if (!username) {
    // Try to get from session via API
    fetch("/api/session-username")
      .then((res) => res.json())
      .then((data) => {
        if (data.username) {
          username = data.username;
          localStorage.setItem("username", username);
          fetchCartItems(username);
        } else {
          showCartLoginError();
        }
      })
      .catch(showCartLoginError);
    return;
  }
  fetchCartItems(username);
}

// Main function to fetch cart items
function fetchCartItems(username) {
  const cartItemsContainer = document.querySelector(".cart-items-container");
  const cartSummaryContainer = document.querySelector(
    ".cart-summary-container"
  );

  if (!cartItemsContainer || !cartSummaryContainer) return;

  // Show loading state
  cartItemsContainer.innerHTML =
    '<div class="loading-state"><ion-icon name="refresh-outline" class="spin"></ion-icon><p>Loading your cart...</p></div>';

  fetch(`/api/cart?username=${encodeURIComponent(username)}`)
    .then((res) => res.json())
    .then((data) => {
      if (!data.success) {
        throw new Error(data.message || "Error fetching cart");
      }

      renderCart(data.data, cartItemsContainer, cartSummaryContainer);
    })
    .catch((error) => {
      console.error("Error:", error);
      cartItemsContainer.innerHTML = `
        <div class="cart-error">
          <ion-icon name="warning-outline"></ion-icon>
          <h3>Oops! Something went wrong.</h3>
          <p>We couldn't load your cart. Please try again.</p>
          <button class="retry-btn" onclick="fetchCartItems('${username}')">Try Again</button>
        </div>
      `;
    });
}

// Render cart items and summary
function renderCart(cart, itemsContainer, summaryContainer) {
  const items = cart.items || [];

  // Check if cart is empty
  if (!items.length) {
    itemsContainer.innerHTML = `
      <div class="empty-cart">
        <ion-icon name="cart-outline" class="empty-cart-icon"></ion-icon>
        <h3 class="empty-cart-message">Your cart is empty</h3>
        <p class="empty-cart-description">Add items to your cart to see them here</p>
        <a href="/products" class="shop-now-btn">Shop Now</a>
      </div>
    `;

    summaryContainer.innerHTML = `
      <div class="cart-summary-header">
        <h3>Order Summary</h3>
      </div>
      <div class="cart-summary-content">
        <div class="summary-row">
          <span class="summary-label">No items in cart</span>
          <span class="summary-value">$0.00</span>
        </div>
        <div class="summary-total">
          <span class="summary-label">Total</span>
          <span class="summary-value">$0.00</span>
        </div>
      </div>
    `;
    return;
  }

  // Render cart items
  let itemsHTML = `
    <div class="cart-items-header">
      <div class="cart-col product-col">Product</div>
      <div class="cart-col price-col">Price</div>
      <div class="cart-col quantity-col">Quantity</div>
      <div class="cart-col total-col">Total</div>
      <div class="cart-col action-col"></div>
    </div>
  `;

  items.forEach((item) => {
    const productTotal = (parseFloat(item.price) * item.quantity).toFixed(2);

    itemsHTML += `
      <div class="cart-item" data-product-id="${item.productId}">
        <div class="cart-product">
          <img src="${
            item.imageUrl || "/images/product-placeholder.jpg"
          }" alt="${item.name}" class="cart-product-image">
          <div class="cart-product-details">
            <h4>${item.name}</h4>
            ${
              item.categoryName
                ? `<div class="product-category">${item.categoryName}</div>`
                : ""
            }
          </div>
        </div>
        
        <div class="cart-product-price">$${parseFloat(item.price).toFixed(
          2
        )}</div>
        
        <div class="cart-item-quantity">
          <div class="quantity-selector">
            <button type="button" class="quantity-btn minus" onclick="updateCartItem(${
              item.productId
            }, ${item.quantity - 1})">
              <ion-icon name="remove-outline"></ion-icon>
            </button>
            <input type="number" min="1" value="${
              item.quantity
            }" class="quantity-input" 
              data-original-value="${item.quantity}"
              onchange="updateCartItemFromInput(this, ${item.productId})"
              oninput="this.value = this.value.replace(/[^0-9]/g, '')">
            <button type="button" class="quantity-btn plus" onclick="updateCartItem(${
              item.productId
            }, ${item.quantity + 1})">
              <ion-icon name="add-outline"></ion-icon>
            </button>
          </div>
        </div>
        
        <div class="cart-item-total">$${productTotal}</div>
        
        <button type="button" class="cart-remove-btn" onclick="removeFromCart(${
          item.productId
        })">
          <ion-icon name="close-outline"></ion-icon>
        </button>
      </div>
    `;
  });

  itemsContainer.innerHTML = itemsHTML;

  // Render summary section
  const subtotal = cart.subtotal || cart.total || "0.00";
  const shipping = "0.00"; // Can be dynamic based on your business logic
  const discount = cart.discount || "0.00";
  const total = cart.total || subtotal;

  const summaryHTML = `
    <div class="cart-summary-header">
      <h3>Order Summary</h3>
    </div>
    <div class="cart-summary-content">
      <div class="summary-row">
        <span class="summary-label">Subtotal</span>
        <span class="summary-value">$${parseFloat(subtotal).toFixed(2)}</span>
      </div>
      <div class="summary-row">
        <span class="summary-label">Shipping</span>
        <span class="summary-value">$${parseFloat(shipping).toFixed(2)}</span>
      </div>
      ${
        parseFloat(discount) > 0
          ? `
      <div class="summary-row">
        <span class="summary-label">Discount</span>
        <span class="summary-value">-$${parseFloat(discount).toFixed(2)}</span>
      </div>`
          : ""
      }
      <div class="summary-total">
        <span class="summary-label">Total</span>
        <span class="summary-value">$${parseFloat(total).toFixed(2)}</span>
      </div>
      
      <a href="/orders/checkout" class="checkout-button">
        Proceed to Checkout
        <ion-icon name="arrow-forward-outline"></ion-icon>
      </a>
      
      <div class="promo-section">
        <div class="promo-title">Have a promo code?</div>
        <div class="promo-form">
          <input type="text" class="promo-input" placeholder="Enter code">
          <button class="apply-promo-btn">Apply</button>
        </div>
      </div>
    </div>
  `;

  summaryContainer.innerHTML = summaryHTML;
}

// Update cart item quantity
async function updateCartItem(productId, quantity) {
  if (!username) return;

  if (quantity <= 0) {
    removeFromCart(productId);
    return;
  }

  // Get the cart item element
  const cartItemElement = document.querySelector(
    `.cart-item[data-product-id="${productId}"]`
  );

  // Store old quantity for rollback if needed
  let oldQuantity;

  // Update UI optimistically
  if (cartItemElement) {
    const quantityInput = cartItemElement.querySelector(".quantity-input");
    oldQuantity = parseInt(quantityInput.value);
    quantityInput.value = quantity;

    // Update item total display optimistically
    const priceElement = cartItemElement.querySelector(".cart-product-price");
    const price = parseFloat(priceElement.textContent.replace("$", ""));
    const itemTotalElement = cartItemElement.querySelector(".cart-item-total");
    const newTotal = price * quantity;
    itemTotalElement.textContent = `$${newTotal.toFixed(2)}`;

    // Add a subtle animation
    cartItemElement.classList.add("quantity-updating");
    setTimeout(() => {
      cartItemElement.classList.remove("quantity-updating");
    }, 300);
  }

  // Log the request with query parameters
  logApiRequest(
    `/cart/update?productId=${productId}&quantity=${quantity}`,
    "POST",
    {
      query: { productId, quantity },
    }
  );

  try {
    // Use the safeCartApiCall utility instead of fetch
    const data = await safeCartApiCall(
      `/cart/update?productId=${productId}&quantity=${quantity}`,
      "POST",
      {}, // No additional options needed
      // Optional retry callback to show user feedback
      () => {
        showToast("Retrying", "Attempting to update cart again...", "info");
      }
    );

    if (data.success) {
      // Update cart summary without refreshing the whole cart
      updateCartSummary();

      // Update cart count in header
      const itemCount = data.data?.itemCount || 0;
      updateCartCount(itemCount);

      // Show success message for significant changes
      if (Math.abs(quantity - (oldQuantity || 1)) > 1) {
        showToast("Success", "Cart quantity updated", "success");
      }
    } else {
      // Revert to old quantity on failure
      if (cartItemElement && oldQuantity) {
        cartItemElement.querySelector(".quantity-input").value = oldQuantity;
      }
      showToast("Error", data.message || "Error updating cart", "error");
    }
  } catch (error) {
    // Use enhanced error handling
    handleCartError("update", productId, quantity, error);

    // Revert to old quantity on error
    if (cartItemElement && oldQuantity) {
      cartItemElement.querySelector(".quantity-input").value = oldQuantity;
    }
  }
}

// Update cart item from input field
function updateCartItemFromInput(input, productId) {
  const quantity = parseInt(input.value);
  const originalQuantity = input.getAttribute("data-original-value") || 1;

  // Validate the input value
  if (isNaN(quantity) || quantity < 1) {
    input.value = 1;
    updateCartItem(productId, 1);
    return;
  }

  // Debounce the update to avoid rapid firing when typing
  clearTimeout(input.dataset.updateTimeout);

  // Use a small delay to prevent too many API calls while user is typing
  input.dataset.updateTimeout = setTimeout(() => {
    // Only update if quantity actually changed
    if (parseInt(originalQuantity) !== quantity) {
      // Store new quantity for reference
      input.setAttribute("data-original-value", quantity);
      updateCartItem(productId, quantity);
    }
  }, 500); // 500ms debounce delay
}

// Remove item from cart
async function removeFromCart(productId) {
  if (!username) return;

  // Get the cart item element
  const cartItemElement = document.querySelector(
    `.cart-item[data-product-id="${productId}"]`
  );

  // Add removing class for animation
  if (cartItemElement) {
    cartItemElement.classList.add("cart-item-removing");
  }

  // Log the request with query parameters
  logApiRequest(`/cart/remove?productId=${productId}`, "POST", {
    query: { productId },
  });

  try {
    // Use the safeCartApiCall utility instead of fetch
    const data = await safeCartApiCall(
      `/cart/remove?productId=${productId}`,
      "POST",
      {}, // No additional options needed
      // Optional retry callback to show user feedback
      () => {
        showToast("Retrying", "Attempting to remove item again...", "info");
      }
    );

    if (data.success) {
      // Update cart count
      const itemCount = data.data?.itemCount || 0;
      updateCartCount(itemCount);

      // Option 2: Remove item from DOM without full refresh
      if (cartItemElement) {
        setTimeout(() => {
          cartItemElement.remove();

          // Update items count in header
          const cartItemsHeader = document.querySelector(".cart-items-header");
          if (cartItemsHeader) {
            const remainingItems =
              document.querySelectorAll(".cart-item").length;
            if (remainingItems === 0) {
              // No items left, show empty cart
              fetchCartItems(username);
            } else {
              // Update subtotal and total in summary
              updateCartSummary();
            }
          }
        }, 300); // Match animation duration
      } else {
        fetchCartItems(username);
      }

      // Show success message
      showToast("Success", "Item removed from cart", "success");
    } else {
      showToast(
        "Error",
        data.message || "Error removing item from cart",
        "error"
      );
    }
  } catch (error) {
    // Use enhanced error handling
    handleCartError("remove", productId, null, error);

    // Remove animation class if failed
    if (cartItemElement) {
      cartItemElement.classList.remove("cart-item-removing");
    }
  }
}

// Retry mechanism for cart operations
function retryCartOperation(operationType, productId, quantity = null) {
  // Show retry confirmation
  const confirmed = confirm(
    `Retry ${operationType}? The previous attempt failed.`
  );
  if (!confirmed) return;

  // Based on operation type, retry the appropriate function
  switch (operationType) {
    case "remove":
      removeFromCart(productId);
      break;
    case "update":
      if (quantity === null) {
        // Try to get current quantity from input if not provided
        const cartItem = document.querySelector(
          `.cart-item[data-product-id="${productId}"]`
        );
        if (cartItem) {
          const quantityInput = cartItem.querySelector(".quantity-input");
          quantity = parseInt(quantityInput?.value || 1);
        } else {
          quantity = 1;
        }
      }
      updateCartItem(productId, quantity);
      break;
    default:
      console.error("Unknown operation type:", operationType);
  }
}

// Update cart count in UI and across tabs
function updateCartCount(count) {
  // Use the built-in counter sync system if available
  if (typeof window.dispatchEvent === "function") {
    try {
      // Create and dispatch a custom event that counter-sync.js will listen for
      const event = new CustomEvent("cart:updated", {
        detail: { count: count },
      });
      window.dispatchEvent(event);
      return;
    } catch (e) {
      console.error("Error dispatching cart:updated event", e);
      // Fall back to manual update
    }
  }

  // Fallback implementation if the event doesn't work
  try {
    // Update all cart counters in the UI
    const cartCounters = document.querySelectorAll(".cart-counter");
    cartCounters.forEach((counter) => {
      counter.textContent = count;

      // Add animation
      counter.classList.add("counter-update");
      setTimeout(() => {
        counter.classList.remove("counter-update");
      }, 500);
    });

    // Save to localStorage for persistence
    localStorage.setItem("cartCount", count.toString());
  } catch (e) {
    console.error("Error updating cart count", e);
  }
}

// Update cart summary totals without refreshing the whole cart
function updateCartSummary() {
  // Get all cart items
  const cartItems = document.querySelectorAll(".cart-item");

  // Calculate new subtotal
  let subtotal = 0;
  cartItems.forEach((item) => {
    const price =
      parseFloat(
        item.querySelector(".cart-product-price").textContent.replace("$", "")
      ) || 0;
    const quantity = parseInt(item.querySelector(".quantity-input").value) || 0;
    subtotal += price * quantity;
  });

  // Update subtotal display
  const subtotalElement = document.querySelector(
    ".summary-value:first-of-type"
  );
  if (subtotalElement) {
    subtotalElement.textContent = `$${subtotal.toFixed(2)}`;
  }

  // Calculate tax and total
  const shipping = 0; // Free shipping
  const discount =
    parseFloat(
      document
        .querySelector(".summary-value:nth-of-type(3)")
        ?.textContent.replace("$", "")
        .replace("-", "")
    ) || 0;
  const tax = subtotal * 0.18; // 18% tax rate
  const total = subtotal + shipping + tax - discount;

  // Update tax display if present
  const taxElement = document.querySelector(
    ".summary-row:nth-of-type(3) .summary-value"
  );
  if (taxElement && taxElement.textContent.includes("Tax")) {
    taxElement.textContent = `$${tax.toFixed(2)}`;
  }

  // Update total display
  const totalElement = document.querySelector(".summary-total .summary-value");
  if (totalElement) {
    totalElement.textContent = `$${total.toFixed(2)}`;
  }

  // Update items count in header
  const itemsCount = cartItems.length;
  const headerTitle = document.querySelector(".cart-items-header h3");
  if (headerTitle) {
    headerTitle.textContent = `Your Cart (${itemsCount} item${
      itemsCount !== 1 ? "s" : ""
    })`;
  }
}

// Show toast notifications
function showToast(title, message, type = "success", autoHide = true) {
  const toastContainer =
    document.querySelector(".toast-container") ||
    (function () {
      const container = document.createElement("div");
      container.className = "toast-container";
      document.body.appendChild(container);
      return container;
    })();

  const toast = document.createElement("div");
  toast.className = `toast ${type}`;

  // Check if message contains HTML (like our retry buttons)
  const hasHTML = /<[a-z][\s\S]*>/i.test(message);

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

  // If message has HTML elements like buttons, don't auto-hide
  // This gives users time to click the retry button
  if (autoHide && !hasHTML) {
    setTimeout(() => {
      toast.style.opacity = "0";
      setTimeout(() => toast.remove(), 300);
    }, 3000);
  } else if (hasHTML) {
    // For toasts with interactive elements, auto-hide after a longer period
    setTimeout(() => {
      toast.style.opacity = "0";
      setTimeout(() => toast.remove(), 300);
    }, 10000); // 10 seconds for toasts with buttons
  }
}

// Show login error for cart
function showCartLoginError() {
  const cartItemsContainer = document.querySelector(".cart-items-container");
  const cartSummaryContainer = document.querySelector(
    ".cart-summary-container"
  );

  if (cartItemsContainer) {
    cartItemsContainer.innerHTML = `
      <div class="login-required">
        <ion-icon name="lock-closed-outline"></ion-icon>
        <h3>Please log in to view your cart</h3>
        <p>You need to be logged in to view and manage your cart items.</p>
        <a href="/login?redirect=/cart" class="login-btn">Log In</a>
      </div>
    `;
  }

  if (cartSummaryContainer) {
    cartSummaryContainer.innerHTML = "";
  }
}

// Function to handle session timeouts
function handleSessionTimeout() {
  // Clear session-related data
  localStorage.removeItem("username");
  localStorage.removeItem("cartCount");
  sessionStorage.removeItem("cartCount");

  // Log the timeout event
  console.log("Session timeout detected in cart system");

  // Reset cart counter to 0 in UI
  updateCartCount(0);

  // Don't interrupt user flow with a modal on every page
  // Instead, show a non-modal toast with login option
  const currentPage = window.location.pathname;

  // Only show modal on cart/checkout pages where action is critical
  if (currentPage.includes("/cart") || currentPage.includes("/checkout")) {
    // Show modal asking user to login again
    const confirmLogin = confirm(
      "Your session has expired. Would you like to log in again?"
    );

    if (confirmLogin) {
      // Redirect to login page with return URL
      window.location.href = `/login?redirect=${encodeURIComponent(
        currentPage
      )}`;
    }
  } else {
    // On other pages, just show a toast notification
    const loginUrl = `/login?redirect=${encodeURIComponent(currentPage)}`;
    const message = `Your session has expired. <a href="${loginUrl}" class="toast-link">Log in again</a>`;
    showToast("Session Expired", message, "error", false);
  }
}

// Debug helper for API responses
function debugApiResponse(
  endpoint,
  response,
  error = null,
  requestData = null
) {
  console.group(`API Debug: ${endpoint}`);
  console.log("Status:", response?.status);
  console.log("Status Text:", response?.statusText);

  if (requestData) {
    console.log("Request Data:", requestData);
  }

  if (error) {
    console.error("Error:", error);
  }

  // Try to get more details from the response if available
  if (response && response.bodyUsed !== true) {
    try {
      response
        .clone()
        .json()
        .then((data) => {
          console.log("Response Data:", data);
        })
        .catch(() => {
          console.log("Response body could not be parsed as JSON");
        });
    } catch (e) {
      console.log("Could not read response body");
    }
  }

  console.groupEnd();

  // Log to console in a visible way
  const style =
    "background: #f8d7da; color: #721c24; padding: 2px 5px; border-radius: 3px;";
  if (error || (response && !response.ok)) {
    console.log("%c⚠️ API Error: Check the network tab for details", style);
  }
}

// Enhanced logging for API requests to help with debugging
function logApiRequest(endpoint, method, data) {
  const timestamp = new Date().toISOString();
  console.group(`🚀 API Request: ${endpoint} (${timestamp})`);
  console.log(`Method: ${method}`);
  console.log("Request Data:", data);
  console.groupEnd();
}

// API endpoint fallback mechanism
// This helps in case the server changes endpoints
function tryEndpoints(
  operation,
  data,
  primaryEndpoint,
  fallbackEndpoints = []
) {
  return new Promise((resolve, reject) => {
    // Get CSRF token
    const csrfToken = document
      .querySelector('meta[name="_csrf"]')
      ?.getAttribute("content");

    // Try primary endpoint first
    const tryEndpoint = (index) => {
      const currentEndpoint =
        index === 0 ? primaryEndpoint : fallbackEndpoints[index - 1];
      console.log(`Trying ${operation} with endpoint: ${currentEndpoint}`);

      fetch(currentEndpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-CSRF-TOKEN": csrfToken,
        },
        body: JSON.stringify(data),
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error(`Server responded with status: ${response.status}`);
          }
          return response.json();
        })
        .then((responseData) => {
          // If successful, store the working endpoint in sessionStorage
          sessionStorage.setItem(`cart_${operation}_endpoint`, currentEndpoint);
          resolve(responseData);
        })
        .catch((error) => {
          // If we have more endpoints to try
          if (index < fallbackEndpoints.length) {
            console.log(
              `Endpoint ${currentEndpoint} failed, trying next option...`
            );
            tryEndpoint(index + 1);
          } else {
            // All endpoints failed
            console.error(`All ${operation} endpoints failed`);
            reject(error);
          }
        });
    };

    // Check if we have a previously successful endpoint stored
    const storedEndpoint = sessionStorage.getItem(`cart_${operation}_endpoint`);
    if (storedEndpoint) {
      // Start with the previously successful endpoint
      const allEndpoints = [storedEndpoint];
      if (storedEndpoint !== primaryEndpoint)
        allEndpoints.push(primaryEndpoint);
      fallbackEndpoints.forEach((ep) => {
        if (ep !== storedEndpoint) allEndpoints.push(ep);
      });

      tryEndpoint(0);
    } else {
      // Start with the primary endpoint
      tryEndpoint(0);
    }
  });
}

// Enhanced error handling for cart operations
function handleCartError(operation, productId, quantity, error) {
  console.error(`Error with cart ${operation}:`, error);

  // Log the actual endpoint that was used
  const endpoint =
    operation === "update"
      ? `/cart/${operation}?productId=${productId}&quantity=${quantity}`
      : `/cart/${operation}?productId=${productId}`;

  console.log(`Request URL was: ${endpoint}`);

  // Create toast with helpful retry button
  const toastMessage = `
    Failed to ${operation} cart. 
    <button class="retry-btn" onclick="retryCartOperation('${operation}', ${productId}${
    quantity ? ", " + quantity : ""
  })">
      Retry
    </button>
  `;

  showToast("Error", toastMessage, "error");
}
