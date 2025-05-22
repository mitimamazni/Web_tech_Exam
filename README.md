deployment link : http://138.197.91.58/
🪑 FurniFind – Modern Furniture E-Commerce Platform

FurniFind is a full-featured e-commerce web application designed for discovering and purchasing stylish furniture online. Built with Spring Boot and Thymeleaf, it offers a seamless shopping experience for customers and robust management tools for administrators.
🌐 Live Demo: http://138.197.91.58
#Admin Credentials
Username: admin
Password: 1234

📦 Features
🛍️ Customer Experience
Product Catalog: Browse a wide range of furniture items with detailed descriptions and images.

Advanced Filtering: Search and filter products by category, price, and other attributes.

Shopping Cart: Add items to a persistent cart with real-time updates.

Wishlist: Save favorite products for future reference.

User Accounts: Register and log in to manage orders and personal information.

Order Tracking: View order history and track current orders.

🛠️ Admin Dashboard
Product Management: Add, edit, and delete products, including images and descriptions.

Order Management: View and update the status of customer orders.

User Management: Manage customer accounts and permissions.

🧰 Technology Stack
Backend: Spring Boot 3.1.5, Java 17

Frontend: Thymeleaf, HTML, CSS, JavaScript

Database: MySQL

Build Tool: Maven

Security: Spring Security for authentication and authorization

Others: Lombok for reducing boilerplate code



⚙️ Installation & Setup
Prerequisites
Java 17 or higher

Maven 3.6.3 or higher

MySQL 8.0 or higher

Configuration
Set up environment variables or configure the application.properties file:
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/furnifind_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Security
JWT_SECRET=your_jwt_secret
DEBUG_MODE=false


Running the Application
Use Maven to run the application:

mvn spring-boot:run
Access the application at http://localhost:8080



🗂️ Project Structure
 src/
├── main/
│   ├── java/com/furnifind/
│   │   ├── controller/       # MVC and REST controllers
│   │   ├── model/            # Domain entities
│   │   ├── repository/       # Data access interfaces
│   │   ├── service/          # Business logic
│   │   └── config/           # Application configuration
│   └── resources/
│       ├── templates/        # Thymeleaf templates
│       ├── static/           # Static resources (CSS, JS, images)
│       └── application.properties


🔐 Security & Authentication
Authentication: Implemented using Spring Security with session-based authentication.

Authorization: Role-based access control for USER and ADMIN roles.

Password Management: Secure password storage using BCrypt.

CSRF Protection: Enabled for all forms.

🛒 REST API Endpoints
Cart API (/api/cart)
GET /api/cart – Retrieve cart contents

POST /api/cart/add – Add item to cart

POST /api/cart/update – Update item quantity

DELETE /api/cart/remove – Remove item from cart

DELETE /api/cart/clear – Clear the cart

Wishlist API (/api/wishlist)
GET /api/wishlist – Retrieve wishlist items

POST /api/wishlist/add – Add item to wishlist

DELETE /api/wishlist/remove – Remove item from wishlist

DELETE /api/wishlist/clear – Clear the wishlist

Product API (/api/products)
GET /api/products – Retrieve paginated list of products

GET /api/products/{id} – Retrieve product details

GET /api/products/category/{categoryId} – Retrieve products by category

🚀 Deployment
The application is deployed on a cloud platform and can be accessed at:

👉 http://138.197.91.58

Ensure the following for production:

DEBUG_MODE is set to false

Proper CORS configurations are in place

Secure handling of environment variables

🧪 Testing
Unit Tests: Implemented for service and repository layers.

Integration Tests: Cover critical workflows like user registration and order processing.

Frontend Testing: Basic validation for forms and user interactions.

📝 Documentation
API Documentation: Detailed API specifications are available in the docs/ directory.

User Guide: Instructions for using the application are provided for both customers and administrators.


📄 License
This project is licensed under the MIT License – see the LICENSE file for details.
