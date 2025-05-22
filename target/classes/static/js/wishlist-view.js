/**
 * Wishlist View Page Script
 * This script handles rendering wishlist items from localStorage
 */
document.addEventListener("DOMContentLoaded", function () {
  // Get wishlist container element
  const wishlistContainer = document.querySelector(".wishlist-products");
  if (!wishlistContainer) return;

  // Set data attribute for the container to identify it for the WishlistStorage module
  wishlistContainer.classList.add("wishlist-items");

  // Initialize display
  WishlistStorage.updateWishlistUI();

  // Add event listener for wishlist updates
  document.addEventListener("wishlist:updated", function (e) {
    // If all items were removed, show empty wishlist message
    if (e.detail.count === 0) {
      const emptySection = document.querySelector(".wishlist-empty-section");
      const itemsSection = document.querySelector(".wishlist-items-section");

      if (emptySection && itemsSection) {
        emptySection.style.display = "block";
        itemsSection.style.display = "none";
      } else {
        location.reload(); // Fallback if sections not found
      }
    }
  });

  // Handle clear wishlist button if present
  const clearWishlistBtn = document.querySelector(".clear-wishlist-btn");
  if (clearWishlistBtn) {
    clearWishlistBtn.addEventListener("click", function (e) {
      e.preventDefault();

      // Confirm before clearing
      if (confirm("Are you sure you want to clear your wishlist?")) {
        WishlistStorage.clearWishlist();

        // Show empty wishlist section
        const emptySection = document.querySelector(".wishlist-empty-section");
        const itemsSection = document.querySelector(".wishlist-items-section");

        if (emptySection && itemsSection) {
          emptySection.style.display = "block";
          itemsSection.style.display = "none";
        } else {
          location.reload(); // Fallback if sections not found
        }
      }
    });
  }

  // Update wishlist count in the header
  WishlistStorage.updateWishlistUI();
});
