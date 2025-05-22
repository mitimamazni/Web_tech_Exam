/**
 * Order Notifications System
 *
 * This script provides a unified toast notification system for the order process pages,
 * including checkout, confirmation, and details pages.
 *
 * Features:
 * - Consistent toast notifications across pages
 * - Support for success, error, and info message types
 * - Auto-dismissal with configurable timing
 * - Animation effects for better UX
 * - Query parameter message detection
 * - Flash message compatibility
 */

console.log("Initializing order notifications system");

// Initialize the toast notification container if it doesn't exist
function ensureToastContainer() {
  let toastContainer = document.querySelector(".toast-container");
  if (!toastContainer) {
    console.log("Creating toast container");
    toastContainer = document.createElement("div");
    toastContainer.className = "toast-container";
    document.body.appendChild(toastContainer);
  }
  return toastContainer;
}

document.addEventListener("DOMContentLoaded", function () {
  ensureToastContainer();

  // Check URL for message parameters
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.has("success")) {
    showToast("Success", urlParams.get("success"), "success");
  }
  if (urlParams.has("error")) {
    showToast("Error", urlParams.get("error"), "error");
  }
  if (urlParams.has("info")) {
    showToast("Information", urlParams.get("info"), "info");
  }

  // Check for flash messages in the DOM
  const successFlash = document.querySelector(".alert-success");
  if (successFlash) {
    showToast("Success", successFlash.textContent.trim(), "success");
    successFlash.style.display = "none"; // Hide the original flash message
  }

  const errorFlash = document.querySelector(".alert-danger");
  if (errorFlash) {
    showToast("Error", errorFlash.textContent.trim(), "error");
    errorFlash.style.display = "none"; // Hide the original flash message
  }

  // Add click handlers to close buttons for any static toasts
  document.querySelectorAll(".toast-close-btn").forEach((btn) => {
    btn.addEventListener("click", function () {
      const toast = this.closest(".notification-toast");
      if (toast) {
        toast.style.opacity = "0";
        setTimeout(() => toast.remove(), 300);
      }
    });
  });
});

/**
 * Shows a toast notification
 * @param {string} title - The title of the notification
 * @param {string} message - The message content
 * @param {string} type - The type of notification: 'success', 'error', or 'info'
 * @param {boolean} autoHide - Whether to automatically hide the toast after a delay
 * @param {number} delay - The delay in milliseconds before hiding the toast
 */
function showToast(
  title,
  message,
  type = "success",
  autoHide = true,
  delay = 5000
) {
  console.log(`Showing ${type} toast: ${title} - ${message}`);

  // Find or create toast container
  let toastContainer = ensureToastContainer();

  // Create toast element
  const toast = document.createElement("div");
  toast.className = `notification-toast ${type}-toast`;

  // Define icon based on notification type
  let iconName;
  switch (type) {
    case "success":
      iconName = "checkmark-circle-outline";
      break;
    case "error":
      iconName = "alert-circle-outline";
      break;
    case "info":
      iconName = "information-circle-outline";
      break;
    default:
      iconName = "chatbubble-outline";
  }

  // Set toast content
  toast.innerHTML = `
    <div class="toast-banner">
      <ion-icon name="${iconName}"></ion-icon>
    </div>
    <div class="toast-detail">
      <p class="toast-message"><strong>${title}</strong>: ${message}</p>
      <button class="toast-close-btn">
        <ion-icon name="close-outline"></ion-icon>
      </button>
    </div>
  `;

  // Add toast to container
  toastContainer.appendChild(toast);

  // Add click listener to close button
  const closeBtn = toast.querySelector(".toast-close-btn");
  if (closeBtn) {
    closeBtn.addEventListener("click", function () {
      toast.style.opacity = "0";
      setTimeout(() => toast.remove(), 300);
    });
  }

  // Auto-hide after delay (except for "info" type if specified)
  if (autoHide && (type !== "info" || delay < 10000)) {
    setTimeout(() => {
      toast.style.opacity = "0";
      setTimeout(() => {
        if (toast.parentNode) {
          toast.remove();
        }
      }, 300);
    }, delay);
  }

  return toast;
}

/**
 * Shows a confirmation dialog with toast notification styling
 * @param {string} title - The title of the dialog
 * @param {string} message - The message to display
 * @param {Function} onConfirm - Function to call when user confirms
 * @param {Function} onCancel - Function to call when user cancels
 */
function showConfirmToast(title, message, onConfirm, onCancel) {
  // Find or create toast container
  let toastContainer = document.querySelector(".toast-container");
  if (!toastContainer) {
    toastContainer = document.createElement("div");
    toastContainer.className = "toast-container";
    document.body.appendChild(toastContainer);
  }

  // Create toast element with buttons
  const toast = document.createElement("div");
  toast.className = "notification-toast confirm-toast";

  toast.innerHTML = `
    <div class="toast-banner">
      <ion-icon name="help-circle-outline"></ion-icon>
    </div>
    <div class="toast-detail">
      <p class="toast-message"><strong>${title}</strong>: ${message}</p>
      <div class="toast-actions">
        <button class="toast-confirm-btn">Confirm</button>
        <button class="toast-cancel-btn">Cancel</button>
      </div>
    </div>
  `;

  // Add toast to container
  toastContainer.appendChild(toast);

  // Add click listeners to buttons
  const confirmBtn = toast.querySelector(".toast-confirm-btn");
  const cancelBtn = toast.querySelector(".toast-cancel-btn");

  if (confirmBtn) {
    confirmBtn.addEventListener("click", function () {
      if (typeof onConfirm === "function") {
        onConfirm();
      }
      toast.style.opacity = "0";
      setTimeout(() => toast.remove(), 300);
    });
  }

  if (cancelBtn) {
    cancelBtn.addEventListener("click", function () {
      if (typeof onCancel === "function") {
        onCancel();
      }
      toast.style.opacity = "0";
      setTimeout(() => toast.remove(), 300);
    });
  }

  return toast;
}

// Expose functions globally
window.showToast = showToast;
window.showConfirmToast = showConfirmToast;
