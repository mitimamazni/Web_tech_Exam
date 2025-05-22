/**
 * Cart Persistence Handler
 *
 * This script ensures cart state is properly maintained during the checkout flow.
 * It handles:
 * - Saving cart state before checkout
 * - Restoring cart if user navigates back from checkout without completing order
 * - Synchronizing cart state between client and server
 */

document.addEventListener("DOMContentLoaded", function () {
  // Initialize cart persistence system
  initCartPersistence();
});

/**
 * Initialize the cart persistence system
 */
function initCartPersistence() {
  // Listen for checkout button clicks to save cart state
  const checkoutButtons = document.querySelectorAll(".checkout-btn");
  checkoutButtons.forEach((btn) => {
    btn.addEventListener("click", saveCartStateBeforeCheckout);
  });

  // When on checkout page, mark that we're in checkout flow
  if (window.location.pathname.includes("/orders/checkout")) {
    sessionStorage.setItem("inCheckoutFlow", "true");
  }

  // When on cart page, check if user came back from an incomplete checkout
  if (window.location.pathname.includes("/cart")) {
    const inCheckoutFlow = sessionStorage.getItem("inCheckoutFlow");
    if (inCheckoutFlow === "true") {
      // User returned to cart without completing checkout
      sessionStorage.removeItem("inCheckoutFlow");

      // No need to restore cart because we fixed the issue where cart was cleared
      // But we can log a message for debugging
      console.log("User returned from checkout without completing order");
    }
  }

  // On order confirmation page, clear the checkout flow marker
  if (window.location.pathname.includes("/confirmation")) {
    sessionStorage.removeItem("inCheckoutFlow");
  }
}

/**
 * Save the current cart state before proceeding to checkout
 * This is just for tracking purposes now since we don't clear the cart prematurely
 */
function saveCartStateBeforeCheckout() {
  try {
    // Get the current timestamp
    const timestamp = new Date().toISOString();
    sessionStorage.setItem("lastCheckoutAttempt", timestamp);

    // Add analytics event if analytics system is available
    if (typeof dataLayer !== "undefined") {
      dataLayer.push({
        event: "checkout_started",
        timestamp: timestamp,
      });
    }
  } catch (e) {
    console.error("Error saving cart state before checkout:", e);
  }
}
