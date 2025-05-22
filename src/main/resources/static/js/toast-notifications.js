/**
 * Toast Notification System for BookByte
 *
 * This script provides a simple toast notification system
 * for displaying success, error, and info messages to users.
 */

(function () {
  // Create toast container if it doesn't exist
  document.addEventListener("DOMContentLoaded", function () {
    if (!document.querySelector(".toast-container")) {
      const toastContainer = document.createElement("div");
      toastContainer.className = "toast-container";
      document.body.appendChild(toastContainer);
    }
  });

  /**
   * Shows a toast notification
   * @param {string} message - The message to display
   * @param {string} type - The type of toast (success, error, info)
   * @param {number} duration - How long to display the toast in ms (default: 3000)
   */
  window.showToast = function (message, type = "info", duration = 3000) {
    // Find or create toast container
    let toastContainer = document.querySelector(".toast-container");
    if (!toastContainer) {
      toastContainer = document.createElement("div");
      toastContainer.className = "toast-container";
      document.body.appendChild(toastContainer);
    }

    // Create toast element
    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;

    // Add appropriate icon
    let icon = "";
    switch (type) {
      case "success":
        icon = '<ion-icon name="checkmark-circle"></ion-icon>';
        break;
      case "error":
        icon = '<ion-icon name="alert-circle"></ion-icon>';
        break;
      default:
        icon = '<ion-icon name="information-circle"></ion-icon>';
    }

    // Set toast content
    toast.innerHTML = `
            <div class="toast-icon">${icon}</div>
            <div class="toast-message">${message}</div>
            <button class="toast-close" aria-label="Close notification">
                <ion-icon name="close-outline"></ion-icon>
            </button>
        `;

    // Add close functionality
    const closeBtn = toast.querySelector(".toast-close");
    if (closeBtn) {
      closeBtn.addEventListener("click", () => {
        toast.classList.add("toast-hide");
        setTimeout(() => {
          if (toast.parentNode) {
            toastContainer.removeChild(toast);
          }
        }, 300); // Match the CSS transition time
      });
    }

    // Add to container
    toastContainer.appendChild(toast);

    // Remove after delay
    setTimeout(() => {
      toast.classList.add("toast-hide");
      setTimeout(() => {
        if (toast.parentNode) {
          toastContainer.removeChild(toast);
        }
      }, 300); // Match the CSS transition time
    }, duration);

    return toast;
  };
})();
