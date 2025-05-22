/**
 * FurniFind Product Categories
 * This file contains structured data for furniture product categories that can be used
 * to populate category modals dynamically.
 */

// Add furniture categories to the bookbyteCategories object
document.addEventListener("DOMContentLoaded", function () {
  // Wait for bookbyteCategories to be defined
  if (typeof bookbyteCategories !== "undefined") {
    // Add furniture categories
    bookbyteCategories["bedroom-furniture"] = {
      title: "Bedroom Furniture",
      items: [
        {
          id: 301,
          title: "King Size Upholstered Bed with Storage",
          price: 599.99,
          originalPrice: 699.99,
          coverImage:
            "https://images.unsplash.com/photo-1505693314120-0d443867891c?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1513694203232-719a280e022f?q=80&w=1469&auto=format&fit=crop",
          category: "Bedroom",
          rating: 4.5,
          inStock: true,
          isNew: true,
          badge: "New",
          description:
            "Elegant king size bed with upholstered headboard and convenient storage drawers.",
        },
        {
          id: 302,
          title: "Modern Bedside Table with Drawers",
          price: 129.99,
          originalPrice: 159.99,
          coverImage:
            "https://images.unsplash.com/photo-1618220179428-22790b461013?q=80&w=1527&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?q=80&w=1470&auto=format&fit=crop",
          category: "Bedroom",
          rating: 4.0,
          inStock: true,
          badge: "Sale",
          description:
            "Sleek bedside table with two spacious drawers for storage and organization.",
        },
        {
          id: 303,
          title: "Queen Size Memory Foam Mattress",
          price: 449.99,
          originalPrice: 599.99,
          coverImage:
            "https://images.unsplash.com/photo-1634646803845-c25be01623e9?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1634646751066-6ecdef6dacc1?q=80&w=1470&auto=format&fit=crop",
          category: "Bedroom",
          rating: 4.8,
          inStock: true,
          isNew: false,
          badge: "25% Off",
          description:
            "Premium queen size memory foam mattress with cooling technology for ultimate comfort.",
        },
        {
          id: 304,
          title: "Mirrored Dresser with 6 Drawers",
          price: 349.99,
          originalPrice: 399.99,
          coverImage:
            "https://images.unsplash.com/photo-1595428774223-ef52624120d2?q=80&w=1374&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1594026112284-02bb6f3352fe?q=80&w=1374&auto=format&fit=crop",
          category: "Bedroom",
          rating: 4.3,
          inStock: true,
          isNew: false,
          badge: null,
          description:
            "Elegant mirrored dresser with 6 spacious drawers for all your storage needs.",
        },
        {
          id: 305,
          title: "Bedroom Vanity Set with Stool",
          price: 219.99,
          originalPrice: 259.99,
          coverImage:
            "https://images.unsplash.com/photo-1613545325278-f24b0cae1224?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1616627434852-258b2f401945?q=80&w=1470&auto=format&fit=crop",
          category: "Bedroom",
          rating: 4.2,
          inStock: true,
          isNew: false,
          badge: "Hot Deal",
          description:
            "Modern vanity set with mirror, table, and comfortable padded stool.",
        },
        {
          id: 306,
          title: "Full Size Bunk Bed with Ladder",
          price: 399.99,
          originalPrice: 499.99,
          coverImage:
            "https://images.unsplash.com/photo-1595333785094-830064506c56?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1650349827016-788cab98149e?q=80&w=1480&auto=format&fit=crop",
          category: "Bedroom",
          rating: 4.6,
          inStock: true,
          isNew: true,
          badge: "New",
          description:
            "Space-saving full size bunk bed with sturdy ladder and safety rails.",
        },
      ],
    };

    bookbyteCategories["home-office"] = {
      title: "Home Office Furniture",
      items: [
        {
          id: 401,
          title: "L-Shaped Computer Desk with Storage",
          price: 199.99,
          originalPrice: 259.99,
          coverImage:
            "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?q=80&w=1469&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1593062096033-9a26b09da705?q=80&w=1470&auto=format&fit=crop",
          category: "Home Office",
          rating: 5.0,
          inStock: true,
          badge: "25%",
          description:
            "Spacious L-shaped desk with built-in storage cabinets and ample workspace.",
        },
        {
          id: 402,
          title: "Premium Ergonomic Office Chair",
          price: 249.99,
          originalPrice: 299.99,
          coverImage:
            "https://images.unsplash.com/photo-1596162954151-cdcb4c0f70fb?q=80&w=1374&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1580480055273-228ff5388ef8?q=80&w=1374&auto=format&fit=crop",
          category: "Home Office",
          rating: 4.5,
          inStock: true,
          badge: "Best Seller",
          description:
            "High-quality ergonomic office chair with adjustable lumbar support and breathable mesh back.",
        },
        {
          id: 403,
          title: "Adjustable Standing Desk",
          price: 329.99,
          originalPrice: 399.99,
          coverImage:
            "https://images.unsplash.com/photo-1595500381751-d837f87d3f14?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1619642992330-32ac10509fd6?q=80&w=1374&auto=format&fit=crop",
          category: "Home Office",
          rating: 4.9,
          inStock: true,
          isNew: true,
          badge: "New",
          description:
            "Electric height-adjustable standing desk for ergonomic comfort throughout your workday.",
        },
        {
          id: 404,
          title: "Bookshelf with 5 Shelves",
          price: 149.99,
          originalPrice: 179.99,
          coverImage:
            "https://images.unsplash.com/photo-1588639768595-ecaf661a038f?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1594026112284-02bb6f3352fe?q=80&w=1374&auto=format&fit=crop",
          category: "Home Office",
          rating: 4.3,
          inStock: true,
          isNew: false,
          badge: null,
          description:
            "Modern 5-shelf bookcase perfect for organizing books and displaying decorative items.",
        },
        {
          id: 405,
          title: "Filing Cabinet with Lock",
          price: 119.99,
          originalPrice: 149.99,
          coverImage:
            "https://images.unsplash.com/photo-1607275121066-5b3dcb04b775?q=80&w=1374&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1524741131464-62306a23bb12?q=80&w=1374&auto=format&fit=crop",
          category: "Home Office",
          rating: 4.4,
          inStock: true,
          isNew: false,
          badge: "20% Off",
          description:
            "Secure filing cabinet with lock, featuring three drawers for document storage.",
        },
        {
          id: 406,
          title: "Desk Lamp with Wireless Charging",
          price: 79.99,
          originalPrice: 99.99,
          coverImage:
            "https://images.unsplash.com/photo-1585913161203-695a5800ac38?q=80&w=1480&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1570991402126-31e6ffef882f?q=80&w=1374&auto=format&fit=crop",
          category: "Home Office",
          rating: 4.7,
          inStock: true,
          isNew: true,
          badge: "Hot Deal",
          description:
            "Modern LED desk lamp with adjustable brightness and integrated wireless charging pad.",
        },
      ],
    };

    bookbyteCategories["living-room"] = {
      title: "Living Room Furniture",
      items: [
        {
          id: 501,
          title: "Modern L-Shaped Sectional Sofa",
          price: 799.99,
          originalPrice: 999.99,
          coverImage:
            "https://images.unsplash.com/photo-1550581190-9c1c48d21d6c?q=80&w=1469&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?q=80&w=1470&auto=format&fit=crop",
          category: "Living Room",
          rating: 4.0,
          inStock: true,
          badge: "Trending",
          description:
            "Contemporary L-shaped sectional sofa with plush cushions and durable upholstery.",
        },
        {
          id: 502,
          title: "Wall-Mounted Entertainment Center",
          price: 349.99,
          originalPrice: 449.99,
          coverImage:
            "https://images.unsplash.com/photo-1585412727339-54e4bae3bbf9?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1616046386594-c152babc9e15?q=80&w=1480&auto=format&fit=crop",
          category: "Living Room",
          rating: 5.0,
          inStock: true,
          badge: "Hot Deal",
          description:
            "Space-saving wall-mounted entertainment center with modern floating design.",
        },
        {
          id: 503,
          title: "Mid-Century Modern Coffee Table",
          price: 189.99,
          originalPrice: 249.99,
          coverImage:
            "https://images.unsplash.com/photo-1532372320572-cda25653a694?q=80&w=1374&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1484101403633-562f891dc89a?q=80&w=1374&auto=format&fit=crop",
          category: "Living Room",
          rating: 4.6,
          inStock: true,
          isNew: false,
          badge: "24% Off",
          description:
            "Elegant mid-century modern coffee table with solid wood legs and tempered glass top.",
        },
        {
          id: 504,
          title: "Accent Chair with Ottoman",
          price: 279.99,
          originalPrice: 329.99,
          coverImage:
            "https://images.unsplash.com/photo-1567538096621-38d2284b23ff?q=80&w=1374&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=1558&auto=format&fit=crop",
          category: "Living Room",
          rating: 4.3,
          inStock: true,
          isNew: true,
          badge: "New",
          description:
            "Comfortable accent chair with matching ottoman, perfect for reading corners.",
        },
        {
          id: 505,
          title: "Modern Area Rug 5x8",
          price: 149.99,
          originalPrice: 199.99,
          coverImage:
            "https://images.unsplash.com/photo-1600166898405-da9535204843?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1578500351865-ea7926cb32eb?q=80&w=1376&auto=format&fit=crop",
          category: "Living Room",
          rating: 4.4,
          inStock: true,
          isNew: false,
          badge: "25% Off",
          description:
            "Stylish 5x8 area rug with contemporary geometric pattern and soft texture.",
        },
        {
          id: 506,
          title: "Floor Lamp with Adjustable Head",
          price: 89.99,
          originalPrice: 119.99,
          coverImage:
            "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?q=80&w=1374&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1513506806656-8c6462a5b2e1?q=80&w=1374&auto=format&fit=crop",
          category: "Living Room",
          rating: 4.2,
          inStock: true,
          isNew: false,
          badge: null,
          description:
            "Modern floor lamp with adjustable head and energy-efficient LED bulb included.",
        },
      ],
    };

    bookbyteCategories["dining-room"] = {
      title: "Dining Room Furniture",
      items: [
        {
          id: 601,
          title: "Extendable Dining Table with Leaf",
          price: 449.99,
          originalPrice: 599.99,
          coverImage:
            "https://images.unsplash.com/photo-1604578762246-41134e37f9cc?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1615876234886-9b2c0c2b0b1e?q=80&w=1374&auto=format&fit=crop",
          category: "Dining Room",
          rating: 4.5,
          inStock: true,
          badge: "New Arrival",
          description:
            "Elegant extendable dining table that seats 4-8 people with removable leaf insert.",
        },
        {
          id: 602,
          title: "Set of 4 Upholstered Dining Chairs",
          price: 329.99,
          originalPrice: 399.99,
          coverImage:
            "https://images.unsplash.com/photo-1601628828688-632f38a5a7d0?q=80&w=1374&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1611486212557-88be5ff6f941?q=80&w=1470&auto=format&fit=crop",
          category: "Dining Room",
          rating: 4.0,
          inStock: true,
          badge: "Best Seller",
          description:
            "Set of 4 comfortable upholstered dining chairs with solid wood legs.",
        },
        {
          id: 603,
          title: "Modern China Cabinet",
          price: 529.99,
          originalPrice: 699.99,
          coverImage:
            "https://images.unsplash.com/photo-1594026112284-02bb6f3352fe?q=80&w=1374&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1631679706909-1844bbd07221?q=80&w=1392&auto=format&fit=crop",
          category: "Dining Room",
          rating: 4.7,
          inStock: true,
          isNew: false,
          badge: "24% Off",
          description:
            "Elegant china cabinet with glass doors for displaying your fine dinnerware collection.",
        },
        {
          id: 604,
          title: "Counter Height Dining Set",
          price: 499.99,
          originalPrice: 599.99,
          coverImage:
            "https://images.unsplash.com/photo-1615874959474-d609969a20ed?q=80&w=1480&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1617806118233-18e1de247200?q=80&w=1632&auto=format&fit=crop",
          category: "Dining Room",
          rating: 4.3,
          inStock: true,
          isNew: true,
          badge: "New",
          description:
            "Modern counter height dining set with table and 4 stools, perfect for smaller spaces.",
        },
        {
          id: 605,
          title: "Buffet Sideboard with Wine Rack",
          price: 379.99,
          originalPrice: 459.99,
          coverImage:
            "https://images.unsplash.com/photo-1595428774223-ef52624120d2?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?q=80&w=1632&auto=format&fit=crop",
          category: "Dining Room",
          rating: 4.6,
          inStock: true,
          isNew: false,
          badge: "17% Off",
          description:
            "Multifunctional buffet sideboard with wine rack, cabinets and drawers for dining storage.",
        },
        {
          id: 606,
          title: "Pendant Light Fixture for Dining",
          price: 129.99,
          originalPrice: 169.99,
          coverImage:
            "https://images.unsplash.com/photo-1565814329452-e1efa11c5b89?q=80&w=1470&auto=format&fit=crop",
          hoverImage:
            "https://images.unsplash.com/photo-1540932239986-30128078f3c5?q=80&w=1374&auto=format&fit=crop",
          category: "Dining Room",
          rating: 4.4,
          inStock: true,
          isNew: false,
          badge: null,
          description:
            "Modern pendant light fixture, perfect for illuminating dining tables with style.",
        },
      ],
    };

    console.log("✅ Furniture categories added to bookbyteCategories");
  } else {
    console.error("❌ bookbyteCategories is not defined yet");
  }
});
