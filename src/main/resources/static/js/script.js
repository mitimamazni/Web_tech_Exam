/**
 * Main JavaScript file for E-commerce application
 */

document.addEventListener("DOMContentLoaded", function () {
  // Auto-hide alerts after 5 seconds
  setTimeout(function () {
    const alerts = document.querySelectorAll(".alert");
    alerts.forEach(function (alert) {
      const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
      bsAlert.close();
    });
  }, 5000);

  // Quantity input validation
  const quantityInputs = document.querySelectorAll(".quantity-input");
  quantityInputs.forEach(function (input) {
    input.addEventListener("change", function () {
      if (parseInt(this.value) < 1) {
        this.value = 1;
      }
      if (parseInt(this.value) > parseInt(this.max)) {
        this.value = this.max;
      }
    });
  });

  // Cart quantity update form auto-submit
  const cartQuantityForms = document.querySelectorAll(".cart-quantity-form");
  cartQuantityForms.forEach(function (form) {
    const input = form.querySelector(".quantity-input");
    input.addEventListener("change", function () {
      form.submit();
    });
  });

  // Category filter functionality
  const categoryFilters = document.querySelectorAll(".category-filter");
  categoryFilters.forEach(function (filter) {
    filter.addEventListener("change", function () {
      const form = this.closest("form");
      form.submit();
    });
  });

  // Product image preview
  const productImage = document.getElementById("product-image");
  const imageThumbnails = document.querySelectorAll(".image-thumbnail");

  if (productImage && imageThumbnails.length > 0) {
    imageThumbnails.forEach(function (thumbnail) {
      thumbnail.addEventListener("click", function () {
        productImage.src = this.src;

        // Remove active class from all thumbnails
        imageThumbnails.forEach(function (thumb) {
          thumb.classList.remove("border-primary");
        });

        // Add active class to clicked thumbnail
        this.classList.add("border-primary");
      });
    });
  }

  // Admin order status update
  const statusSelect = document.getElementById("order-status");
  const statusForm = document.getElementById("status-form");

  if (statusSelect && statusForm) {
    statusSelect.addEventListener("change", function () {
      statusForm.submit();
    });
  }
});
