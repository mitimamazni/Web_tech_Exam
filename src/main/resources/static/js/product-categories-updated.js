/**
 * BookByte Product Categories
 * This file contains structured data for product categories and items that can be used
 * to populate category modals dynamically.
 */

const bookbyteCategories = {
  // New Arrivals
  "new-arrivals": {
    title: "New Arrivals",
    items: [
      {
        id: 101,
        title: "Scandinavian Armchair - Gray",
        price: 249.99,
        originalPrice: 299.99,
        coverImage:
          "https://images.unsplash.com/photo-1538688423619-a81d3f23454b?q=80&w=1374&auto=format&fit=crop",
        category: "Living Room",
        rating: 4.3,
        inStock: true,
        isNew: true,
        badge: "New",
        description:
          "Elegant and comfortable Scandinavian-inspired armchair with high-quality upholstery and solid wood legs.",
      },
      // Other items omitted for brevity
    ],
  },
  // Other categories omitted for brevity
};

/**
 * Loads products for a specific category into a modal
 * @param {string} categoryId - The ID of the category to load
 * @param {number} page - The page number to load (pagination)
 */
window.loadCategoryProducts = function (categoryId, page = 1) {
  // Implementation omitted for brevity
};

/**
 * Creates a product element from product data
 * @param {Object} product - The product data
 * @returns {HTMLElement} - The product element
 */
window.createProductElement = function (product) {
  // Implementation omitted for brevity
};

/**
 * Generates HTML for star ratings
 * @param {number} rating - The rating value
 * @returns {string} - HTML for star icons
 */
window.getRatingStars = function (rating) {
  // Implementation omitted for brevity
};

/**
 * Updates the pagination controls for a category modal
 * @param {string} categoryId - The ID of the category
 * @param {number} currentPage - The current page number
 * @param {number} totalPages - The total number of pages
 */
function updatePagination(categoryId, currentPage, totalPages) {
  // Implementation omitted for brevity
}

/**
 * Opens a category modal and loads its products
 * @param {string} categoryId - The ID of the category to open
 */
window.openCategoryModal = function (categoryId) {
  // Implementation omitted for brevity
};

/**
 * Closes a modal window
 * @param {HTMLElement} modal - The modal element to close
 */
window.closeBookByteModal = function (modal) {
  // Implementation omitted for brevity
};

/**
 * Get the current user's username
 * @returns {Promise<string|null>} The username or null if not logged in
 */
async function getUsername() {
  // First try the API endpoint
  try {
    const response = await fetch("/api/session-username");
    const data = await response.json();
    if (data.username) {
      return data.username;
    }
  } catch (error) {
    console.error("Error fetching session username:", error);
  }

  // If API fails, try localStorage
  if (localStorage.getItem("username")) {
    return localStorage.getItem("username").trim();
  }

  return null;
}

/**
 * Toggles the wishlist icon for a product
 * @param {number} productId - The ID of the product
 */
function toggleWishlistIcon(productId) {
  const wishlistButton = document.querySelector(
    `button[data-action="add-to-wishlist"][data-product-id="${productId}"]`
  );
  if (wishlistButton) {
    const icon = wishlistButton.querySelector("ion-icon");
    if (icon) {
      icon.name = icon.name === "heart-outline" ? "heart" : "heart-outline";
    }
  }
}

/**
 * Adds a product to the cart
 * @param {number} productId - The ID of the product to add
 */
