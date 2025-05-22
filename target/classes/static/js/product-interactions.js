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

async function addToCart(productId) {
  try {
    const username = await getUsername();
    if (!username) {
      window.location.href = "/login";
      return;
    }

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

    if (!response.ok) {
      throw new Error("Failed to add item to cart");
    }

    const result = await response.json();
    if (result.success) {
      showToast("Success", "Product added to cart!", "success");

      // Update cart counter if it exists
      const cartCounter = document.querySelector(".cart-counter");
      if (cartCounter && result.data && result.data.cartCount !== undefined) {
        cartCounter.textContent = result.data.cartCount;
      }
    } else {
      throw new Error(result.message || "Failed to add item to cart");
    }
  } catch (error) {
    console.error("Error adding to cart:", error);
    showToast("Error", "Failed to add product to cart", "error");
  }
}

async function addToWishlist(productId) {
  try {
    const username = await getUsername();
    if (!username) {
      window.location.href = "/login";
      return;
    }

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

    if (!response.ok) {
      throw new Error("Failed to add item to wishlist");
    }

    const result = await response.json();
    if (result.success) {
      // Toggle heart icon
      const wishlistButton = document.querySelector(
        `button[data-action="add-to-wishlist"][data-product-id="${productId}"]`
      );
      if (wishlistButton) {
        const icon = wishlistButton.querySelector("ion-icon");
        if (icon) {
          icon.name = icon.name === "heart-outline" ? "heart" : "heart-outline";
        }
      }

      // Update wishlist counter if it exists
      const wishlistCounter = document.querySelector(".wishlist-counter");
      if (
        wishlistCounter &&
        result.data &&
        result.data.wishlistCount !== undefined
      ) {
        wishlistCounter.textContent = result.data.wishlistCount;
      }

      showToast("Success", "Product added to wishlist!", "success");
    } else {
      throw new Error(result.message || "Failed to add item to wishlist");
    }
  } catch (error) {
    console.error("Error adding to wishlist:", error);
    showToast("Error", "Failed to add product to wishlist", "error");
  }
}

function showToast(title, message, type = "success") {
  // Check if toast container exists, if not create it
  let toastContainer = document.querySelector(".toast-container");
  if (!toastContainer) {
    toastContainer = document.createElement("div");
    toastContainer.className = "toast-container";
    document.body.appendChild(toastContainer);
  }

  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.innerHTML = `
        <div class="toast-content">
            <div class="toast-title">${title}</div>
            <div class="toast-message">${message}</div>
        </div>
    `;

  toastContainer.appendChild(toast);

  // Auto remove after 3 seconds
  setTimeout(() => {
    toast.classList.add("toast-fade-out");
    setTimeout(() => {
      toast.remove();
      if (toastContainer.children.length === 0) {
        toastContainer.remove();
      }
    }, 300);
  }, 3000);
}
