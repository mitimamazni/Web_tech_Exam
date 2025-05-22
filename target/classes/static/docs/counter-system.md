# Cart and Wishlist Counter System

## Overview

This is a unified counter system that ensures cart and wishlist counters are consistently updated across all pages of the e-commerce application. The system uses a combination of server-side updates, localStorage for cross-page synchronization, and custom events for intra-page communication.

## Features

- **Cross-Page Synchronization**: Updates made on one page are reflected on all open pages via localStorage
- **Consistent Counter Styling**: Uniform appearance of counters across the application
- **Server Verification**: Periodically verifies counter values with the server
- **Event-Based Communication**: Uses custom events to notify different components about counter changes
- **Multi-Tab Support**: Handles counter updates when multiple tabs are open

## Implementation Details

### Core Files

- `counter-unified.js`: Main implementation of the unified counter system
- `cart-counter.js`: Original cart counter implementation (now augmented by unified system)
- `wishlist-counter.js`: Original wishlist counter implementation (now augmented by unified system)

### How It Works

1. **Initialization**:
   - On page load, the system initializes counters from localStorage
   - It also performs a background fetch to verify counter values with the server
2. **Counter Updates**:
   - When a counter is updated, the new value is:
     - Updated in the DOM
     - Stored in localStorage
     - Broadcast via custom events
3. **Cross-Tab Synchronization**:

   - The system listens for localStorage changes to detect updates from other tabs
   - When detected, it updates the UI without triggering another localStorage update

4. **Periodic Verification**:
   - Every 5 minutes, the system verifies counter values with the server
   - When the page becomes visible after being in the background, it also verifies counter values

## Integration Points

The unified counter system integrates with the following pages:

- Home page
- Product detail page
- Product listing page
- Cart page
- Wishlist page

## API Functions

### Global Functions

- `updateCartCounter(count)`: Updates the cart counter throughout the application
- `updateWishlistCounter(count)`: Updates the wishlist counter throughout the application

### Utility Functions

The system exposes utility functions via the `window.counterUtils` object:

- `refreshCounters()`: Forces a refresh of counters from localStorage and server
- `forceServerSync()`: Forces synchronization with the server
- `setCartCount(count)`: Manually sets the cart counter to a specific value
- `setWishlistCount(count)`: Manually sets the wishlist counter to a specific value
- `resetCounters()`: Resets all counters to zero

## Testing

A test utility is included in `counter-test.js`. In development mode, press `Ctrl+Shift+T` to show the test panel.

The test panel allows:

- Incrementing/decrementing counters
- Manual refresh of counter data
- Verification of counter synchronization

For programmatic testing, use the `window.counterTestUtils` object in the browser console:

```javascript
// Show test panel
counterTestUtils.showTestPanel();

// Run full diagnostic test
counterTestUtils.runFullTest().then(console.table);
```

## Troubleshooting

If counters become out of sync:

1. Check browser console for error messages
2. Try refreshing the counters: `window.counterUtils.refreshCounters()`
3. Force server synchronization: `window.counterUtils.forceServerSync()`
4. If problems persist, reset counters and refresh the page: `window.counterUtils.resetCounters()`
