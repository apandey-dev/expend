/**
 * Expend V1 Showcase Website Client Script
 * Handles screenshot carousel, caption synchronization, and smooth navigation
 */

document.addEventListener('DOMContentLoaded', () => {
  const slides = document.querySelectorAll('.carousel-slide');
  const dots = document.querySelectorAll('.carousel-dot');
  const caption = document.getElementById('carousel-caption');

  const captions = [
    'AMOLED Night — Total Balance banner & recent transactions.',
    'Credit Card accounting with live outstanding debt & limit.',
    'Subtle pastel badges for clean visual category identification.',
    '40 built-in vector icons with instant selection sheet.',
    'Full-screen transaction editing with date & time pickers.'
  ];

  let currentIndex = 0;
  let autoplayTimer = null;
  const AUTO_PLAY_INTERVAL = 4500;

  function setSlide(index) {
    if (index < 0) index = slides.length - 1;
    if (index >= slides.length) index = 0;

    currentIndex = index;

    slides.forEach((slide, i) => {
      slide.classList.toggle('active', i === currentIndex);
    });

    dots.forEach((dot, i) => {
      dot.classList.toggle('active', i === currentIndex);
      dot.setAttribute('aria-selected', i === currentIndex ? 'true' : 'false');
    });

    if (caption && captions[currentIndex]) {
      caption.textContent = captions[currentIndex];
    }
  }

  function startAutoplay() {
    stopAutoplay();
    autoplayTimer = setInterval(() => {
      setSlide(currentIndex + 1);
    }, AUTO_PLAY_INTERVAL);
  }

  function stopAutoplay() {
    if (autoplayTimer) {
      clearInterval(autoplayTimer);
      autoplayTimer = null;
    }
  }

  dots.forEach((dot) => {
    dot.addEventListener('click', (e) => {
      const targetIndex = parseInt(e.currentTarget.getAttribute('data-index'), 10);
      setSlide(targetIndex);
      startAutoplay();
    });
  });

  const mockup = document.querySelector('.phone-mockup');
  if (mockup) {
    mockup.addEventListener('mouseenter', stopAutoplay);
    mockup.addEventListener('mouseleave', startAutoplay);
    mockup.addEventListener('click', () => {
      setSlide(currentIndex + 1);
      startAutoplay();
    });
  }

  // Keyboard navigation
  document.addEventListener('keydown', (e) => {
    if (e.key === 'ArrowRight') {
      setSlide(currentIndex + 1);
      startAutoplay();
    } else if (e.key === 'ArrowLeft') {
      setSlide(currentIndex - 1);
      startAutoplay();
    }
  });

  // Initialize
  setSlide(0);
  startAutoplay();
});
