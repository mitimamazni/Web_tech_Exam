# Modal System - Troubleshooting Guide

This guide helps resolve issues with the modal system in The Daily Grind e-commerce website.

## Common Issues and Solutions

### 1. Modals Not Appearing

If modals don't appear when clicking the "Show All" category buttons:

- **Solution 1**: Make sure `modal-unified.js` is properly included in your HTML file:

  ```html
  <script th:src="@{/js/modal-unified.js}"></script>
  ```

- **Solution 2**: Check browser console for JavaScript errors and fix any issues

- **Solution 3**: Add the following CSS to ensure modals appear above all other elements:

  ```css
  .modal {
    z-index: 999999 !important;
    position: fixed !important;
  }

  .modal-content {
    z-index: 1000000 !important;
  }
  ```

### 2. Modal Opens But Immediately Closes

- **Solution**: Ensure event handlers are correctly implemented and not propagating events incorrectly. Use our modal-unified.js which properly stops event propagation.

### 3. Can't Close Modal

- **Solution 1**: Check that close button has proper event handling:

  ```javascript
  document.querySelectorAll(".close-modal").forEach((btn) => {
    btn.onclick = function (e) {
      e.preventDefault();
      closeModal(this.closest(".modal").id);
      return false;
    };
  });
  ```

- **Solution 2**: Make sure the Escape key handler is working:
  ```javascript
  document.onkeydown = function (event) {
    if (event.key === "Escape") {
      document
        .querySelectorAll('.modal:not([style*="display: none"])')
        .forEach((modal) => {
          closeModal(modal.id);
        });
    }
  };
  ```

### 4. Products Not Loading in Modal

- **Solution 1**: Check that the `loadProductsForCategory` function is properly defined
- **Solution 2**: Verify the server endpoint for product data is working correctly
- **Solution 3**: Make sure the product container structure is correct:
  ```html
  <div class="modal-products">
    <!-- Products will be loaded here -->
  </div>
  ```

## Diagnostic Tools

We've created several diagnostic tools to help identify and fix modal issues:

1. **Modal Unified Script**: Consolidates modal functionality and fixes common issues

   - File: `/js/modal-unified.js`

2. **Modal Diagnostic Tool**: Real-time diagnosis and fixing of modal issues

   - File: `/js/modal-diagnostic.js`
   - Usage: Add to any page to get a floating diagnostic panel

3. **Modal Test Suite**: Comprehensive test and fix suite for all modal functionality
   - URL: `/admin/modal-test-suite.html`

## Implementation Requirements

For modals to work correctly, ensure your HTML follows this structure:

```html
<div id="category-name-modal" class="modal">
  <div class="modal-content">
    <span class="close-modal">&times;</span>
    <div class="modal-header">
      <h2>Category Name</h2>
    </div>
    <div class="modal-products">
      <!-- Products will be loaded dynamically -->
    </div>
    <div class="modal-pagination"></div>
  </div>
</div>
```

And category buttons should use this pattern:

```html
<a
  href="javascript:void(0);"
  class="category-btn"
  onclick="openCategoryModal('category-name'); return false;"
  >Show All</a
>
```

## Emergency Fix

If modals are completely broken and you need a quick fix, add this to your page:

```html
<script>
  document.addEventListener("DOMContentLoaded", function () {
    // Emergency override for modal functionality
    window.openCategoryModal = function (category) {
      const modalId = `${category}-modal`;
      const modal = document.getElementById(modalId);

      if (!modal) return false;

      // Close other modals
      document.querySelectorAll(".modal").forEach((m) => {
        m.style.display = "none";
      });

      // Force show this modal
      modal.style.display = "block";
      document.body.style.overflow = "hidden";

      return false;
    };

    window.closeModal = function (modalId) {
      const modal =
        typeof modalId === "string"
          ? document.getElementById(modalId)
          : modalId;
      if (!modal) return;

      modal.style.display = "none";
      document.body.style.overflow = "auto";
    };

    // Fix close buttons
    document.querySelectorAll(".close-modal").forEach((btn) => {
      btn.onclick = function () {
        closeModal(this.closest(".modal").id);
        return false;
      };
    });
  });
</script>
```

## Support

If you continue experiencing issues, please check the browser console for errors and contact the development team.
