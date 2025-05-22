# BookByte Rebranding Guide

This document outlines the transition from "The Daily Grind" coffee e-commerce website to "BookByte" book store.

## Rebranding Overview

The website has been transformed from a coffee shop to a bookstore through comprehensive visual and content changes:

1. **Brand Identity**

   - Name changed from "The Daily Grind" to "BookByte"
   - Logo updated with book iconography
   - Favicon changed to book icon
   - Email changed to info@bookbyte.com
   - Address updated to "456 Literary Avenue"

2. **Color Scheme**

   - Primary colors changed from coffee browns to blues and purples
   - CSS variables renamed from `--coffee-*` to `--book-*`
   - Legacy variables maintained for backward compatibility

3. **Typography**

   - New font families: Merriweather, Playfair Display, and Roboto Slab
   - Font hierarchy established for different text elements

4. **Content**

   - Product categories changed to book genres
   - Coffee-specific terminology replaced with book-related terms
   - Product descriptions updated to focus on books

5. **UI Elements**
   - Icons updated to book-themed icons
   - Error pages updated with book-related imagery
   - Service sections renamed with book-specific terminology

## CSS Variable Migration

For backward compatibility, the following variable mappings are in place:

```css
--coffee-primary: var(--book-primary);
--coffee-dark: var(--book-dark);
--coffee-light: var(--book-light);
--coffee-accent: var(--book-accent);
--coffee-medium: var(--book-secondary);
--coffee-bg: var(--book-bg);
```

## Future Development Guidelines

1. **Use New Variables**: Always use `--book-*` variables for new development
2. **Text Content**: Ensure all new content uses book-related terminology
3. **Images**: Use book-related imagery for products and banners
4. **Gradual Replacement**: When modifying existing code, replace `--coffee-*` variables with `--book-*` equivalents
5. **Icon Usage**: Use book-themed icons from the Ionicons library

## Files to Reference

- `/css/bookbyte-theme.css`: Contains book-specific styling rules
- `/css/theme-migration.css`: Contains variable mappings for transition
- `/js/bookbyte-migration.js`: Script to dynamically replace coffee terms with book terms

## Technical Debt Notes

The following items may need attention in future updates:

1. Complete removal of `--coffee-*` variables in CSS
2. Update any remaining coffee-themed images or icons
3. Review and update product data in the database for book-specific attributes
4. Update search functionality for book-specific filters (author, ISBN, etc.)

## Contact

For questions about the rebranding, contact the development team at dev@bookbyte.com

---

Last updated: May 20, 2025
