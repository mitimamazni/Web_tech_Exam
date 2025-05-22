/**
 * Browser-side Wishlist Storage Module
 * This module handles wishlist functionality using browser's localStorage
 */

(function () {
  // Key for localStorage
  const WISHLIST_STORAGE_KEY = "ecommerce_wishlist";

  /**
   * Initialize wishlist from localStorage or create empty one if doesn't exist
   * @returns {Object} Wishlist object
   */
  function initializeWishlist() {
    const storedWishlist = localStorage.getItem(WISHLIST_STORAGE_KEY);
    if (storedWishlist) {
      return JSON.parse(storedWishlist);
    } else {
      return {
        items: {},
        count: 0,
        lastUpdated: Date.now(),
      };
    }
  }

  /**
   * Save wishlist to localStorage
   * @param {Object} wishlist - Wishlist object to save
   */
  function saveWishlistToStorage(wishlist) {
    wishlist.lastUpdated = Date.now();
    localStorage.setItem(WISHLIST_STORAGE_KEY, JSON.stringify(wishlist));
  }

  /**
   * Add product to wishlist
   * @param {Object} product - Product to add
   */
  function addToWishlist(product) {
    if (!product || !product.id) {
      console.error("Cannot add invalid product to wishlist");
      return false;
    }

    const wishlist = initializeWishlist();

    // If product already exists in wishlist, do nothing
    if (wishlist.items[product.id]) {
      return false;
    }

    // Add product to wishlist
    wishlist.items[product.id] = {
      id: product.id,
      name: product.name,
      price: product.price,
      salePrice: product.salePrice,
      imageUrl: product.imageUrl || getMainProductImage(product),
      addedAt: Date.now(),
    };

    wishlist.count = Object.keys(wishlist.items).length;
    saveWishlistToStorage(wishlist);

    // Trigger custom event
    document.dispatchEvent(
      new CustomEvent("wishlist:updated", {
        detail: { action: "add", productId: product.id, count: wishlist.count },
      })
    );

    return true;
  }

  /**
   * Remove product from wishlist
   * @param {Number} productId - ID of product to remove
   */
  function removeFromWishlist(productId) {
    if (!productId) return false;

    const wishlist = initializeWishlist();

    if (!wishlist.items[productId]) {
      return false;
    }

    delete wishlist.items[productId];
    wishlist.count = Object.keys(wishlist.items).length;
    saveWishlistToStorage(wishlist);

    // Trigger custom event
    document.dispatchEvent(
      new CustomEvent("wishlist:updated", {
        detail: {
          action: "remove",
          productId: productId,
          count: wishlist.count,
        },
      })
    );

    return true;
  }

  /**
   * Toggle product in wishlist (add if not present, remove if present)
   * @param {Object|Number} productOrId - Product object or product ID
   */
  function toggleWishlistItem(productOrId) {
    if (!productOrId) return false;

    const productId =
      typeof productOrId === "object" ? productOrId.id : productOrId;
    const wishlist = initializeWishlist();

    if (wishlist.items[productId]) {
      return removeFromWishlist(productId);
    } else if (typeof productOrId === "object") {
      return addToWishlist(productOrId);
    } else {
      console.error("Cannot add item to wishlist: product data is required");
      return false;
    }
  }

  /**
   * Check if product is in wishlist
   * @param {Number} productId - Product ID to check
   * @returns {Boolean} True if product is in wishlist
   */
  function isInWishlist(productId) {
    if (!productId) return false;

    const wishlist = initializeWishlist();
    return !!wishlist.items[productId];
  }

  /**
   * Get wishlist count
   * @returns {Number} Number of items in wishlist
   */
  function getWishlistCount() {
    const wishlist = initializeWishlist();
    return wishlist.count;
  }

  /**
   * Get all items in wishlist
   * @returns {Array} Array of wishlist items
   */
  function getWishlistItems() {
    const wishlist = initializeWishlist();
    return Object.values(wishlist.items);
  }

  /**
   * Clear all items from wishlist
   */
  function clearWishlist() {
    const wishlist = { items: {}, count: 0, lastUpdated: Date.now() };
    saveWishlistToStorage(wishlist);

    document.dispatchEvent(
      new CustomEvent("wishlist:updated", {
        detail: { action: "clear", count: 0 },
      })
    );
  }

  /**
   * Helper function to get main product image URL
   */
  function getMainProductImage(product) {
    if (!product) return "";

    if (product.images && product.images.length) {
      // Find primary image or use first image
      const primaryImage = product.images.find((img) => img.isPrimary);
      return primaryImage ? primaryImage.imageUrl : product.images[0].imageUrl;
    }

    // Fallback to imageUrl if exists
    return product.imageUrl || "";
  }

  /**
   * Update UI to reflect current wishlist state
   */
  function updateWishlistUI() {
    const count = getWishlistCount();

    // Update all wishlist count badges
    document.querySelectorAll(".wishlist-btn .count").forEach((counter) => {
      counter.textContent = count;
    });

    // Update wishlist button states for products on page
    document.querySelectorAll("[data-product-id]").forEach((productElem) => {
      const productId = parseInt(productElem.dataset.productId);
      const wishlistBtn = productElem.querySelector(".wishlist-btn");

      if (wishlistBtn) {
        if (isInWishlist(productId)) {
          wishlistBtn.classList.add("in-wishlist");
          const icon = wishlistBtn.querySelector("ion-icon");
          if (icon) {
            icon.setAttribute("name", "heart");
          }
        } else {
          wishlistBtn.classList.remove("in-wishlist");
          const icon = wishlistBtn.querySelector("ion-icon");
          if (icon) {
            icon.setAttribute("name", "heart-outline");
          }
        }
      }
    });

    // If on wishlist page, update the items display
    const wishlistContainer = document.querySelector(".wishlist-items");
    if (wishlistContainer) {
      renderWishlistItems(wishlistContainer);
    }
  }

  /**
   * Render wishlist items to a container
   * @param {HTMLElement} container - Container to render to
   */
  function renderWishlistItems(container) {
    const items = getWishlistItems();

    if (items.length === 0) {
      container.innerHTML = `
                <div class="wishlist-empty">
                    <div class="empty-icon">
                        <ion-icon name="heart-outline"></ion-icon>
                    </div>
                    <h3>Your wishlist is empty</h3>
                    <p>Browse our products and add items to your wishlist</p>
                    <a href="/products" class="btn btn-primary">Browse Products</a>
                </div>
            `;
      return;
    }

    let html = "";
    items.forEach((item) => {
      html += `
                <div class="wishlist-item" data-product-id="${item.id}">
                    <div class="wishlist-item-img">
                        <a href="/products/${item.id}">
                            <img src="${item.imageUrl}" alt="${item.name}">
                        </a>
                    </div>
                    <div class="wishlist-item-content">
                        <a href="/products/${item.id}">
                            <h3 class="wishlist-item-title">${item.name}</h3>
                        </a>
                        <div class="wishlist-item-price">
                            ${
                              item.salePrice
                                ? `<span class="sale-price">$${item.salePrice.toFixed(
                                    2
                                  )}</span>
                                 <span class="original-price">$${item.price.toFixed(
                                   2
                                 )}</span>`
                                : `<span class="price">$${item.price.toFixed(
                                    2
                                  )}</span>`
                            }
                        </div>
                    </div>
                    <div class="wishlist-item-actions">
                        <button class="btn btn-primary add-to-cart-btn" onclick="WishlistStorage.addToCart(${
                          item.id
                        })">
                            Add to Cart
                        </button>
                        <button class="btn btn-icon remove-wishlist-btn" onclick="WishlistStorage.removeFromWishlist(${
                          item.id
                        })">
                            <ion-icon name="trash-outline"></ion-icon>
                        </button>
                    </div>
                </div>
            `;
    });

    container.innerHTML = html;
  }

  /**
   * Add product from wishlist to cart
   * @param {Number} productId - ID of product to add to cart
   */
  function addToCart(productId) {
    if (!productId) return false;

    // Use the existing global addToCart function if available
    if (typeof window.addToCart === "function") {
      window.addToCart(productId);
      // Optionally remove from wishlist after adding to cart
      // removeFromWishlist(productId);
      return true;
    }

    // Fallback if global addToCart not available
    fetch("/cart/add", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-CSRF-TOKEN": document
          .querySelector('meta[name="_csrf"]')
          ?.getAttribute("content"),
      },
      body: JSON.stringify({ productId: productId, quantity: 1 }),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to add item to cart");
        }
        return response.json();
      })
      .then((data) => {
        if (data.success) {
          showToast("Success", "Item added to cart");
          // Optionally remove from wishlist after adding to cart
          // removeFromWishlist(productId);
        }
      })
      .catch((error) => {
        console.error("Error adding item to cart:", error);
        showToast("Error", "Failed to add item to cart");
      });

    return true;
  }

  /**
   * Show toast notification
   * @param {String} title - Toast title
   * @param {String} message - Toast message
   * @param {String} type - Toast type (success, error, info)
   */
  function showToast(title, message, type = "success") {
    // Use the existing global showToast function if available
    if (typeof window.showToast === "function") {
      window.showToast(title, message, type);
      return;
    }

    // Simple fallback toast implementation
    const toastContainer =
      document.querySelector(".toast-container") ||
      (() => {
        const container = document.createElement("div");
        container.className = "toast-container";
        document.body.appendChild(container);
        return container;
      })();

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerHTML = `
            <div class="toast-header">
                <strong>${title}</strong>
                <button type="button" class="toast-close" onclick="this.parentElement.parentElement.remove()">×</button>
            </div>
            <div class="toast-body">${message}</div>
        `;

    toastContainer.appendChild(toast);

    // Auto remove toast after 3 seconds
    setTimeout(() => {
      toast.classList.add("toast-hiding");
      setTimeout(() => toast.remove(), 500);
    }, 3000);
  }

  /**
   * Synchronize wishlist with server (for logged-in users)
   * This will save the browser-based wishlist to the user's account
   */
  function syncWithServer() {
    const items = getWishlistItems();

    // Only sync if there are items
    if (items.length === 0) return;

    fetch("/wishlist/sync", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-CSRF-TOKEN": document
          .querySelector('meta[name="_csrf"]')
          ?.getAttribute("content"),
      },
      body: JSON.stringify(items),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to sync wishlist with server");
        }
        return response.json();
      })
      .then((data) => {
        if (data.success) {
          showToast("Success", "Wishlist synchronized with your account");
        }
      })
      .catch((error) => {
        console.error("Error syncing wishlist with server:", error);
        // Don't show toast on error as it might be annoying
      });
  }

  /**
   * Refresh product data from server
   * This ensures we always show the latest pricing, images, etc.
   */
  function refreshProductData() {
    const items = getWishlistItems();

    // Only refresh if there are items
    if (items.length === 0) return;

    // Get all product IDs
    const productIds = items.map((item) => item.id);

    // Fetch updated product data from server
    fetch(`/wishlist/products?${productIds.map((id) => `ids=${id}`).join("&")}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to refresh product data");
        }
        return response.json();
      })
      .then((data) => {
        if (data.success && data.products) {
          // Update local wishlist with fresh data
          const wishlist = initializeWishlist();

          data.products.forEach((serverProduct) => {
            if (wishlist.items[serverProduct.id]) {
              // Update product data while keeping the original added date
              const addedAt = wishlist.items[serverProduct.id].addedAt;
              wishlist.items[serverProduct.id] = {
                ...serverProduct,
                addedAt: addedAt,
              };
            }
          });

          saveWishlistToStorage(wishlist);
          // Update UI to reflect changes
          updateWishlistUI();
        }
      })
      .catch((error) => {
        console.error("Error refreshing product data:", error);
      });
  }

  /**
   * Initialize wishlist functionality
   */
  function init() {
    // Update UI on page load
    document.addEventListener("DOMContentLoaded", () => {
      updateWishlistUI();

      // Refresh product data from server when page loads
      refreshProductData();

      // Add "Sync with account" button if on wishlist page and user is logged in
      const wishlistContainer = document.querySelector(".wishlist-items");
      if (
        wishlistContainer &&
        document.querySelector('meta[name="user-logged-in"]')
      ) {
        const syncButton = document.createElement("button");
        syncButton.className = "btn btn-secondary sync-wishlist-btn";
        syncButton.innerHTML = "Save Wishlist to Account";
        syncButton.addEventListener("click", () => {
          syncWithServer();
        });

        // Add button to the top of wishlist page
        const wishlistHeader = document.querySelector(".wishlist-header");
        if (wishlistHeader) {
          wishlistHeader.appendChild(syncButton);
        } else {
          const buttonContainer = document.createElement("div");
          buttonContainer.className = "wishlist-sync-container";
          buttonContainer.appendChild(syncButton);
          wishlistContainer.parentNode.insertBefore(
            buttonContainer,
            wishlistContainer
          );
        }
      }

      // Add event listeners for wishlist buttons
      document.querySelectorAll(".wishlist-btn").forEach((btn) => {
        if (!btn.dataset.initialized) {
          btn.dataset.initialized = "true";
          btn.addEventListener("click", function (e) {
            // If it's part of a header or footer, it's a link to wishlist page
            if (
              btn.closest("header") ||
              btn.closest("footer") ||
              btn.closest(".mobile-bottom-navigation")
            ) {
              return; // Let the link work
            }

            // Otherwise it's a button to add/remove from wishlist
            e.preventDefault();

            const productElem = btn.closest("[data-product-id]");
            if (!productElem) return;

            const productId = parseInt(productElem.dataset.productId);
            if (!productId) return;

            // Toggle wishlist item
            if (isInWishlist(productId)) {
              removeFromWishlist(productId);
              showToast("Removed", "Item removed from wishlist");
            } else {
              // Get product data from data attributes or fetch from API
              const product = {
                id: productId,
                name:
                  productElem.dataset.productName ||
                  productElem
                    .querySelector(".product-title, .showcase-title")
                    ?.textContent?.trim(),
                price: parseFloat(
                  productElem.dataset.productPrice ||
                    productElem
                      .querySelector(".price")
                      ?.textContent?.replace("$", "")
                ),
                salePrice: productElem.dataset.productSalePrice
                  ? parseFloat(productElem.dataset.productSalePrice)
                  : null,
                imageUrl:
                  productElem.dataset.productImage ||
                  productElem.querySelector(".product-img, .showcase-img")?.src,
              };

              if (addToWishlist(product)) {
                showToast("Added", "Item added to wishlist");
              }
            }

            updateWishlistUI();
          });
        }
      });
    });

    // Update UI when wishlist changes
    document.addEventListener("wishlist:updated", () => {
      updateWishlistUI();
    });
  }

  // Expose public API
  window.WishlistStorage = {
    addToWishlist,
    removeFromWishlist,
    toggleWishlistItem,
    isInWishlist,
    getWishlistCount,
    getWishlistItems,
    clearWishlist,
    updateWishlistUI,
    addToCart,
    syncWithServer,
    refreshProductData,
    init,
  };

  // Auto-initialize
  init();
})();
