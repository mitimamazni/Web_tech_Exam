/**
 * BookByte Theme Migration
 * This script helps to dynamically replace coffee-themed text with book-themed text
 */

document.addEventListener("DOMContentLoaded", function () {
  // Text replacement mapping
  const replacements = {
    // Product categories
    "Coffee Beans": "Fiction Books",
    "Brewing Equipment": "Non-Fiction Books",
    "coffee beans": "books",
    Accessories: "Book Accessories",
    "Gift Sets": "Book Collections",
    "Coffee Origin": "Book Genre",
    Ethiopia: "Fantasy",
    Colombia: "Mystery",
    Brazil: "Romance",
    Guatemala: "Biography",
    "Costa Rica": "Science Fiction",
    Honduras: "History",
    Kenya: "Young Adult",
    Peru: "Self-Help",
    Ecuador: "Poetry",

    // Shop content
    "Roast Level": "Reading Level",
    "Light Roast": "Beginner",
    "Medium Roast": "Intermediate",
    "Dark Roast": "Advanced",
    "Ground Coffee": "Paperback",
    "Whole Bean": "Hardcover",
    Espresso: "Reference",
    "Pour Over": "Classic",
    "French Press": "Modern",
    "Drip Coffee": "Bestseller",

    // Property replacements
    Aroma: "Synopsis",
    "Flavor Notes": "Themes",
    "Roast Date": "Publication Date",
    Origin: "Author",
    Process: "Publisher",
    Altitude: "Page Count",
    Varietal: "Edition",

    // Error references
    "Coffee Spill": "Book Error",
    "Broken Coffee Machine": "Torn Book",

    // General replacements
    "The Daily Grind": "BookByte",
    "Daily Grind": "BookByte",
    brewing: "reading",
    brew: "read",
    "coffee lover": "book lover",
    "coffee enthusiast": "book enthusiast",
    barista: "librarian",
    cafe: "bookstore",
    "cup of coffee": "good book",
    "info@thedailygrind.com": "info@bookbyte.com",
    "123 Coffee Street": "456 Literary Avenue",
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

  console.log("BookByte theme migration script loaded");
});
