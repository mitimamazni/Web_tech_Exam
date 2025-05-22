/**
 * Unified Counter System
 * Handles cart and wishlist counters across the application
 * Optimized to prevent UI freezing during operations
 */

(function () {
  "use strict";

  // Configuration
  const config = {
    updateDebounceTime: 100, // Debounce time for DOM updates (ms)
    syncDebounceTime: 500, // Debounce time for sync operations (ms)
    autoRefreshInterval: 60000, // Auto-refresh interval for counters (ms)
    requestTimeout: 5000, // API request timeout (ms)
    localStorageTTL: 300000, // Time to live for localStorage cache (5 minutes)
    retryDelay: 3000, // Retry delay after failure (ms)
    maxRetries: 2, // Maximum number of retries
    logLevel: "info", // Log level: debug, info, warn, error
  };

  // State management
  const state = {
    cartCount: 0,
    wishlistCount: 0,
    isInitialized: false,
    isUpdating: false,
    lastCartUpdate: 0,
    lastWishlistUpdate: 0,
    pendingOperations: new Set(),
    operationsInProgress: new Set(),
  };

  // DOM elements cache
  let cartCounters = [];
  let wishlistCounters = [];

  // Utility functions
  const utils = {
    // Debounce function to prevent excessive calls
    debounce: function (func, wait) {
      let timeout;
      return function () {
        const context = this;
        const args = arguments;
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(context, args), wait);
      };
    },

    // Log messages with appropriate level
    log: function (level, ...args) {
      const levels = { debug: 0, info: 1, warn: 2, error: 3 };
      const configLevel = levels[config.logLevel] || 1;

      if (levels[level] >= configLevel) {
        console[level]("Counter System:", ...args);
      }
    },

    // Safely parse JSON
    safeJSONParse: function (data, fallback) {
      try {
        // Check for invalid values before attempting to parse
        if (
          !data ||
          data === "NaN" ||
          data === "undefined" ||
          data === "null"
        ) {
          return fallback;
        }
        return JSON.parse(data);
      } catch (e) {
        utils.log("warn", "Failed to parse JSON data", e);
        return fallback;
      }
    },

    // Safe localStorage operations with error handling
    storage: {
      get: function (key, defaultValue = null) {
        try {
          const item = localStorage.getItem(key);
          if (!item) return defaultValue;

          // Handle special cases that aren't valid JSON
          if (item === "NaN" || item === "undefined" || item === "null") {
            utils.log(
              "debug",
              `Found non-JSON value in localStorage for key ${key}: ${item}`
            );
            return defaultValue;
          }

          // Handle direct numeric values that might not be JSON formatted
          if (!isNaN(item)) {
            const numericValue = parseInt(item, 10);
            if (!isNaN(numericValue)) {
              return numericValue;
            }
          }

          const parsed = utils.safeJSONParse(item, {
            value: defaultValue,
            timestamp: 0,
          });

          // Handle TTL expiration
          if (Date.now() - parsed.timestamp > config.localStorageTTL) {
            utils.storage.remove(key);
            return defaultValue;
          }

          return parsed.value;
        } catch (e) {
          utils.log("warn", "LocalStorage get error", e);
          return defaultValue;
        }
      },

      set: function (key, value) {
        try {
          // Use setTimeout to make localStorage operations async and non-blocking
          setTimeout(() => {
            // Ensure value is valid and not NaN or undefined
            if (
              value === null ||
              value === undefined ||
              (typeof value === "number" && isNaN(value))
            ) {
              utils.log(
                "warn",
                `Attempted to store invalid value for ${key}: ${value}`
              );
              // Store a default value instead of an invalid one
              localStorage.setItem(
                key,
                JSON.stringify({
                  value: 0,
                  timestamp: Date.now(),
                })
              );
              return;
            }

            const data = JSON.stringify({
              value: value,
              timestamp: Date.now(),
            });
            localStorage.setItem(key, data);
          }, 0);
        } catch (e) {
          utils.log("warn", "LocalStorage set error", e);
        }
      },

      remove: function (key) {
        try {
          localStorage.removeItem(key);
        } catch (e) {
          utils.log("warn", "LocalStorage remove error", e);
        }
      },
    },
  };

  // API operations
  const api = {
    fetchWishlistCount: utils.debounce(async function () {
      if (state.pendingOperations.has("fetchWishlistCount")) return;

      try {
        state.pendingOperations.add("fetchWishlistCount");
        utils.log("debug", "Fetching wishlist count from server");

        const response = await fetch("/api/wishlist/count", {
          method: "GET",
          headers: { Accept: "application/json" },
          credentials: "same-origin",
        });

        if (!response.ok) throw new Error(`HTTP error ${response.status}`);

        const contentType = response.headers.get("Content-Type");
        if (!contentType || !contentType.includes("application/json")) {
          throw new Error("Invalid content type received");
        }

        let data;
        try {
          data = await response.json();
        } catch (err) {
          utils.log(
            "warn",
            "Failed to parse wishlist count JSON response:",
            err
          );
          return utils.storage.get("wishlistCount", 0);
        }

        // Debug the response structure
        utils.log("debug", "Wishlist count API response:", data);

        let count = 0;

        // Handle various response formats (including the Spring Boot ApiResponseDTO pattern)
        if (typeof data === "number") {
          count = data;
        } else if (data && data.count !== undefined) {
          count = parseInt(data.count, 10);
        } else if (data && data.success && data.data !== undefined) {
          // Standard ApiResponseDTO format used in your Spring Boot controllers
          count = parseInt(data.data || 0, 10);
        } else if (data && typeof data === "object") {
          // Try to find any number property that might contain count
          for (const key in data) {
            if (typeof data[key] === "number") {
              count = data[key];
              break;
            }
          }
        }

        // Ensure count is a valid number
        if (isNaN(count)) {
          count = 0;
        }

        state.wishlistCount = count;
        utils.storage.set("wishlistCount", count);
        state.lastWishlistUpdate = Date.now();

        return count;
      } catch (e) {
        utils.log("error", "Error fetching wishlist count", e);
        return utils.storage.get("wishlistCount", 0);
      } finally {
        state.pendingOperations.delete("fetchWishlistCount");
      }
    }, config.syncDebounceTime),

    fetchCartCount: utils.debounce(async function () {
      if (state.pendingOperations.has("fetchCartCount")) return;

      try {
        state.pendingOperations.add("fetchCartCount");
        utils.log("debug", "Fetching cart count from server");

        // Try to get username from various sources
        let username = null;
        try {
          // Order of priority: window global -> localStorage -> data attribute
          if (typeof window.username === "string" && window.username.trim()) {
            username = window.username.trim();
          } else if (localStorage.getItem("username")) {
            username = localStorage.getItem("username").trim();
          } else {
            const usernameElem = document.querySelector("[data-username]");
            if (usernameElem) {
              const attrValue = usernameElem.getAttribute("data-username");
              if (attrValue && attrValue.trim()) {
                username = attrValue.trim();
              }
            }
          }
        } catch (err) {
          utils.log("warn", "Could not retrieve username", err);
        }

        // If no username is found, return cached count
        if (!username) {
          utils.log("warn", "No username available for cart count fetch");
          return utils.storage.get("cartCount", 0);
        }

        // Remove any null, undefined, or "null" string values
        if (
          username === "null" ||
          username === "undefined" ||
          !username.trim()
        ) {
          utils.log("warn", "Invalid username value detected:", username);
          return utils.storage.get("cartCount", 0);
        }

        utils.log("debug", "Using username for cart count:", username);

        // Make API request with username parameter
        // Ensure URL has the correct endpoint
        const apiUrl = `/api/cart/count?username=${encodeURIComponent(
          username
        )}`;
        utils.log("debug", "Fetching cart count from URL:", apiUrl);

        const response = await fetch(apiUrl, {
          method: "GET",
          headers: {
            Accept: "application/json",
            "Cache-Control": "no-cache",
          },
          credentials: "same-origin",
          cache: "no-store", // Prevent caching
        }).catch((err) => {
          utils.log("error", "Network error when fetching cart count:", err);
          return { ok: false, status: 0 };
        });

        // Handle non-OK responses gracefully
        if (!response.ok) {
          utils.log(
            "warn",
            `HTTP error ${response.status} when fetching cart count`
          );
          return utils.storage.get("cartCount", 0);
        }

        const contentType = response.headers.get("Content-Type");
        if (!contentType || !contentType.includes("application/json")) {
          throw new Error("Invalid content type received");
        }

        let data;
        try {
          data = await response.json();
        } catch (err) {
          utils.log("warn", "Failed to parse cart count JSON response:", err);
          return utils.storage.get("cartCount", 0);
        }

        let count = 0;

        // Debug the response structure
        utils.log("debug", "Cart count API response:", data);

        // Handle various response formats (including the Spring Boot ApiResponseDTO pattern)
        if (typeof data === "number") {
          count = data;
        } else if (data && data.data !== undefined) {
          // First try to handle ApiResponseDTO format
          count =
            typeof data.data === "number"
              ? data.data
              : parseInt(data.data || "0", 10);
        } else if (data && data.success && data.data !== undefined) {
          // Standard ApiResponseDTO format used in your Spring Boot controllers
          count = parseInt(data.data || 0, 10);
        } else if (data && data.count !== undefined) {
          count = parseInt(data.count, 10);
        }

        // Ensure count is a valid number
        if (isNaN(count)) {
          count = 0;
        }

        state.cartCount = count;
        utils.storage.set("cartCount", count);
        state.lastCartUpdate = Date.now();

        return count;
      } catch (e) {
        utils.log("error", "Error fetching cart count", e);
        return utils.storage.get("cartCount", 0);
      } finally {
        state.pendingOperations.delete("fetchCartCount");
      }
    }, config.syncDebounceTime),
  };

  // UI operations
  const ui = {
    updateCartCounter: utils.debounce(function (count) {
      try {
        if (state.isUpdating) return;
        state.isUpdating = true;

        // Find all cart counters if not already cached
        if (cartCounters.length === 0) {
          cartCounters = document.querySelectorAll(".cart-counter");
          utils.log("debug", `Found ${cartCounters.length} cart counters`);
        }

        // Make sure count is a valid number, explicitly handle NaN
        let safeCount = parseInt(count || 0, 10);
        if (isNaN(safeCount)) {
          utils.log(
            "debug",
            `Converting NaN cart count to 0, original value: ${count}`
          );
          safeCount = 0;
        }

        // Update all cart counters
        cartCounters.forEach((counter) => {
          if (counter && counter !== undefined && counter !== null) {
            try {
              // Safely convert count to a string to avoid toString errors
              counter.textContent = String(safeCount);
              // Only manipulate classes if counter has classList
              if (counter.classList) {
                counter.classList.toggle("empty", safeCount === 0);
                counter.classList.toggle("has-items", safeCount > 0);
              }
            } catch (err) {
              utils.log("warn", "Error updating individual cart counter", err);
            }
          }
        });
      } catch (e) {
        utils.log("error", "Error updating cart counters", e);
      } finally {
        state.isUpdating = false;
      }
    }, config.updateDebounceTime),

    updateWishlistCounter: utils.debounce(function (count) {
      try {
        if (state.isUpdating) return;
        state.isUpdating = true;

        // Find all wishlist counters if not already cached
        if (wishlistCounters.length === 0) {
          wishlistCounters = document.querySelectorAll(".wishlist-counter");
          utils.log(
            "debug",
            `Found ${wishlistCounters.length} wishlist counters`
          );
        }

        // Make sure count is a valid number, explicitly handle NaN
        let safeCount = parseInt(count || 0, 10);
        if (isNaN(safeCount)) {
          utils.log(
            "debug",
            `Converting NaN wishlist count to 0, original value: ${count}`
          );
          safeCount = 0;
        }

        // Update all wishlist counters
        wishlistCounters.forEach((counter) => {
          if (counter && counter !== undefined && counter !== null) {
            try {
              // Safely convert count to a string to avoid toString errors
              counter.textContent = String(safeCount);
              // Only manipulate classes if counter has classList
              if (counter.classList) {
                counter.classList.toggle("empty", safeCount === 0);
                counter.classList.toggle("has-items", safeCount > 0);
              }
            } catch (err) {
              utils.log(
                "warn",
                "Error updating individual wishlist counter",
                err
              );
            }
          }
        });
      } catch (e) {
        utils.log("error", "Error updating wishlist counters", e);
      } finally {
        state.isUpdating = false;
      }
    }, config.updateDebounceTime),
  };

  // Core functions
  const core = {
    init: function () {
      if (state.isInitialized) return;

      utils.log("info", "Initializing counter system");

      // Pre-cache DOM elements with null checks
      try {
        cartCounters = Array.from(
          document.querySelectorAll(".cart-counter") || []
        );
        wishlistCounters = Array.from(
          document.querySelectorAll(".wishlist-counter") || []
        );

        // Filter out any null/undefined elements
        cartCounters = cartCounters.filter(
          (counter) => counter !== null && counter !== undefined
        );
        wishlistCounters = wishlistCounters.filter(
          (counter) => counter !== null && counter !== undefined
        );

        utils.log(
          "debug",
          `Found ${cartCounters.length} cart counters and ${wishlistCounters.length} wishlist counters`
        );
      } catch (err) {
        utils.log("warn", "Error caching counter elements:", err);
        cartCounters = [];
        wishlistCounters = [];
      }

      // Initialize from localStorage with enhanced safety checks
      try {
        // Get cart count with fallback
        let cartCount = utils.storage.get("cartCount", 0);
        cartCount =
          cartCount === null || cartCount === undefined || isNaN(cartCount)
            ? 0
            : cartCount;
        state.cartCount = cartCount;

        // Get wishlist count with fallback
        let wishlistCount = utils.storage.get("wishlistCount", 0);
        wishlistCount =
          wishlistCount === null ||
          wishlistCount === undefined ||
          isNaN(wishlistCount)
            ? 0
            : wishlistCount;
        state.wishlistCount = wishlistCount;
      } catch (err) {
        utils.log("warn", "Error initializing counts from localStorage:", err);
        state.cartCount = 0;
        state.wishlistCount = 0;
      }

      // Update DOM counters with cached values first (safely)
      const safeCartCount = parseInt(state.cartCount || 0, 10) || 0; // Double fallback to 0
      const safeWishlistCount = parseInt(state.wishlistCount || 0, 10) || 0; // Double fallback to 0

      // Safely update cart counters
      if (cartCounters.length > 0) {
        cartCounters.forEach((counter) => {
          if (counter) counter.textContent = String(safeCartCount);
        });
      }

      // Safely update wishlist counters
      if (wishlistCounters.length > 0) {
        wishlistCounters.forEach((counter) => {
          if (counter) counter.textContent = String(safeWishlistCount);
        });
      }

      // Set up storage event listener for cross-tab synchronization
      window.addEventListener("storage", function (e) {
        try {
          // Skip if no key or newValue is invalid
          if (
            !e.key ||
            !e.newValue ||
            e.newValue === "NaN" ||
            e.newValue === "undefined" ||
            e.newValue === "null"
          ) {
            return;
          }

          if (e.key === "cartCount") {
            // Handle direct numeric values or properly formatted JSON
            let count = 0;
            if (!isNaN(e.newValue)) {
              // Handle direct numeric values
              count = parseInt(e.newValue, 10);
            } else {
              // Handle JSON formatted values
              const data = utils.safeJSONParse(e.newValue, { value: 0 });
              count = parseInt(data.value || 0, 10);
            }
            // Ensure count is a valid number
            count = isNaN(count) ? 0 : count;
            state.cartCount = count;
            ui.updateCartCounter(count);
          } else if (e.key === "wishlistCount") {
            // Handle direct numeric values or properly formatted JSON
            let count = 0;
            if (!isNaN(e.newValue)) {
              // Handle direct numeric values
              count = parseInt(e.newValue, 10);
            } else {
              // Handle JSON formatted values
              const data = utils.safeJSONParse(e.newValue, { value: 0 });
              count = parseInt(data.value || 0, 10);
            }
            // Ensure count is a valid number
            count = isNaN(count) ? 0 : count;
            state.wishlistCount = count;
            ui.updateWishlistCounter(count);
          }
        } catch (err) {
          utils.log("error", "Error handling storage event", err);
        }
      });

      // Set up visibility change listener to refresh counters when tab becomes visible
      document.addEventListener("visibilitychange", function () {
        if (document.visibilityState === "visible") {
          // Use a timeout to prevent freezing when switching tabs
          setTimeout(() => {
            core.refreshCounts();
          }, 300);
        }
      });

      // Refresh counts from server (with a delay to prevent initial UI freezing)
      setTimeout(() => {
        core.refreshCounts();

        // Set up periodic refresh
        setInterval(core.refreshCounts, config.autoRefreshInterval);
      }, 1000);

      state.isInitialized = true;
      utils.log("info", "Counter system initialized successfully");
    },

    refreshCounts: async function () {
      // Handle cart count and wishlist count separately to prevent one error from affecting the other
      try {
        let cartCount = await api.fetchCartCount();
        // Ensure cart count is never NaN
        if (isNaN(cartCount)) {
          utils.log("debug", "Received NaN from fetchCartCount, using 0");
          cartCount = 0;
        }
        if (cartCount !== undefined && cartCount !== state.cartCount) {
          ui.updateCartCounter(cartCount);
        }
      } catch (e) {
        utils.log("error", "Error fetching cart count during refresh", e);
      }

      try {
        let wishlistCount = await api.fetchWishlistCount();
        // Ensure wishlist count is never NaN
        if (isNaN(wishlistCount)) {
          utils.log("debug", "Received NaN from fetchWishlistCount, using 0");
          wishlistCount = 0;
        }
        if (
          wishlistCount !== undefined &&
          wishlistCount !== state.wishlistCount
        ) {
          ui.updateWishlistCounter(wishlistCount);
        }
      } catch (e) {
        utils.log("error", "Error fetching wishlist count during refresh", e);
      }
    },

    // Exposed APIs
    updateCartCount: function (count) {
      // If count is NaN, explicitly use 0 instead of returning
      if (count === undefined) return;
      if (isNaN(count)) {
        utils.log("debug", "Received NaN cart count, using 0 instead");
        count = 0;
      }

      state.cartCount = parseInt(count, 10);
      // Double check that we never set NaN
      if (isNaN(state.cartCount)) state.cartCount = 0;
      ui.updateCartCounter(state.cartCount);
      utils.storage.set("cartCount", state.cartCount);
    },

    updateWishlistCount: function (count) {
      // If count is NaN, explicitly use 0 instead of returning
      if (count === undefined) return;
      if (isNaN(count)) {
        utils.log("debug", "Received NaN wishlist count, using 0 instead");
        count = 0;
      }

      state.wishlistCount = parseInt(count, 10);
      // Double check that we never set NaN
      if (isNaN(state.wishlistCount)) state.wishlistCount = 0;
      ui.updateWishlistCounter(state.wishlistCount);
      utils.storage.set("wishlistCount", state.wishlistCount);
    },

    incrementCartCount: function () {
      core.updateCartCount(state.cartCount + 1);
    },

    decrementCartCount: function () {
      if (state.cartCount > 0) {
        core.updateCartCount(state.cartCount - 1);
      }
    },

    incrementWishlistCount: function () {
      core.updateWishlistCount(state.wishlistCount + 1);
    },

    decrementWishlistCount: function () {
      if (state.wishlistCount > 0) {
        core.updateWishlistCount(state.wishlistCount - 1);
      }
    },

    getState: function () {
      return {
        cart: state.cartCount,
        wishlist: state.wishlistCount,
        lastCartUpdate: state.lastCartUpdate,
        lastWishlistUpdate: state.lastWishlistUpdate,
      };
    },
  };

  // Initialize when DOM is ready
  function ready(fn) {
    if (document.readyState !== "loading") {
      // DOM already loaded, call with a small delay to ensure all scripts are processed
      setTimeout(fn, 200);
    } else {
      // Wait for DOM to load, then call with a delay
      document.addEventListener("DOMContentLoaded", function () {
        setTimeout(fn, 200);
      });
    }

    // Also add a window load event as a fallback
    window.addEventListener("load", function () {
      if (!state.isInitialized) {
        setTimeout(fn, 200);
      }
    });
  }

  ready(function () {
    // Initialize with a delay to let other scripts load
    try {
      core.init();
    } catch (e) {
      utils.log("error", "Error initializing counter system", e);
      // Try again after a delay
      setTimeout(core.init, 500);
    }
  });

  // Expose public API
  window.CounterSystem = {
    updateCartCount: core.updateCartCount,
    updateWishlistCount: core.updateWishlistCount,
    incrementCartCount: core.incrementCartCount,
    decrementCartCount: core.decrementCartCount,
    incrementWishlistCount: core.incrementWishlistCount,
    decrementWishlistCount: core.decrementWishlistCount,
    refresh: core.refreshCounts,
    getState: core.getState,
  };

  // Keep the old reference for backward compatibility
  window.counterSystem = window.CounterSystem;

  // Also attach to document for easier event-based updates
  document.addEventListener("cart:updated", function (event) {
    if (event.detail && typeof event.detail.count !== "undefined") {
      core.updateCartCount(event.detail.count);
    }
  });

  document.addEventListener("wishlist:updated", function (event) {
    if (event.detail && typeof event.detail.count !== "undefined") {
      core.updateWishlistCount(event.detail.count);
    }
  });

  // Refresh counts every 60 seconds to keep counters in sync
  setInterval(core.refreshCounts, 60000);
})();
