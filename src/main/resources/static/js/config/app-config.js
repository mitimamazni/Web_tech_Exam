/**
 * app-config.js
 * Centralized configuration for the e-commerce application
 * All configuration values previously hardcoded in multiple files are now consolidated here
 */

const APP_CONFIG = {
  // Product display configuration
  product: {
    // Home page product metadata configurations
    homePage: {
      // New Arrivals section configuration
      newArrivals: [
        { id: 101, rating: null, badge: null, order: 1 },
        { id: 102, rating: null, badge: null, order: 2 },
        { id: 103, rating: null, badge: null, order: 3 },
        { id: 104, rating: null, badge: null, order: 4 },
      ],

      // Featured Products section configuration
      featured: [
        { id: 201, rating: 4, badge: "15%", order: 1 },
        { id: 202, rating: 4, badge: "Sale", order: 2 },
        { id: 203, rating: 4.5, badge: "NEW", order: 3 },
        { id: 204, rating: 5, badge: null, order: 4 },
        { id: 205, rating: 4.5, badge: "20%", order: 5 },
        { id: 206, rating: 4, badge: null, order: 6 },
      ],
    },
  },

  // Modal system configuration
  modal: {
    // Animation timing in milliseconds
    animationDuration: 300,

    // Default z-index values
    zIndex: {
      overlay: 999999,
      content: 1000000,
    },

    // Style settings
    styles: {
      overlayBackground: "rgba(0, 0, 0, 0.5)",
      contentMargin: "5% auto",
    },

    // Feature toggles
    features: {
      enableAnimation: true,
      enableKeyboardNavigation: true,
      closeOnBackgroundClick: true,
    },
  },

  // Counter system configuration
  counter: {
    // Animation timing in milliseconds
    animationDuration: 500,

    // Visual appearance
    appearance: {
      useEnhancedAnimations: true,
      badgeBackground: "#ff4400",
      badgeTextColor: "#ffffff",
    },
  },

  // API endpoints (relative to the application)
  api: {
    products: "/api/products",
    cart: "/api/cart",
    wishlist: "/api/wishlist",
    checkout: "/api/checkout",
    orders: "/api/orders",
  },

  // Feature flags
  features: {
    enableWishlist: true,
    enableQuickView: true,
    enableProductComparison: false,
    enableRecentlyViewed: true,

    // Debug settings - should be off in production
    debug: {
      enableLogging: false,
      showDevTools: false,
    },
  },
};

// Export for use in other scripts
if (typeof window !== "undefined") {
  window.APP_CONFIG = APP_CONFIG;
}

// For node.js environments (if needed)
if (typeof module !== "undefined" && module.exports) {
  module.exports = APP_CONFIG;
}
