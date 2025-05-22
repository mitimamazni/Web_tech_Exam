/**
 * Checkout Flow Handler
 *
 * This script manages the checkout flow process:
 * - Ensures the cart is not cleared prematurely when clicking checkout
 * - Properly handles cart state during order placement
 * - Provides consistent notifications during the checkout process
 * - Handles frontend validation for the checkout form
 */

document.addEventListener("DOMContentLoaded", function () {
  // Check if we're on the checkout page
  const isCheckoutPage = window.location.pathname.includes("/orders/checkout");

  if (isCheckoutPage) {
    // Show welcome notification on checkout page
    if (typeof showToast === "function") {
      showToast(
        "Checkout",
        "Please review your order and enter shipping information",
        "info"
      );
    }

    // Set up checkout form validation
    setupCheckoutFormValidation();

    // Add event listeners to saved address options
    setupAddressSelectionHandlers();
  }
});

/**
 * Set up form validation for checkout
 */
function setupCheckoutFormValidation() {
  const checkoutForm = document.getElementById("checkout-form");
  if (!checkoutForm) return;

  checkoutForm.addEventListener("submit", function (e) {
    // Prevent the form from submitting if validation fails
    if (!validateCheckoutForm()) {
      e.preventDefault();
      if (typeof showToast === "function") {
        showToast("Error", "Please fill in all required fields", "error");
      }
    } else {
      // Show processing message
      const submitBtn = checkoutForm.querySelector('button[type="submit"]');
      if (submitBtn) {
        submitBtn.innerHTML =
          '<ion-icon name="hourglass-outline"></ion-icon> Processing...';
        submitBtn.disabled = true;
      }

      // We allow the form to submit naturally here - the cart will be cleared
      // server-side after successful order placement
    }
  });
}

/**
 * Validate the checkout form
 * @returns {boolean} Whether the form is valid
 */
function validateCheckoutForm() {
  const checkoutForm = document.getElementById("checkout-form");
  if (!checkoutForm) return true;

  // Get all addressId radio buttons
  const addressRadios = document.querySelectorAll('input[name="addressId"]');
  const newAddressOption = document.getElementById("use-new-address");

  // Check if any address option is selected
  const anyAddressSelected = Array.from(addressRadios).some(
    (radio) => radio.checked
  );
  if (!anyAddressSelected) return false;

  // If using new address
  if (newAddressOption && newAddressOption.checked) {
    // Validate new address fields
    const requiredFields = [
      "streetAddress",
      "city",
      "state",
      "postalCode",
      "country",
    ];

    for (const field of requiredFields) {
      const input = document.querySelector(`input[name="${field}"]`);
      if (!input || !input.value.trim()) {
        return false;
      }
    }
  } else {
    // Validate that a saved address is selected that's not "new"
    const selectedAddress = document.querySelector(
      'input[name="addressId"]:checked'
    );
    if (
      !selectedAddress ||
      selectedAddress.value === "new" ||
      selectedAddress.value === ""
    ) {
      return false;
    }
  }

  return true;
}

/**
 * Set up handlers for address selection
 */
function setupAddressSelectionHandlers() {
  const addressRadios = document.querySelectorAll('input[name="addressId"]');
  if (!addressRadios.length) return;

  // Get the new address option
  const newAddressOption = document.getElementById("use-new-address");

  // Add change handlers to all address radios
  addressRadios.forEach((radio) => {
    radio.addEventListener("change", function () {
      const newAddressForm = document.getElementById("new-address-form");
      const savedAddresses = document.querySelector(".saved-addresses");

      if (this.id === "use-new-address" && this.value === "new") {
        if (newAddressForm) newAddressForm.classList.remove("hidden");
        if (savedAddresses) savedAddresses.classList.add("dim-section");
      } else {
        if (newAddressForm) newAddressForm.classList.add("hidden");
        if (savedAddresses) savedAddresses.classList.remove("dim-section");
      }
    });
  });

  // Trigger the change event on the selected radio
  const selectedRadio = document.querySelector(
    'input[name="addressId"]:checked'
  );
  if (selectedRadio) {
    const event = new Event("change");
    selectedRadio.dispatchEvent(event);
  }
}

/**
 * Navigate back to cart
 */
function returnToCart() {
  window.location.href = "/cart";
}