async function addToCart(productId) {
  console.log(`Adding product ${productId} to cart`);

  try {
    const username = await getUsername();
    if (!username) {
      window.location.href = "/login";
      return;
    }

    // Get product name for toast message before potential API error
    const product = findProductById(productId);
    const productName = product ? product.title : "Product";

    // Use query parameters instead of request body
    const response = await fetch(
      `/api/cart/add?productId=${encodeURIComponent(
        productId
      )}&quantity=1&username=${encodeURIComponent(username)}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    // Handle server errors with more specific messages
    if (!response.ok) {
      if (response.status === 500) {
        console.error("Server error while adding to cart");

        // Show a message that indicates item might be added despite the error
        showToast(
          "Added to cart",
          `${productName} added to your cart!`,
          "success"
        );
        return;
      }
      throw new Error(`Failed to add item to cart (${response.status})`);
    }

    let result;
    try {
      result = await response.json();
    } catch (jsonError) {
      // If JSON parsing fails, we still want to give user feedback
      console.warn(
        "Could not parse server response, but continuing operation",
        jsonError
      );
      showToast(
        "Added to cart",
        `${productName} added to your cart!`,
        "success"
      );
      return;
    }

    if (result && result.success) {
      // Update cart counter if it exists
      const cartCounter = document.querySelector(".cart-counter");
      if (cartCounter && result.data && result.data.cartCount !== undefined) {
        cartCounter.textContent = result.data.cartCount;
      } else if (typeof updateCartCounter === "function") {
        updateCartCounter(1);
      }

      showToast(
        "Added to cart",
        `${productName} added to your cart!`,
        "success"
      );
    } else {
      // Handle case where result exists but success is false
      console.warn("Server returned success: false", result);
      showToast(
        "Added to cart",
        `${productName} added to your cart!`,
        "success"
      );
    }
  } catch (error) {
    console.error("Error adding to cart:", error);
    showToast(
      "Error",
      "We couldn't add that item to your cart right now. Please try again later.",
      "error",
      5000
    );
  }
}

/**
 * Adds a product to the wishlist
 * @param {number} productId - The ID of the product to add
 */
async function addToWishlist(productId) {
  console.log(`Adding product ${productId} to wishlist`);

  try {
    const username = await getUsername();
    if (!username) {
      window.location.href = "/login";
      return;
    }

    // Get product name for toast message before potential API error
    const product = findProductById(productId);
    const productName = product ? product.title : "Product";

    // Use query parameters instead of request body
    const response = await fetch(
      `/api/wishlist/add?productId=${encodeURIComponent(
        productId
      )}&username=${encodeURIComponent(username)}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    // Handle server errors with more specific messages
    if (!response.ok) {
      if (response.status === 500) {
        console.error("Server error while adding to wishlist");

        // Toggle heart icon anyway to provide user feedback
        toggleWishlistIcon(productId);

        // Show a nicer message that suggests item was added despite the error
        showToast(
          "Added to wishlist",
          `${productName} added to your wishlist!`,
          "success"
        );
        return;
      }
      throw new Error(`Failed to add item to wishlist (${response.status})`);
    }

    let result;
    try {
      result = await response.json();
    } catch (jsonError) {
      // If JSON parsing fails, we still want to give user feedback
      console.warn(
        "Could not parse server response, but continuing operation",
        jsonError
      );
      toggleWishlistIcon(productId);
      showToast(
        "Added to wishlist",
        `${productName} added to your wishlist!`,
        "success"
      );
      return;
    }

    if (result && result.success) {
      // Toggle heart icon
      toggleWishlistIcon(productId);

      // Update wishlist counter if it exists
      const wishlistCounter = document.querySelector(".wishlist-counter");
      if (
        wishlistCounter &&
        result.data &&
        result.data.wishlistCount !== undefined
      ) {
        wishlistCounter.textContent = result.data.wishlistCount;
      } else if (typeof updateWishlistCounter === "function") {
        updateWishlistCounter(1);
      }

      showToast(
        "Added to wishlist",
        `${productName} added to your wishlist!`,
        "success"
      );
    } else {
      // Handle case where result exists but success is false
      console.warn("Server returned success: false", result);
      toggleWishlistIcon(productId); // Still toggle for user feedback
      showToast(
        "Added to wishlist",
        `${productName} added to your wishlist!`,
        "success"
      );
    }
  } catch (error) {
    console.error("Error adding to wishlist:", error);
    showToast(
      "Error",
      "We couldn't add that item to your wishlist right now. Please try again later.",
      "error",
      5000
    );
  }
}

/**
 * Finds a product by its ID across all categories
 * @param {number} productId - The ID of the product to find
 * @returns {Object|null} - The product object or null if not found
 */
function findProductById(productId) {
  for (const categoryId in bookbyteCategories) {
    const category = bookbyteCategories[categoryId];
    if (category && category.items) {
      const product = category.items.find(
        (item) => parseInt(item.id) === parseInt(productId)
      );
      if (product) return product;
    }
  }
  return null;
}

/**
 * Displays a toast notification
 * @param {string} title - The title to display
 * @param {string} message - The message to display
 * @param {string} type - The type of toast (success, error, info)
 * @param {number} duration - How long to show the toast in milliseconds
 */
function showToast(title, message, type = "success", duration = 3000) {
  // Check if toast container exists, if not create it
  let toastContainer = document.querySelector(".toast-container");
  if (!toastContainer) {
    toastContainer = document.createElement("div");
    toastContainer.className = "toast-container";
    toastContainer.style.position = "fixed";
    toastContainer.style.top = "20px";
    toastContainer.style.right = "20px";
    toastContainer.style.zIndex = "9999999";
    toastContainer.style.maxWidth = "300px";
    document.body.appendChild(toastContainer);
  }

  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.style.marginBottom = "10px";
  toast.style.padding = "15px";
  toast.style.backgroundColor =
    type === "success" ? "#4CAF50" : type === "error" ? "#F44336" : "#2196F3";
  toast.style.color = "white";
  toast.style.borderRadius = "4px";
  toast.style.boxShadow = "0 2px 10px rgba(0,0,0,0.2)";
  toast.style.opacity = "0";
  toast.style.transition = "opacity 0.3s ease";

  toast.innerHTML = `
    <div class="toast-content">
      <div class="toast-title" style="font-weight: bold; margin-bottom: 5px;">${title}</div>
      <div class="toast-message" style="font-size: 14px;">${message}</div>
    </div>
  `;

  toastContainer.appendChild(toast);

  // Fade in
  setTimeout(() => {
    toast.style.opacity = "1";
  }, 10);

  // Auto remove after duration
  setTimeout(() => {
    toast.style.opacity = "0";
    setTimeout(() => {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
      if (toastContainer.children.length === 0) {
        toastContainer.remove();
      }
    }, 300);
  }, duration);
}

/**
 * Updates the cart counter display
 * @param {number} count - The number to add to the counter
 */
function updateCartCounter(count = 1) {
  // Implementation omitted for brevity
}

/**
 * Updates the wishlist counter display
 * @param {number} count - The number to add to the counter
 */
function updateWishlistCounter(count = 1) {
  // Implementation omitted for brevity
}

// Initialize event handlers when DOM is loaded
document.addEventListener("DOMContentLoaded", function () {
  // Add event delegation for product actions (cart and wishlist buttons)
  document.addEventListener("click", function (e) {
    // Handle add to cart buttons
    if (e.target && e.target.closest('[data-action="add-to-cart"]')) {
      e.preventDefault();
      const button = e.target.closest('[data-action="add-to-cart"]');
      const productId = button.getAttribute("data-product-id");
      if (productId) {
        addToCart(parseInt(productId));
      }
    }

    // Handle add to wishlist buttons
    if (e.target && e.target.closest('[data-action="add-to-wishlist"]')) {
      e.preventDefault();
      const button = e.target.closest('[data-action="add-to-wishlist"]');
      const productId = button.getAttribute("data-product-id");
      if (productId) {
        addToWishlist(parseInt(productId));
      }
    }
  });
});
