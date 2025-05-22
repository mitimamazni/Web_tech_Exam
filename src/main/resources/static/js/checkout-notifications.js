/**
 * Checkout Process Notifications
 *
 * This script enhances the checkout experience with timely notifications
 * and feedback throughout the process.
 */

document.addEventListener("DOMContentLoaded", function () {
  // Initialize checkout notifications
  initCheckoutNotifications();
});

/**
 * Initialize checkout notification system
 */
function initCheckoutNotifications() {
  // Only run on checkout-related pages
  const isCheckoutPage = window.location.pathname.includes("/orders/checkout");
  const isConfirmationPage = window.location.pathname.includes("/confirmation");
  const isOrderDetailsPage = window.location.pathname.match(/\/orders\/\d+$/);

  if (!isCheckoutPage && !isConfirmationPage && !isOrderDetailsPage) {
    return;
  }

  // Check for the notification system
  if (typeof showToast !== "function") {
    console.warn("Notification system not available, loading it dynamically");
    loadNotificationSystem();
    return;
  }

  // Welcome message for each page
  if (isCheckoutPage) {
    // Show checkout welcome message after a short delay
    setTimeout(() => {
      showToast(
        "Checkout",
        "Please complete your shipping information to place your order",
        "info"
      );
    }, 500);

    // Set up form validation notices
    setupFormValidationNotices();
  }

  // Success alerts based on URL parameters
  const urlParams = new URLSearchParams(window.location.search);

  if (urlParams.has("success")) {
    showToast(
      "Success",
      decodeURIComponent(urlParams.get("success")),
      "success"
    );

    // Clean up URL
    const url = new URL(window.location);
    url.searchParams.delete("success");
    window.history.replaceState({}, document.title, url);
  }

  if (urlParams.has("error")) {
    showToast("Error", decodeURIComponent(urlParams.get("error")), "error");

    // Clean up URL
    const url = new URL(window.location);
    url.searchParams.delete("error");
    window.history.replaceState({}, document.title, url);
  }
}

/**
 * Set up form validation notices for the checkout form
 */
function setupFormValidationNotices() {
  const checkoutForm = document.getElementById("checkout-form");
  if (!checkoutForm) return;

  // Add a warning when user tries to submit with invalid fields
  checkoutForm.addEventListener("submit", function (event) {
    // Check if form validation exists and fails
    if (typeof validateCheckoutForm === "function" && !validateCheckoutForm()) {
      event.preventDefault();
      showToast("Form Error", "Please fill in all required fields", "error");

      // Highlight invalid fields
      highlightInvalidFields();
    } else {
      // Form is valid, show processing message
      showToast("Processing", "Placing your order...", "info");
    }
  });

  // Set up helpful hints for address fields
  setupAddressFieldHints();
}

/**
 * Highlight invalid fields in the checkout form
 */
function highlightInvalidFields() {
  // Function to add invalid style to a field
  function markInvalid(field) {
    field.classList.add("invalid-field");
    field.addEventListener(
      "focus",
      function () {
        this.classList.remove("invalid-field");
      },
      { once: true }
    );
  }

  // Get address selection mode
  const addressMode = document.querySelector(
    'input[name="addressSelection"]:checked'
  );
  if (!addressMode) return;

  if (addressMode.value === "new") {
    // Check required fields for new address
    ["streetAddress", "city", "state", "postalCode", "country"].forEach(
      (fieldName) => {
        const field = document.querySelector(`input[name="${fieldName}"]`);
        if (field && !field.value.trim()) {
          markInvalid(field);
        }
      }
    );
  } else {
    // Check if an existing address is selected
    const addressId = document.querySelector('input[name="addressId"]:checked');
    if (!addressId) {
      // Show notice to select an address
      const addressSection = document.getElementById("saved-address-fields");
      if (addressSection) {
        addressSection.classList.add("highlight-section");
        setTimeout(() => {
          addressSection.classList.remove("highlight-section");
        }, 2000);
      }
    }
  }
}

/**
 * Set up helpful hints for address fields
 */
function setupAddressFieldHints() {
  // Add helpful hints when focusing address fields
  const addressFields = {
    streetAddress:
      "Enter your full street address including apartment or unit number",
    city: "Enter the city name",
    state: "Enter the state, province or region",
    postalCode: "Enter ZIP or postal code",
    country: "Enter country name",
  };

  // Add focus listeners
  Object.entries(addressFields).forEach(([fieldName, hint]) => {
    const field = document.querySelector(`input[name="${fieldName}"]`);
    if (field) {
      field.addEventListener("focus", function () {
        // Show hint toast when field is focused
        if (typeof showToast === "function") {
          showToast("Hint", hint, "info", true, 3000);
        }
      });
    }
  });
}

/**
 * Dynamically load the notification system if missing
 */
function loadNotificationSystem() {
  // Simple fallback notification function if script fails to load
  window.showToast =
    window.showToast ||
    function (title, message, type) {
      console.log(`[${type}] ${title}: ${message}`);
      alert(`${title}: ${message}`);
    };

  // Try to load the notification script
  const script = document.createElement("script");
  script.src = "/js/order-notifications.js";
  script.onload = function () {
    console.log("Notification system loaded successfully");
    // Reinitialize after script loads
    setTimeout(initCheckoutNotifications, 100);
  };
  script.onerror = function () {
    console.error("Failed to load notification system");
  };

  document.head.appendChild(script);
}
