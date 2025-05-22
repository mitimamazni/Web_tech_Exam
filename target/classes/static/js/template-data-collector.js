/**
 * Template Data Collector Utility
 *
 * This script helps collect data from all templates in the ecommerce application
 * and prepares it for insertion into the database.
 */

// Configuration
const API_BASE_URL = "/api/template-data";
const OUTPUT_FOLDER = "./database-inserts";

// Main function to collect all template data
async function collectAllTemplateData() {
  try {
    console.log("Starting template data collection...");

    // 1. Fetch all data from the API
    const response = await fetch(`${API_BASE_URL}/all`);
    if (!response.ok) {
      throw new Error(`API error: ${response.status} ${response.statusText}`);
    }

    const result = await response.json();
    if (!result.success) {
      throw new Error(`API returned error: ${result.message}`);
    }

    const data = result.data;
    console.log(`Successfully collected data from templates`);

    // 2. Process and format the data for insertion
    const formattedData = formatDataForInsertion(data);

    // 3. Export the SQL statements
    await exportSqlStatements();

    console.log("Template data collection completed successfully");
    return formattedData;
  } catch (error) {
    console.error("Error collecting template data:", error);
    throw error;
  }
}

// Process and format data for database insertion
function formatDataForInsertion(data) {
  const formatted = {
    products: {},
    categories: {},
    users: {},
    orders: {},
  };

  // Format products
  if (data.products && Array.isArray(data.products)) {
    data.products.forEach((product) => {
      formatted.products[product.id] = {
        id: product.id,
        name: product.name,
        description: product.description,
        price: product.price,
        salePrice: product.salePrice,
        stockQuantity: product.stockQuantity,
        isSubscription: product.isSubscription,
        categoryId: product.category ? product.category.id : null,
        active: product.active,
        images: product.images.map((img) => ({
          id: img.id,
          imageUrl: img.imageUrl,
          isPrimary: img.isPrimary,
          displayOrder: img.displayOrder,
        })),
        averageRating: product.averageRating,
      };
    });
  }

  // Format categories
  if (data.categories && Array.isArray(data.categories)) {
    data.categories.forEach((category) => {
      formatted.categories[category.id] = {
        id: category.id,
        name: category.name,
        description: category.description,
        productCount: category.productCount,
      };
    });
  }

  // Format users (excluding sensitive data)
  if (data.users && Array.isArray(data.users)) {
    data.users.forEach((user) => {
      formatted.users[user.id] = {
        id: user.id,
        firstName: user.firstName,
        lastName: user.lastName,
        username: user.username,
        email: user.email,
        roles: user.roles,
        addresses: user.addresses.map((addr) => ({
          id: addr.id,
          streetAddress: addr.streetAddress,
          city: addr.city,
          state: addr.state,
          postalCode: addr.postalCode,
          country: addr.country,
          addressType: addr.addressType,
          isDefault: addr.isDefault,
        })),
      };
    });
  }

  // Format orders
  if (data.orders && Array.isArray(data.orders)) {
    data.orders.forEach((order) => {
      formatted.orders[order.id] = {
        id: order.id,
        orderNumber: order.orderNumber,
        userId: order.userId,
        totalAmount: order.totalAmount,
        status: order.status,
        shippingAddress: order.shippingAddress,
        billingAddress: order.billingAddress,
        paymentMethod: order.paymentMethod,
        createdAt: order.createdAt,
        items: order.items.map((item) => ({
          productId: item.productId,
          productName: item.productName,
          quantity: item.quantity,
          price: item.price,
          totalPrice: item.totalPrice,
        })),
      };
    });
  }

  return formatted;
}

// Export data as SQL statements
async function exportSqlStatements() {
  try {
    console.log("Generating SQL export...");

    const response = await fetch(`${API_BASE_URL}/sql-export`);
    if (!response.ok) {
      throw new Error(`API error: ${response.status} ${response.statusText}`);
    }

    const result = await response.json();
    if (!result.success) {
      throw new Error(`API returned error: ${result.message}`);
    }

    const sqlStatements = result.data;

    // In a real application, this would save files to disk
    // Since we're in a browser environment, we'll log the statements
    console.log("SQL Export Generated:");

    for (const [tableName, sql] of Object.entries(sqlStatements)) {
      console.log(`\n--- ${tableName.toUpperCase()} SQL ---`);
      console.log(sql);
    }

    return sqlStatements;
  } catch (error) {
    console.error("Error exporting SQL statements:", error);
    throw error;
  }
}

// Execute specific data collection functions
const TemplateDataCollector = {
  collectAllData: collectAllTemplateData,

  collectProducts: async function () {
    const response = await fetch(`${API_BASE_URL}/products`);
    const result = await response.json();
    return result.success ? result.data : [];
  },

  collectCategories: async function () {
    const response = await fetch(`${API_BASE_URL}/categories`);
    const result = await response.json();
    return result.success ? result.data : [];
  },

  collectUsers: async function () {
    const response = await fetch(`${API_BASE_URL}/users`);
    const result = await response.json();
    return result.success ? result.data : [];
  },

  collectOrders: async function () {
    const response = await fetch(`${API_BASE_URL}/orders`);
    const result = await response.json();
    return result.success ? result.data : [];
  },

  collectCart: async function (userId) {
    const response = await fetch(`${API_BASE_URL}/carts/${userId}`);
    const result = await response.json();
    return result.success ? result.data : null;
  },

  collectWishlist: async function (userId) {
    const response = await fetch(`${API_BASE_URL}/wishlists/${userId}`);
    const result = await response.json();
    return result.success ? result.data : null;
  },

  exportSql: exportSqlStatements,
};

// Export the collector for use in other modules
export default TemplateDataCollector;
