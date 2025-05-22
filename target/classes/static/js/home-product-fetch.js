// src/main/resources/static/js/home-product-fetch.js
// Dynamically fetch and render home page products using DB data and meta from home-product-meta.js

document.addEventListener("DOMContentLoaded", function () {
  // Wait for meta to be loaded
  if (!window.HOME_PRODUCT_META) {
    console.error("HOME_PRODUCT_META not loaded");
    return;
  }

  // Helper: Render rating stars
  function renderStars(rating) {
    if (!rating) return "";
    const full = Math.floor(rating);
    const half = rating % 1 >= 0.5;
    let html = "";
    for (let i = 0; i < 5; i++) {
      if (i < full) html += '<ion-icon name="star"></ion-icon>';
      else if (i === full && half)
        html += '<ion-icon name="star-half"></ion-icon>';
      else html += '<ion-icon name="star-outline"></ion-icon>';
    }
    return html;
  }

  // Helper: Render badge
  function renderBadge(badge) {
    if (!badge) return "";
    if (badge === "Sale" || badge === "NEW")
      return `<p class="showcase-badge angle black">${badge}</p>`;
    if (badge.endsWith("%")) return `<p class="showcase-badge">${badge}</p>`;
    return `<p class="showcase-badge">${badge}</p>`;
  }

  // Fetch all home product IDs from meta
  const homeProductIds = Object.keys(window.HOME_PRODUCT_META).map(Number);

  // Fetch product details from API (fetch one at a time)
  console.log("Fetching products with IDs:", homeProductIds);

  // Create a promise for each product fetch
  const productPromises = [];

  homeProductIds.forEach((id) => {
    const apiUrl = `/api/products/${id}`;
    console.log(`Fetching product ID: ${id} from ${apiUrl}`);

    const promise = fetch(apiUrl)
      .then((res) => {
        if (!res.ok) {
          console.error(
            `Error fetching product ID ${id}:`,
            res.status,
            res.statusText
          );
          return null;
        }
        return res.json();
      })
      .then((data) => {
        if (!data || !data.success) {
          console.error(`Invalid data for product ID ${id}`);
          return null;
        }
        return data.data; // Return just the product data
      })
      .catch((err) => {
        console.error(`Failed to fetch product ID ${id}:`, err);
        return null;
      });

    productPromises.push(promise);
  });

  // Wait for all product fetches to complete
  Promise.all(productPromises)
    .then((products) => {
      // Filter out any nulls from failed requests
      const validProducts = products.filter((p) => p !== null);
      console.log(
        `Successfully fetched ${validProducts.length} of ${homeProductIds.length} products`
      );

      // Create a response object similar to the original API format
      return {
        success: true,
        data: validProducts,
      };
    })
    .then((json) => {
      console.log("API Response data:", json);
      if (!json.success || !json.data || !json.data.length) return;
      const productsById = {};
      json.data.forEach((p) => {
        productsById[p.id] = p;
      });

      // Render New Arrivals
      const newArrivals = homeProductIds
        .filter((id) => window.HOME_PRODUCT_META[id].section === "new-arrivals")
        .sort(
          (a, b) =>
            window.HOME_PRODUCT_META[a].order -
            window.HOME_PRODUCT_META[b].order
        )
        .map((id) =>
          renderShowcase(productsById[id], window.HOME_PRODUCT_META[id])
        )
        .join("");
      const newArrivalsContainer = document.querySelector(
        ".product-minimal .showcase-wrapper .showcase-container"
      );
      if (newArrivalsContainer) newArrivalsContainer.innerHTML = newArrivals;

      // Render Featured Products
      const featured = homeProductIds
        .filter((id) => window.HOME_PRODUCT_META[id].section === "featured")
        .sort(
          (a, b) =>
            window.HOME_PRODUCT_META[a].order -
            window.HOME_PRODUCT_META[b].order
        )
        .map((id) =>
          renderShowcase(productsById[id], window.HOME_PRODUCT_META[id], true)
        )
        .join("");
      const featuredContainer = document.querySelector(
        ".product-main .product-grid"
      );
      if (featuredContainer) featuredContainer.innerHTML = featured;
    });

  // Render a product showcase card
  function renderShowcase(product, meta, isFeatured) {
    if (!product) return "";
    const badge = renderBadge(meta.badge);
    const rating = renderStars(meta.rating);
    const priceHtml = product.salePrice
      ? `<p class="price">$${product.price.toFixed(
          2
        )}</p><del>$${product.salePrice.toFixed(2)}</del>`
      : `<p class="price">$${product.price.toFixed(2)}</p>`;
    const imgUrl =
      product.imageUrl ||
      (product.images && product.images[0] && product.images[0].imageUrl) ||
      "";
    const category = product.category ? product.category.name : "";
    const productUrl = `/products/${product.id}`;
    return isFeatured
      ? `
      <div class="showcase">
        <div class="showcase-banner">
          <img src="${imgUrl}" alt="${
          product.name
        }" class="product-img default" width="300" />
          <img src="${imgUrl}" alt="${
          product.name
        }" class="product-img hover" width="300" />
          ${badge}
          <div class="showcase-actions">
            <button class="btn-action" onclick="addToWishlist(${
              product.id
            })"><ion-icon name="heart-outline"></ion-icon></button>
            <button class="btn-action" onclick="quickView(${
              product.id
            })"><ion-icon name="eye-outline"></ion-icon></button>
            <button class="btn-action" data-cart-action="add" data-product-id="${
              product.id
            }"><ion-icon name="bag-add-outline"></ion-icon></button>
          </div>
        </div>
        <div class="showcase-content">
          <a href="#${
            product.category ? product.category.id : ""
          }" class="showcase-category">${category}</a>
          <a href="${productUrl}"><h3 class="showcase-title">${
          product.name
        }</h3></a>
          <div class="showcase-rating">${rating}</div>
          <div class="price-box">${priceHtml}</div>
        </div>
      </div>
    `
      : `
      <div class="showcase">
        <a href="${productUrl}" class="showcase-img-box">
          <img src="${imgUrl}" alt="${
          product.name
        }" class="showcase-img" width="70" />
        </a>
        <div class="showcase-content">
          <a href="${productUrl}"><h4 class="showcase-title">${
          product.name
        }</h4></a>
          <a href="#${
            product.category ? product.category.id : ""
          }" class="showcase-category">${category}</a>
          <div class="price-box">${priceHtml}</div>
        </div>
      </div>
    `;
  }
});
