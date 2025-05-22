/**
 * Order Completion Handler
 *
 * This script handles post-order tasks like:
 * - Clearing the cart after successful order placement
 * - Updating cart counters
 * - Tracking purchase events
 * - Handling order history
 */

document.addEventListener("DOMContentLoaded", function () {
  // Check if we're on the order confirmation page
  if (
    window.location.pathname.includes("/orders/") &&
    (window.location.pathname.includes("/confirmation") ||
      window.location.search.includes("confirmation=true"))
  ) {
    // Clear cart data in localStorage
    clearCartData();

    // Reset cart count to zero
    if (typeof updateCartCount === "function") {
      updateCartCount(0);
    }

    // Show confirmation toast
    if (typeof showToast === "function") {
      showToast(
        "Success",
        "Your order has been placed successfully!",
        "success"
      );
    }
  }
});

/**
 * Clears the cart data from localStorage and cookies
 */
function clearCartData() {
  try {
    // Clear cart count
    localStorage.removeItem("cartCount");
    sessionStorage.removeItem("cartCount");

    // Clear any cart items data if stored
    localStorage.removeItem("cartItems");
    sessionStorage.removeItem("cartItems");

    console.log("Cart data cleared after successful order placement");

    // Update UI display if on order confirmation page
    const cartCounters = document.querySelectorAll(".cart-counter");
    cartCounters.forEach((counter) => {
      counter.textContent = "0";
    });
  } catch (e) {
    console.error("Error clearing cart data:", e);
  }
}

/**
 * Track purchase for analytics
 * @param {Object} orderData - The order data
 */
function trackPurchase(orderData) {
  try {
    // This function can be expanded to integrate with analytics systems
    console.log("Purchase tracking:", orderData);

    // Example tracking code (placeholder)
    if (typeof dataLayer !== "undefined") {
      dataLayer.push({
        event: "purchase",
        ecommerce: {
          purchase: {
            actionField: {
              id: orderData.id,
              revenue: orderData.totalAmount,
              coupon: orderData.discountCode || "",
            },
            products: orderData.items.map((item) => ({
              id: item.productId,
              name: item.productName,
              price: item.price,
              quantity: item.quantity,
            })),
          },
        },
      });
    }
  } catch (e) {
    console.error("Error tracking purchase:", e);
  }
}
