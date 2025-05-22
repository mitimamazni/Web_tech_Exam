// src/main/resources/static/js/home-product-meta.js
// Home page product meta: badge, rating, and display order for home page sections
// This file now uses the centralized configuration from app-config.js
// Kept for backward compatibility - in the future, components should use APP_CONFIG directly

/**
 * @deprecated Use APP_CONFIG.product.homePage instead
 * This function converts the new configuration format to the old format for backwards compatibility
 */
function buildProductMetaFromConfig() {
  // Ensure APP_CONFIG is available
  if (typeof APP_CONFIG === "undefined") {
    console.error(
      "APP_CONFIG is not loaded. Make sure app-config.js is included before this script."
    );
    return {};
  }

  const result = {};

  // Process new arrivals
  if (APP_CONFIG.product.homePage.newArrivals) {
    APP_CONFIG.product.homePage.newArrivals.forEach((product) => {
      result[product.id] = {
        rating: product.rating,
        badge: product.badge,
        section: "new-arrivals",
        order: product.order,
      };
    });
  }

  // Process featured products
  if (APP_CONFIG.product.homePage.featured) {
    APP_CONFIG.product.homePage.featured.forEach((product) => {
      result[product.id] = {
        rating: product.rating,
        badge: product.badge,
        section: "featured",
        order: product.order,
      };
    });
  }

  return result;
}

// Create the HOME_PRODUCT_META object for backward compatibility
const HOME_PRODUCT_META = buildProductMetaFromConfig();

// Export for use in other scripts
if (typeof window !== "undefined") {
  window.HOME_PRODUCT_META = HOME_PRODUCT_META;
}
