// Header JavaScript File

document.addEventListener('DOMContentLoaded', function() {
    
    // Variables
    let mobileMenu = false;
    let showCategory = false;
    let modalSearch = false;
    let selectedIndex = -1;
    let listNameProduct = [];
    let allProducts = []; // Lấy từ server hoặc định nghĩa

    // DOM Elements
    const searchInput = document.getElementById('search-input');
    const searchBox = document.querySelector('.search-box');
    const modalSearchElement = document.querySelector('.modal-search');
    const toggleButton = document.querySelector('.toggle');
    const navMenu = document.querySelector('.nav_link ul');
    const categoriesButton = document.querySelector('.categories');
    const modalCategory = document.querySelector('.modal-category');
    const searchSection = document.querySelector('.search');

    // Scroll Effect - Add active class to search bar
    window.addEventListener('scroll', function() {
        if (window.scrollY > 100) {
            searchSection.classList.add('active');
        } else {
            searchSection.classList.remove('active');
        }
    });

    // Mobile Menu Toggle
    if (toggleButton) {
        toggleButton.addEventListener('click', function() {
            mobileMenu = !mobileMenu;
            
            if (mobileMenu) {
                navMenu.classList.remove('link', 'f_flex');
                navMenu.classList.add('nav-link-mobileMenu');
                toggleButton.querySelector('i').classList.remove('fas', 'fa-bars', 'open');
                toggleButton.querySelector('i').classList.add('fa', 'fa-times', 'close', 'home-bth');
            } else {
                navMenu.classList.remove('nav-link-mobileMenu');
                navMenu.classList.add('link', 'f_flex');
                toggleButton.querySelector('i').classList.remove('fa', 'fa-times', 'close', 'home-bth');
                toggleButton.querySelector('i').classList.add('fas', 'fa-bars', 'open');
            }
        });
    }

    // Close mobile menu when clicking on menu items
    const navLinks = document.querySelectorAll('.nav_link ul a');
    navLinks.forEach(link => {
        link.addEventListener('click', function() {
            if (mobileMenu) {
                mobileMenu = false;
                navMenu.classList.remove('nav-link-mobileMenu');
                navMenu.classList.add('link', 'f_flex');
                toggleButton.querySelector('i').classList.remove('fa', 'fa-times', 'close', 'home-bth');
                toggleButton.querySelector('i').classList.add('fas', 'fa-bars', 'open');
            }
        });
    });

    // Categories Modal Toggle
    if (categoriesButton) {
        categoriesButton.addEventListener('click', function() {
            showCategory = !showCategory;
            
            if (modalCategory) {
                if (showCategory) {
                    modalCategory.classList.add('show');
                } else {
                    modalCategory.classList.remove('show');
                }
            }
        });
    }

    // Search Input Handler
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const value = e.target.value;
            
            if (value.trim()) {
                modalSearch = true;
                modalSearchElement.classList.remove('hide');
                
                // Split search keywords
                const searchKeywords = value.toLowerCase().split(/\s+/);
                
                // Filter products - Cần lấy dữ liệu sản phẩm từ server
                // Ví dụ: fetch('/api/products') hoặc dữ liệu đã có sẵn
                const suggestions = filterProducts(allProducts, searchKeywords);
                
                // Remove duplicates
                listNameProduct = [...new Set(suggestions)];
                
                // Render search results
                renderSearchResults(listNameProduct);
                
            } else {
                modalSearch = false;
                modalSearchElement.classList.add('hide');
                listNameProduct = [];
            }
            
            selectedIndex = -1;
        });

        // Focus handler
        searchInput.addEventListener('focus', function() {
            if (searchInput.value.trim()) {
                modalSearch = true;
                modalSearchElement.classList.remove('hide');
            }
            selectedIndex = -1;
        });
    }

    // Click outside to close search modal
    document.addEventListener('mousedown', function(event) {
        if (searchBox && !searchBox.contains(event.target)) {
            modalSearch = false;
            if (modalSearchElement) {
                modalSearchElement.classList.add('hide');
            }
        }
    });

    // Keyboard navigation for search
    document.addEventListener('keydown', function(e) {
        if (!modalSearch || listNameProduct.length === 0) return;

        const searchItems = document.querySelectorAll('.modal-search-item');

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            selectedIndex = selectedIndex < listNameProduct.length - 1 ? selectedIndex + 1 : 0;
            updateSelectedItem(searchItems);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            selectedIndex = selectedIndex > 0 ? selectedIndex - 1 : listNameProduct.length - 1;
            updateSelectedItem(searchItems);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (selectedIndex !== -1) {
                handleSearch(listNameProduct[selectedIndex]);
            } else {
                handleSearch(searchInput.value);
            }
        }
    });

    // Helper Functions

    // Filter products based on search keywords
    function filterProducts(products, searchKeywords) {
        return products
            .map(product => product.name)
            .filter(name => {
                const productWords = name.toLowerCase().split(/\s+/);
                return searchKeywords.every(keyword =>
                    productWords.some(word => word.startsWith(keyword))
                );
            });
    }

    // Render search results
    function renderSearchResults(results) {
        const existingItems = document.querySelectorAll('.modal-search-item');
        existingItems.forEach(item => item.remove());

        const recommendedSection = document.querySelector('.recommended');
        
        results.forEach((item, index) => {
            const div = document.createElement('div');
            div.className = 'modal-search-item';
            div.innerHTML = `
                <span class="name">${item}</span>
                <i class="fa-solid fa-arrow-right"></i>
            `;
            
            div.addEventListener('click', function() {
                handleSearch(item);
            });
            
            if (recommendedSection) {
                modalSearchElement.insertBefore(div, recommendedSection);
            } else {
                modalSearchElement.appendChild(div);
            }
        });
    }

    // Update selected item visual
    function updateSelectedItem(items) {
        items.forEach((item, index) => {
            if (index === selectedIndex) {
                item.classList.add('selected');
            } else {
                item.classList.remove('selected');
            }
        });
    }

    // Handle search submission
    function handleSearch(searchTerm) {
        // Redirect to search results page
        window.location.href = `/item-search?q=${encodeURIComponent(searchTerm)}`;
        
        // Close modal
        modalSearch = false;
        modalSearchElement.classList.add('hide');
        searchInput.blur();
    }

    // Scroll to top function
    function scrollToTop() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }

    // Add scroll to top for logo and cart links
    const logoLink = document.querySelector('.logo a');
    const cartLink = document.querySelector('.cart a');
    
    if (logoLink) {
        logoLink.addEventListener('click', scrollToTop);
    }
    
    if (cartLink) {
        cartLink.addEventListener('click', scrollToTop);
    }

    // Recommended products click handler
    const recommendedBoxes = document.querySelectorAll('.recommended-container .box a');
    recommendedBoxes.forEach(box => {
        box.addEventListener('click', function() {
            modalSearch = false;
            modalSearchElement.classList.add('hide');
            scrollToTop();
        });
    });

    // Load products from server (example)
    // Bạn cần thay đổi URL API phù hợp với backend của bạn
    function loadProducts() {
        // Example: Fetch products from API
        // fetch('/api/products')
        //     .then(response => response.json())
        //     .then(data => {
        //         allProducts = data;
        //     })
        //     .catch(error => console.error('Error loading products:', error));
        
        // Hoặc nếu products đã được render trong HTML:
        const productsData = document.getElementById('products-data');
        if (productsData) {
            try {
                allProducts = JSON.parse(productsData.textContent);
            } catch (e) {
                console.error('Error parsing products data:', e);
            }
        }
    }

    // Initialize
    loadProducts();

    // Format currency to VND
    function toVND(price) {
        return new Intl.NumberFormat('vi-VN').format(price);
    }

    // Export functions if needed
    window.headerFunctions = {
        scrollToTop,
        toVND
    };
});