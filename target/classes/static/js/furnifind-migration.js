/**
 * FurniFind Theme Migration
 * This script helps to dynamically replace book-themed text with furniture-themed text
 */

document.addEventListener("DOMContentLoaded", function () {
  // Text replacement mapping
  const replacements = {
    // Product categories
    "Fiction Books": "Bedroom Furniture",
    "Non-Fiction Books": "Living Room Furniture",
    books: "furniture",
    "Book Accessories": "Home Decor",
    "Book Collections": "Furniture Sets",
    "Book Genre": "Furniture Style",
    Fantasy: "Scandinavian",
    Mystery: "Mid-Century",
    Romance: "Modern",
    Biography: "Industrial",
    "Science Fiction": "Contemporary",
    History: "Traditional",
    "Young Adult": "Minimalist",
    "Self-Help": "Bohemian",
    Poetry: "Rustic",

    // Shop content
    "Reading Level": "Material Type",
    Beginner: "Wood",
    Intermediate: "Metal",
    Advanced: "Leather",
    Paperback: "Fabric",
    Hardcover: "Solid Wood",
    Reference: "Glass",
    Classic: "Marble",
    Modern: "Engineered Wood",
    Bestseller: "Custom Design",

    // Property replacements
    Synopsis: "Description",
    Themes: "Features",
    "Publication Date": "Manufacturing Date",
    Author: "Designer",
    Publisher: "Manufacturer",
    "Page Count": "Dimensions",
    Edition: "Collection",

    // Error references
    "Book Error": "Assembly Issue",
    "Torn Book": "Damaged Furniture",

    // General replacements
    BookByte: "FurniFind",
    reading: "designing",
    read: "design",
    "book lover": "furniture enthusiast",
    "book enthusiast": "interior design enthusiast",
    librarian: "interior designer",
    bookstore: "furniture showroom",
    "good book": "quality furniture",
    "info@bookbyte.com": "info@furnifind.com",
    "456 Literary Avenue": "789 Design Boulevard, Portland",
  };

  // Walk through the DOM and replace text nodes
  function walkTextNodes(node, replacer) {
    if (node.nodeType === 3) {
      // Text node
      let text = node.nodeValue;
      let changed = false;

      // Apply replacements
      for (const [search, replace] of Object.entries(replacements)) {
        // Case-insensitive search with word boundaries
        const regex = new RegExp("\\b" + search + "\\b", "gi");
        if (regex.test(text)) {
          text = text.replace(regex, replace);
          changed = true;
        }
      }

      if (changed) {
        node.nodeValue = text;
      }
    } else if (node.nodeType === 1) {
      // Element node
      // Skip script and style elements
      if (node.nodeName !== "SCRIPT" && node.nodeName !== "STYLE") {
        // Process children
        const childNodes = Array.from(node.childNodes);
        childNodes.forEach((child) => walkTextNodes(child, replacer));
      }
    }
  }

  // Process placeholder attributes for inputs
  function processPlaceholders() {
    const inputs = document.querySelectorAll(
      "input[placeholder], textarea[placeholder]"
    );
    inputs.forEach((input) => {
      const placeholder = input.getAttribute("placeholder");
      if (placeholder) {
        let newPlaceholder = placeholder;
        for (const [search, replace] of Object.entries(replacements)) {
          // Case-insensitive search with word boundaries
          const regex = new RegExp("\\b" + search + "\\b", "gi");
          if (regex.test(newPlaceholder)) {
            newPlaceholder = newPlaceholder.replace(regex, replace);
          }
        }

        if (newPlaceholder !== placeholder) {
          input.setAttribute("placeholder", newPlaceholder);
        }
      }
    });
  }

  // Run the text replacement
  walkTextNodes(document.body, replacements);
  processPlaceholders();

  // Also run after any AJAX content is loaded
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.addedNodes && mutation.addedNodes.length > 0) {
        mutation.addedNodes.forEach((node) => {
          walkTextNodes(node, replacements);
        });
        processPlaceholders();
      }
    });
  });

  // Start observing the document for DOM changes
  observer.observe(document.body, {
    childList: true,
    subtree: true,
  });

  console.log("FurniFind theme migration script loaded");
});
