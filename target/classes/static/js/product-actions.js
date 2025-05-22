/**
 * Product Actions JavaScript
 * Handles cart and wishlist operations for products
 */

document.addEventListener("DOMContentLoaded", function () {
  // Add event listeners to all add to cart buttons
  document
    .querySelectorAll('button[data-action="add-to-cart"]')
    .forEach((button) => {
      button.addEventListener("click", function (e) {
        e.preventDefault();
        const productId = this.getAttribute("data-product-id");
        addToCart(productId);
      });
    });

  // Add event listeners to all add to wishlist buttons
  document
    .querySelectorAll('button[data-action="add-to-wishlist"]')
    .forEach((button) => {
      button.addEventListener("click", function (e) {
        e.preventDefault();
        const productId = this.getAttribute("data-product-id");
        addToWishlist(productId);
      });
    });
});

/**
 * Add a product to the cart
 * @param {string} productId - The ID of the product to add
 */
async function addToCart(productId) {
  try {
    const response = await fetch("/api/cart/add", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ productId: productId, quantity: 1 }),
    });

    if (!response.ok) {
      throw new Error("Failed to add item to cart");
    }

    const result = await response.json();

    // Update cart counter if the function exists
    if (typeof updateCartCounter === "function") {
      updateCartCounter(1);
    }

    // Show success message
    showToast("Product added to cart successfully!", "success");
  } catch (error) {
    console.error("Error adding to cart:", error);
    showToast("Failed to add product to cart", "error");
  }
}

/**
 * Add a product to the wishlist
 * @param {string} productId - The ID of the product to add
 */
async function addToWishlist(productId) {
  try {
    const response = await fetch("/api/wishlist/add", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ productId: productId }),
    });

    if (!response.ok) {
      throw new Error("Failed to add item to wishlist");
    }

    const result = await response.json();

    // Update wishlist counter if the function exists
    if (typeof updateWishlistCounter === "function") {
      updateWishlistCounter(1);
    }

    // Show success message
    showToast("Product added to wishlist successfully!", "success");
  } catch (error) {
    console.error("Error adding to wishlist:", error);
    showToast("Failed to add product to wishlist", "error");
  }
}

/**
 * Show a toast notification
 * @param {string} message - The message to display
 * @param {string} type - The type of toast (success, error)
 */
function showToast(message, type = "success") {
  // Check if the toast container exists, if not create it
  let toastContainer = document.querySelector(".toast-container");
  if (!toastContainer) {
    toastContainer = document.createElement("div");
    toastContainer.className = "toast-container";
    document.body.appendChild(toastContainer);
  }

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.textContent = message;

  toastContainer.appendChild(toast);

  // Remove the toast after 3 seconds
  setTimeout(() => {
    toast.remove();
    if (toastContainer.children.length === 0) {
      toastContainer.remove();
    }
  }, 3000);
}
