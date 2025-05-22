// src/main/resources/static/js/product-list-fetch.js
// Fetch and render products from API for the product list page

document.addEventListener("DOMContentLoaded", function () {
  const productGrid = document.getElementById("product-grid");
  if (!productGrid) return;

  // Keep track of wishlist items
  let wishlistItems = new Set();

  // Function to fetch current wishlist items
  function fetchWishlistItems() {
    fetch("/wishlist/items-json")
      .then((res) => res.json())
      .then((data) => {
        if (data.success && data.data && Array.isArray(data.data)) {
          // Store product IDs in the Set
          wishlistItems = new Set(data.data.map((item) => item.productId));

          // Update any rendered wishlist buttons
          updateWishlistButtonStates();
        }
      })
      .catch((err) => console.error("Error fetching wishlist data:", err));
  }

  // Function to update wishlist button states
  function updateWishlistButtonStates() {
    document.querySelectorAll(".wishlist-btn").forEach((btn) => {
      const productId = btn.getAttribute("data-product-id");
      if (productId && wishlistItems.has(parseInt(productId))) {
        btn.classList.add("active");
        btn.innerHTML = '<ion-icon name="heart"></ion-icon>';
      }
    });
  }

  // Fetch wishlist items when page loads
  fetchWishlistItems();

  function renderProductCard(product) {
    const isSale = product.salePrice && product.salePrice > 0;
    const isOut = product.stockQuantity === 0 || !product.active;
    const badge = isOut
      ? '<div class="product-badge out">Sold Out</div>'
      : isSale
      ? '<div class="product-badge">Sale</div>'
      : product.createdAt &&
        Date.now() - new Date(product.createdAt).getTime() <
          1000 * 60 * 60 * 24 * 30
      ? '<div class="product-badge new">New</div>'
      : "";
    const priceHtml = isSale
      ? `<span class="product-price">$${product.price.toFixed(
          2
        )}</span> <span class="product-price-old">$${product.salePrice.toFixed(
          2
        )}</span>`
      : `<span class="product-price">$${product.price.toFixed(2)}</span>`;
    const rating = product.averageRating || 0;
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    let starsHtml = "";
    for (let i = 0; i < 5; i++) {
      if (i < fullStars) starsHtml += '<ion-icon name="star"></ion-icon>';
      else if (i === fullStars && halfStar)
        starsHtml += '<ion-icon name="star-half"></ion-icon>';
      else starsHtml += '<ion-icon name="star-outline"></ion-icon>';
    }
    const productUrl = `/products/${product.id}`;
    return `
      <div class="product-card">
        ${badge}
        <div class="product-tumb">
          <img src="${
            product.imageUrl ||
            (product.images &&
              product.images[0] &&
              product.images[0].imageUrl) ||
            ""
          }" alt="${product.name}" />
          <div class="product-quick-actions">
            <button class="quick-action-btn wishlist-btn" data-product-id="${
              product.id
            }" data-wishlist-action="toggle" title="Add to Wishlist">
              <ion-icon name="heart-outline"></ion-icon>
            </button>
            <a class="quick-action-btn view-btn link-styled" href="${productUrl}" title="Quick View">
              <ion-icon name="eye-outline"></ion-icon>
            </a>
          </div>
        </div>
        <div class="product-details">
          <div class="product-category">${
            product.category ? product.category.name : ""
          }</div>
          <h4 class="product-title">
            <a href="${productUrl}">${product.name}</a>
          </h4>
          <div class="product-rating">
            ${starsHtml}
            <span>(${product.averageRating || 0})</span>
          </div>
          <div class="product-price-container">
            ${priceHtml}
          </div>
          <div class="product-description">
            ${product.description}
          </div>
          <div class="product-action">
            ${
              isOut
                ? `<button class="add-to-cart-btn out-of-stock" disabled><ion-icon name="alert-circle-outline"></ion-icon> Out of Stock</button>`
                : `<button class="add-to-cart-btn" data-product-id="${product.id}" data-cart-action="add"><ion-icon name="cart-outline"></ion-icon> Add to Cart</button>`
            }
          </div>
        </div>
      </div>
    `;
  }

  // Current page state
  let currentPage = 0;
  const pageSize = 20; // Increased page size

  function fetchAndRenderProducts(page = 0) {
    currentPage = page;
    productGrid.innerHTML = '<div class="loading">Loading products...</div>';

    // Use the correct API endpoint and increase page size
    fetch(`/api/products?page=${page}&size=${pageSize}&sortBy=id&direction=asc`)
      .then((res) => res.json())
      .then((json) => {
        if (!json.success || !json.data || !json.data.content) {
          productGrid.innerHTML =
            '<div class="error">Failed to load products.</div>';
          return;
        }
        const products = json.data.content;
        if (!products.length) {
          productGrid.innerHTML = '<div class="info">No products found.</div>';
          return;
        }

        productGrid.innerHTML = products.map(renderProductCard).join("");

        // Initialize Add to Cart buttons after rendering
        initAddToCartButtons();

        // Create or update pagination
        updatePagination(json.data.totalPages);
      })
      .catch((error) => {
        console.error("API Error:", error);
        productGrid.innerHTML =
          '<div class="error">Could not connect to API.</div>';
      });
  }

  // Function to create pagination controls
  function updatePagination(totalPages) {
    const paginationElement = document.getElementById("pagination");
    if (!paginationElement) return;

    paginationElement.innerHTML = "";

    if (totalPages <= 1) return;

    // Add previous page button
    const prevBtn = document.createElement("button");
    prevBtn.classList.add("pagination-btn", "prev-btn");
    prevBtn.innerHTML = "&laquo; Previous";
    prevBtn.disabled = currentPage === 0;
    prevBtn.addEventListener("click", () =>
      fetchAndRenderProducts(currentPage - 1)
    );
    paginationElement.appendChild(prevBtn);

    // Add page number buttons (show 5 pages max)
    const startPage = Math.max(0, Math.min(currentPage - 2, totalPages - 5));
    const endPage = Math.min(totalPages, startPage + 5);

    for (let i = startPage; i < endPage; i++) {
      const pageBtn = document.createElement("button");
      pageBtn.classList.add("pagination-btn", "page-btn");
      if (i === currentPage) pageBtn.classList.add("active");
      pageBtn.textContent = i + 1;
      pageBtn.addEventListener("click", () => fetchAndRenderProducts(i));
      paginationElement.appendChild(pageBtn);
    }

    // Add next page button
    const nextBtn = document.createElement("button");
    nextBtn.classList.add("pagination-btn", "next-btn");
    nextBtn.innerHTML = "Next &raquo;";
    nextBtn.disabled = currentPage >= totalPages - 1;
    nextBtn.addEventListener("click", () =>
      fetchAndRenderProducts(currentPage + 1)
    );
    paginationElement.appendChild(nextBtn);
  }

  // Initialize Add to Cart buttons functionality
  function initAddToCartButtons() {
    // Initialize wishlist toggle buttons
    initWishlistButtons();

    const addToCartBtns = document.querySelectorAll(".add-to-cart-btn");
    addToCartBtns.forEach((btn) => {
      if (!btn.classList.contains("out-of-stock")) {
        btn.addEventListener("click", function () {
          const productId = this.getAttribute("data-product-id");
          if (!productId) {
            console.error("No product ID found on button:", this);
            return;
          }

          // Add product to cart using AJAX
          fetch("/cart/add", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "X-CSRF-TOKEN": document
                .querySelector('meta[name="_csrf"]')
                ?.getAttribute("content"),
            },
            body: JSON.stringify({ productId: productId, quantity: 1 }),
          })
            .then((response) => {
              if (!response.ok) {
                throw new Error("Network response was not ok");
              }
              return response.json();
            })
            .then((data) => {
              if (data.redirect) {
                window.location.href = data.redirect;
                return;
              }

              // Animation and UI update
              btn.classList.add("added");
              setTimeout(() => {
                btn.classList.remove("added");
                btn.innerHTML =
                  '<ion-icon name="checkmark-outline"></ion-icon> Added to Cart';
                setTimeout(() => {
                  btn.innerHTML =
                    '<ion-icon name="cart-outline"></ion-icon> Add to Cart';
                }, 1500);
              }, 1000);

              // Show success message
              if (window.showToast) {
                window.showToast(
                  "Success",
                  "Product added to cart!",
                  "success"
                );
              }

              // Update cart count using the unified counter system
              if (
                data.cartCount !== undefined &&
                typeof window.updateCartCounter === "function"
              ) {
                window.updateCartCounter(data.cartCount);
              }
            })
            .catch((error) => {
              console.error("Error adding to cart:", error);
              if (window.showToast) {
                window.showToast(
                  "Error",
                  "Failed to add product to cart. Please try again.",
                  "error"
                );
              }
            });
        });
      }
    });
  }

  // Initialize wishlist buttons functionality
  function initWishlistButtons() {
    const wishlistBtns = document.querySelectorAll(".wishlist-btn");
    wishlistBtns.forEach((btn) => {
      btn.addEventListener("click", function () {
        const productId = this.getAttribute("data-product-id");
        if (!productId) {
          console.error("No product ID found on wishlist button:", this);
          return;
        }

        const isActive = this.classList.contains("active");
        const endpoint = isActive ? "/wishlist/remove" : "/wishlist/add";

        // Toggle wishlist using AJAX
        fetch(endpoint, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": document
              .querySelector('meta[name="_csrf"]')
              ?.getAttribute("content"),
          },
          body: JSON.stringify({ productId: productId }),
        })
          .then((response) => {
            if (!response.ok) {
              throw new Error("Network response was not ok");
            }
            return response.json();
          })
          .then((data) => {
            if (data.redirect) {
              window.location.href = data.redirect;
              return;
            }

            // Toggle UI state
            btn.classList.toggle("active");
            if (btn.classList.contains("active")) {
              btn.innerHTML = '<ion-icon name="heart"></ion-icon>';
              if (window.showToast) {
                window.showToast(
                  "Success",
                  "Product added to wishlist!",
                  "success"
                );
              }
            } else {
              btn.innerHTML = '<ion-icon name="heart-outline"></ion-icon>';
              if (window.showToast) {
                window.showToast(
                  "Success",
                  "Product removed from wishlist!",
                  "success"
                );
              }
            }

            // Update wishlist count using the unified counter system
            if (
              data.wishlistCount !== undefined &&
              typeof window.updateWishlistCounter === "function"
            ) {
              window.updateWishlistCounter(data.wishlistCount);
            }
          })
          .catch((error) => {
            console.error("Error updating wishlist:", error);
            if (window.showToast) {
              window.showToast(
                "Error",
                "Failed to update wishlist. Please try again.",
                "error"
              );
            }
          });
      });
    });
  }

  fetchAndRenderProducts();
});
