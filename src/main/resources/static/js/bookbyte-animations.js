// BookByte Enhanced Testimonials & Animations
document.addEventListener("DOMContentLoaded", function () {
  // Add hover effects to testimonial cards
  const testimonialCards = document.querySelectorAll(".testimonial-card");
  testimonialCards.forEach((card) => {
    card.addEventListener("mouseenter", function () {
      this.style.transform = "translateY(-8px)";
      this.style.boxShadow = "0 12px 30px rgba(0, 0, 0, 0.1)";
    });

    card.addEventListener("mouseleave", function () {
      this.style.transform = "translateY(0)";
      this.style.boxShadow = "0 4px 15px rgba(0, 0, 0, 0.05)";
    });
  });

  // Add hover effects to category items
  const categoryItems = document.querySelectorAll(".category-item");
  categoryItems.forEach((item) => {
    item.addEventListener("mouseenter", function () {
      this.style.transform = "translateY(-5px)";
      this.style.boxShadow = "0 10px 25px rgba(0, 0, 0, 0.1)";
    });

    item.addEventListener("mouseleave", function () {
      this.style.transform = "translateY(0)";
      this.style.boxShadow = "0 2px 8px rgba(0, 0, 0, 0.08)";
    });
  });

  // Enhanced banner slider effects
  const sliderItems = document.querySelectorAll(".slider-item");
  sliderItems.forEach((item) => {
    const bannerImg = item.querySelector(".banner-img");
    const bannerContent = item.querySelector(".banner-content");

    item.addEventListener("mouseenter", function () {
      if (bannerImg) bannerImg.style.transform = "scale(1.02)";
      if (bannerContent)
        bannerContent.style.backgroundColor = "rgba(42, 67, 101, 0.85)";
    });

    item.addEventListener("mouseleave", function () {
      if (bannerImg) bannerImg.style.transform = "scale(1)";
      if (bannerContent) bannerContent.style.backgroundColor = "";
    });
  });

  // Service item animations
  const serviceItems = document.querySelectorAll(".service-item");
  serviceItems.forEach((item, index) => {
    // Add a staggered animation delay
    setTimeout(() => {
      item.style.opacity = "1";
      item.style.transform = "translateY(0)";
    }, 100 * index);

    // Add hover effects
    item.addEventListener("mouseenter", function () {
      this.style.transform = "translateY(-5px)";
      const icon = this.querySelector(".service-icon");
      if (icon) {
        icon.style.backgroundColor = "var(--bookbyte-accent)";
        icon.style.color = "white";
      }
    });

    item.addEventListener("mouseleave", function () {
      this.style.transform = "translateY(0)";
      const icon = this.querySelector(".service-icon");
      if (icon) {
        icon.style.backgroundColor = "";
        icon.style.color = "";
      }
    });
  });

  // Make the quote section more dynamic
  const quoteSection = document.querySelector(".quote-section");
  if (quoteSection) {
    window.addEventListener("scroll", function () {
      const rect = quoteSection.getBoundingClientRect();
      const windowHeight = window.innerHeight;

      // When the quote section comes into view
      if (rect.top < windowHeight && rect.bottom >= 0) {
        const scrollPosition = rect.top / windowHeight;
        const opacity = 1 - Math.abs(scrollPosition * 0.7);
        quoteSection.style.opacity = Math.max(0.2, Math.min(1, opacity));
      }
    });
  }
});
