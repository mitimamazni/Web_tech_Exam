# E-Commerce Application

A modern, full-featured e-commerce platform built with Spring Boot and Thymelea### Application Structure

#### Key Directories

- `/src/main/java/com/ecommerce/app` - Java source files
- `/src/main/resources/templates` - Thymeleaf templates
- `/src/main/resources/static` - Static web resources
  - `/js` - JavaScript modules
  - `/css` - Stylesheets
  - `/images` - Static images
- `/src/main/resources/application.properties` - Primary configuration, featuring client-side enhancements and hybrid server/browser storage for cart and wishlist management.

## Project Overview

This application is a comprehensive e-commerce solution that provides:

- Product catalog with advanced filtering and search
- Shopping cart with real-time updates
- Hybrid wishlist system (works with or without login)
- User authentication and profile management
- Order processing and tracking
- Admin dashboard for catalog and order management
- Responsive design with modern UI components

### Key Features

1. **Product Management**

   - Category-based organization
   - Support for sale prices
   - Multiple product images with primary image designation
   - Product tags for improved searchability
   - Stock management
   - Support for subscription-based products

2. **Shopping Experience**

   - Persistent shopping cart
   - Real-time cart updates without page refresh
   - Mini cart preview
   - Quantity updates with stock validation
   - Price tracking at item addition

3. **Wishlist System**

   - Browser-based storage for anonymous users
   - Server synchronization for logged-in users
   - Real-time wishlist counter updates
   - Easy product information retrieval
   - Synchronization between devices

4. **Order Management**
   - Multi-step checkout process
   - Address management
   - Order history
   - Order status tracking

## Technology Stack

- **Backend**: Spring Boot 3.1.5, Java 17
- **Frontend**: Thymeleaf, HTML, CSS, JavaScript
- **Database**: MySQL
- **Build Tool**: Maven
- **Other Tools**: Lombok for boilerplate reduction

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- MySQL 8.0 or higher

## Getting Started

### Configuration

The application uses a centralized configuration system that supports environment variables. You can set up the configuration in several ways:

1. **Environment Variables**: Set the variables in your system or deployment environment
2. **`.env` File**: Copy the `.env.example` file to `.env` and customize the values
3. **Application Properties**: Modify the `application.properties` file directly (not recommended for production)

Key configuration options:

```
# Database
DB_URL=jdbc:mysql://localhost:3306/ecommercedb
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Security
JWT_SECRET=your_jwt_secret
DEBUG_MODE=false  # Important: set to false in production!
```

See the complete list of available configuration options in the `.env.example` file.

### Database Setup

The application will automatically create the database tables on startup. Sample data will be loaded if available.

### Running the Application

You can run the application using Maven:

```bash
mvn spring-boot:run
```

Or using the provided VS Code task:

1. Open the Command Palette (Ctrl+Shift+P)
2. Select "Tasks: Run Task"
3. Choose "Run Spring Boot Application"

Once started, the application will be available at `http://localhost:8080`

## Application Structure

### Project Structure

#### Core Packages

- `com.ecommerce.app.controller` - Web controllers for MVC endpoints
- `com.ecommerce.app.controller.api` - REST API controllers
- `com.ecommerce.app.model` - Domain entities and relationships
- `com.ecommerce.app.repository` - Data access interfaces
- `com.ecommerce.app.service` - Business logic interfaces
- `com.ecommerce.app.service.impl` - Service implementations
- `com.ecommerce.app.dto` - Data Transfer Objects
- `com.ecommerce.app.config` - Application configuration
- `com.ecommerce.app.util` - Utility classes

#### Domain Model

Key entities and their relationships:

1. **Product**

   - Properties: name, description, price, salePrice, stockQuantity
   - Relationships:
     - ManyToOne with Category
     - OneToMany with ProductImage
     - ManyToMany with Tag
     - OneToMany with Review

2. **Cart & CartItem**

   - Cart has OneToMany CartItems
   - CartItem tracks product price at time of addition
   - Supports multiple active carts per user

3. **Wishlist & WishlistItem**

   - OneToOne relationship with User
   - WishlistItem has unique constraint on product/wishlist combination
   - Tracks item addition time

