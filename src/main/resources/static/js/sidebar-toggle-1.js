// Add sidebar toggle functionality for responsive design
document.addEventListener("DOMContentLoaded", function () {
  const sidebarToggle = document.getElementById("sidebar-toggle");
  const sidebar = document.querySelector(".sidebar");
  const main = document.querySelector("main");
  const utilityBar = document.querySelector(".utility-bar");

  if (sidebarToggle && sidebar && main) {
    // Function to handle sidebar toggle
    const toggleSidebar = () => {
      sidebar.classList.toggle("sidebar-hidden");
      main.classList.toggle("main-expanded");

      // Update utility bar width
      if (utilityBar) {
        if (main.classList.contains("main-expanded")) {
          utilityBar.style.width = "calc(100% - 250px)";
        } else {
          utilityBar.style.width = "100%";
        }
      }
    };

    // Click event for toggle button
    sidebarToggle.addEventListener("click", toggleSidebar);

    // Handle resize events
    const handleResize = () => {
      if (window.innerWidth > 991) {
        sidebar.classList.remove("sidebar-hidden");
        main.classList.remove("main-expanded");
        utilityBar.style.width = "calc(100% - 250px)";
      } else {
        utilityBar.style.width = "100%";
      }
    };

    // Set initial state
    handleResize();

    // Listen for window resize
    window.addEventListener("resize", handleResize);
  }
});
