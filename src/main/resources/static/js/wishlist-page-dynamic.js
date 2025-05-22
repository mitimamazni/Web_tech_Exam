// wishlist-page-dynamic.js - Handles dynamic functionality for wishlist page
document.addEventListener("DOMContentLoaded", function () {
  loadWishlistItems();

  // Event delegation for wishlist actions
  const wishlistContainer = document.querySelector(".wishlist-items-container");
  if (wishlistContainer) {
    wishlistContainer.addEventListener("click", function (e) {
      // Handle remove item button
      if (e.target.closest(".wishlist-item-remove")) {
        const removeBtn = e.target.closest(".wishlist-item-remove");
        const productId = removeBtn.dataset.productId;
        removeFromWishlist(productId);
        return;
      }

      // Handle move to cart button
      if (e.target.closest(".move-to-cart")) {
        const moveBtn = e.target.closest(".move-to-cart");
        const productId = moveBtn.dataset.productId;
        moveToCart(productId);
        return;
      }
    });
  }

  // Load wishlist items from API
  function loadWishlistItems() {
    const wishlistContainer = document.querySelector(
      ".wishlist-items-container"
    );
    if (!wishlistContainer) return;

    wishlistContainer.innerHTML =
      '<div class="loading-spinner">Loading your wishlist...</div>';

    fetch("/wishlist/items")
      .then((response) => response.json())
      .then((data) => {
        if (!data.success) {
          throw new Error(data.message || "Failed to load wishlist items");
        }

        if (!data.data || data.data.length === 0) {
          wishlistContainer.innerHTML = `
                        <div class="wishlist-empty">
                            <div class="wishlist-empty-icon">
                                <ion-icon name="heart-outline"></ion-icon>
                            </div>
                            <h3>Your wishlist is empty</h3>
                            <p>Add items you want to save for future purchases.</p>
                            <a href="/products" class="btn continue-shopping">Browse Products</a>
                        </div>
                    `;
          return;
        }

        const items = data.data;
        const wishlistHtml = items
          .map((item) => renderWishlistItem(item))
          .join("");
        wishlistContainer.innerHTML = wishlistHtml;
      })
      .catch((error) => {
        console.error("Error loading wishlist:", error);
        wishlistContainer.innerHTML = `
                    <div class="wishlist-error">
                        <ion-icon name="alert-circle-outline"></ion-icon>
                        <p>Failed to load your wishlist. Please try again later.</p>
                        <button class="retry-btn" onclick="loadWishlistItems()">Retry</button>
                    </div>
                `;
      });
  }

  // Render a single wishlist item
  function renderWishlistItem(item) {
    const salePrice = item.salePrice
      ? `<span class="item-sale-price">$${item.salePrice.toFixed(2)}</span>`
      : "";
    const stockStatus =
      item.stockQuantity > 0
        ? '<span class="in-stock">In Stock</span>'
        : '<span class="out-of-stock">Out of Stock</span>';

    return `
            <div class="wishlist-item" data-product-id="${item.productId}">
                <div class="wishlist-item-image">
                    <a href="/products/${item.productId}">
                        <img src="${item.imageUrl}" alt="${item.name}">
                    </a>
                </div>
                <div class="wishlist-item-details">
                    <h4 class="wishlist-item-name">
                        <a href="/products/${item.productId}">${item.name}</a>
                    </h4>
                    <div class="wishlist-item-price">
                        <span class="item-price">$${item.price.toFixed(
                          2
                        )}</span>
                        ${salePrice}
                    </div>
                    <div class="wishlist-item-stock">
                        ${stockStatus}
                    </div>
                    <p class="wishlist-item-added">
                        Added on ${new Date(item.addedOn).toLocaleDateString()}
                    </p>
                    <div class="wishlist-item-actions">
                        <button class="move-to-cart" data-product-id="${
                          item.productId
                        }" 
                            ${item.stockQuantity <= 0 ? "disabled" : ""}>
                            <ion-icon name="cart-outline"></ion-icon>
                            Add to Cart
                        </button>
                        <button class="wishlist-item-remove" data-product-id="${
                          item.productId
                        }">
                            <ion-icon name="trash-outline"></ion-icon>
                            Remove
                        </button>
                    </div>
                </div>
            </div>
        `;
  }

  // Remove item from wishlist
  function removeFromWishlist(productId) {
    fetch("/wishlist/remove", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        // CSRF protection removed
      },
      body: JSON.stringify({ productId }),
    })
      .then((response) => response.json())
      .then((data) => {
        if (!data.success) {
          throw new Error(data.message || "Failed to remove item");
        }

        showToast("Success", "Item removed from wishlist", "success");
        // Reload wishlist to update UI
        loadWishlistItems();
        // Update wishlist count in header
        if (window.updateWishlistCount && data.wishlistCount !== undefined) {
          window.updateWishlistCount(data.wishlistCount);
        }
      })
      .catch((error) => {
        console.error("Error removing item:", error);
        showToast("Error", "Failed to remove item from wishlist", "error");
      });
  }

  // Move item to cart
  function moveToCart(productId) {
    // First add to cart
    fetch("/cart/add", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        // CSRF protection removed
      },
      body: JSON.stringify({ productId, quantity: 1 }),
    })
      .then((response) => response.json())
      .then((data) => {
        if (!data.success) {
          throw new Error(data.message || "Failed to add to cart");
        }

        // Then remove from wishlist
        return fetch("/wishlist/remove", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            // CSRF protection removed
          },
          body: JSON.stringify({ productId }),
        });
      })
      .then((response) => response.json())
      .then((data) => {
        if (!data.success) {
          throw new Error(data.message || "Failed to remove from wishlist");
        }

        showToast("Success", "Item moved to cart", "success");
        // Reload wishlist
        loadWishlistItems();
        // Update counts
        if (window.updateCartCount && data.cartCount !== undefined) {
          window.updateCartCount(data.cartCount);
        }
        if (window.updateWishlistCount && data.wishlistCount !== undefined) {
          window.updateWishlistCount(data.wishlistCount);
        }
      })
      .catch((error) => {
        console.error("Error moving to cart:", error);
        showToast("Error", "Failed to move item to cart", "error");
      });
  }

  // Add all to cart button
  const addAllToCartBtn = document.querySelector(".add-all-to-cart");
  if (addAllToCartBtn) {
    addAllToCartBtn.addEventListener("click", function () {
      const wishlistItems = document.querySelectorAll(".wishlist-item");
      if (wishlistItems.length === 0) {
        showToast("Info", "Your wishlist is empty", "info");
        return;
      }

      // Collect all product IDs that are in stock
      const productIds = Array.from(wishlistItems)
        .filter((item) => !item.querySelector(".out-of-stock"))
        .map((item) => item.dataset.productId);

      if (productIds.length === 0) {
        showToast("Info", "No in-stock items in your wishlist", "info");
        return;
      }

      // Add all items to cart
      fetch("/cart/add-multiple", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          // CSRF protection removed
        },
        body: JSON.stringify({ productIds }),
      })
        .then((response) => response.json())
        .then((data) => {
          if (!data.success) {
            throw new Error(data.message || "Failed to add items to cart");
          }

          // Then clear the wishlist
          return fetch("/wishlist/remove-multiple", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              // CSRF protection removed
            },
            body: JSON.stringify({ productIds }),
          });
        })
        .then((response) => response.json())
        .then((data) => {
          if (!data.success) {
            throw new Error(data.message || "Failed to clear wishlist");
          }

          showToast("Success", "All items moved to cart", "success");
          // Reload wishlist
          loadWishlistItems();
          // Update counts
          if (window.updateCartCount && data.cartCount !== undefined) {
            window.updateCartCount(data.cartCount);
          }
          if (window.updateWishlistCount && data.wishlistCount !== undefined) {
            window.updateWishlistCount(data.wishlistCount);
          }
        })
        .catch((error) => {
          console.error("Error adding all to cart:", error);
          showToast("Error", "Failed to move items to cart", "error");
        });
    });
  }

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