4. **Order & OrderItem**

   - Order captures shipping/billing addresses
   - OrderItem preserves product price at purchase time
   - Supports multiple payment methods

5. **User**
   - Manages authentication without Spring Security
   - Relationships:
     - OneToMany with Order
     - OneToMany with Cart
     - OneToOne with Wishlist
     - OneToMany with Review

### Configuration System

The application uses a unified configuration system that works across both server and client:

#### Server-Side Configuration

Server-side configuration is managed through:

- Environment variables
- `application.properties`
- `AppEnvironment.java` (centralized configuration class)

#### Client-Side Configuration

Frontend configuration is managed through:

- `app-config.js` - Central JavaScript configuration
- Server-injected configuration via `ConfigurationExporter`

### REST API Endpoints

#### Cart API (`/api/cart`)

- GET `/api/cart` - Get cart contents
- GET `/api/cart/count` - Get item count
- POST `/api/cart/add` - Add item
- POST `/api/cart/update` - Update item quantity
- DELETE `/api/cart/remove` - Remove item
- DELETE `/api/cart/clear` - Clear cart

#### Wishlist API (`/api/wishlist`)

- GET `/api/wishlist` - Get wishlist
- GET `/api/wishlist/count` - Get item count
- POST `/api/wishlist/add` - Add item
- DELETE `/api/wishlist/remove` - Remove item
- DELETE `/api/wishlist/clear` - Clear wishlist
- POST `/api/wishlist/sync` - Sync browser wishlist

#### Product API (`/api/products`)

- GET `/api/products` - Get products (paginated)
- GET `/api/products/all` - Get all products
- GET `/api/products/{id}` - Get product details
- GET `/api/products/category/{categoryId}` - Get products by category

### JavaScript Integration

The frontend uses dedicated modules for cart and wishlist management:

1. **Cart Management**

   - Real-time counter updates
   - Persistent storage
   - Price calculation
   - Quantity validation

2. **Wishlist Management**
   - Browser localStorage for anonymous users
   - Server sync for authenticated users
   - Counter management
   - Product data caching

## Authentication System

The application uses a custom authentication system implemented through session management. This replaces the previously used Spring Security.

Key authentication components:

- `AuthInterceptor` - Intercepts requests to verify authentication status
- `RequiresAuth` annotation - Marks controllers requiring authentication
- `AuthHelper` - Manages session-based authentication

## Hardcoded Data and Configuration

The following files contain hardcoded data that should be moved to the database or configuration:

1. `src/main/resources/static/js/home-product-meta.js` - Contains hardcoded product metadata
2. `src/main/resources/static/js/modal.js` and `modal-unified.js` - Contains configuration values
3. `src/main/java/com/ecommerce/app/util/TemplateDataExtractor.java` - Contains test/mock data
4. `src/main/java/com/ecommerce/app/controller/AdminController.java.bak` - Contains test data

## Security Considerations

### Authentication & Authorization

1. Uses session-based authentication without Spring Security
2. Admin access controlled via session attributes
3. API endpoints check authentication status
4. CSRF protection implemented for forms
5. Session timeout configured via `app.security.session.timeout`

### Data Protection

1. Database credentials must be provided via environment variables
2. Sensitive data excluded from client-side configuration export
3. Price consistency maintained across cart/order workflow
4. Input validation implemented on both client and server

### Production Setup

1. Debug mode must be disabled (`app.debug.enabled=false`)
2. Rate limiting available for API endpoints
3. File upload restrictions enforced
4. CORS configurations must be reviewed
5. Error handling prevents information leakage

## Contributing

### Development Process

1. Create a feature branch from `main`
2. Make your changes
3. Submit a pull request

### Code Style

This project follows standard Java code style conventions.

## Documentation

Additional documentation:

- [Hardcoded Data Remediation](./hardcoded-data-remediation.md) - Details on the refactoring to remove hardcoded values
- [Security Changes](./security-changes-README.md) - Information about security-related changes

## License

This project is licensed under the MIT License - see the LICENSE file for details.
