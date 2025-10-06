// Header JavaScript File

document.addEventListener('DOMContentLoaded', function() {
    
    // Variables
    let mobileMenu = false;
    let showCategory = false;
    let selectedIndex = -1;
    let searchTimeout;

    // DOM Elements
    const searchBox = document.querySelector('.search-box');
    const modalSearchElement = document.querySelector('.modal-search');
    const toggleButton = document.querySelector('.toggle');
    const navMenu = document.querySelector('.nav_link ul');
    const categoriesButton = document.querySelector('.categories');
    const modalCategory = document.querySelector('.modal-category');
    const searchSection = document.querySelector('.search');
    const searchInput = document.getElementById('search-input');
    const searchForm = searchInput?.closest('form');
	
	// Trong hàm checkAuthStatus(), sửa phần hiển thị user menu
	function checkAuthStatus() {
	    const token = localStorage.getItem('jwtToken');
	    const userData = localStorage.getItem('user');
	    const authButtons = document.getElementById('authButtons');
	    const userMenu = document.getElementById('userMenu');
	    const userAccountLink = document.getElementById('userAccountLink');
	    
	    if (token && userData) {
	        // Đã đăng nhập
	        authButtons.style.display = 'none';
	        userMenu.style.display = 'flex';
	        userAccountLink.style.display = 'block';
	        
	        try {
	            const user = JSON.parse(userData);
	            document.getElementById('userName').textContent = user.username || 'User';
	            
	            // ✅ THÊM TOKEN VÀO URL PROFILE
	            const profileLink = document.querySelector('a[href="/profile"]');
	            if (profileLink) {
	                profileLink.href = `/profile?token=${token}`;
	            }
	            
	        } catch (e) {
	            console.error('Error parsing user data:', e);
	        }
	        
	        // Load cart quantity
	        loadCartQuantity();
	    } else {
	        // Chưa đăng nhập
	        authButtons.style.display = 'flex';
	        userMenu.style.display = 'none';
	        userAccountLink.style.display = 'none';
	    }
	}

    // Initialize search functionality
    function initSearch() {
        if (!searchInput || !modalSearchElement) return;

        // Prevent form submission when empty
        if (searchForm) {
            searchForm.addEventListener('submit', function(e) {
                const searchValue = searchInput.value.trim();
                if (!searchValue) {
                    e.preventDefault();
                }
            });
        }

        // Real-time search on input
        searchInput.addEventListener('input', function(e) {
            clearTimeout(searchTimeout);
            const searchValue = e.target.value.trim();
            
            if (searchValue.length >= 1) {
                searchTimeout = setTimeout(() => {
                    fetchSearchResults(searchValue);
                }, 300);
            } else {
                hideSearchModal();
            }
        });

        // Handle Enter key
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const searchValue = searchInput.value.trim();
                if (searchValue) {
                    hideSearchModal();
                    // Submit form or redirect
                    if (searchForm) {
                        searchForm.submit();
                    }
                }
            }

            // Keyboard navigation in search results
            if (modalSearchElement && !modalSearchElement.classList.contains('hide')) {
                const items = modalSearchElement.querySelectorAll('.modal-search-item');
                if (items.length === 0) return;

                switch(e.key) {
                    case 'ArrowDown':
                        e.preventDefault();
                        selectedIndex = Math.min(selectedIndex + 1, items.length - 1);
                        updateSelectedItem(items);
                        break;
                    case 'ArrowUp':
                        e.preventDefault();
                        selectedIndex = Math.max(selectedIndex - 1, -1);
                        updateSelectedItem(items);
                        break;
                    case 'Escape':
                        e.preventDefault();
                        hideSearchModal();
                        break;
                }
            }
        });

        // Show modal on focus if there's text
        searchInput.addEventListener('focus', function() {
            const searchValue = searchInput.value.trim();
            if (searchValue.length >= 1) {
                fetchSearchResults(searchValue);
            }
        });

        // Mouse events for search items
        modalSearchElement.addEventListener('mouseover', function(e) {
            const item = e.target.closest('.modal-search-item');
            if (item) {
                const items = modalSearchElement.querySelectorAll('.modal-search-item');
                selectedIndex = Array.from(items).indexOf(item);
                updateSelectedItem(items);
            }
        });

        modalSearchElement.addEventListener('click', function(e) {
            const item = e.target.closest('.modal-search-item');
            if (item && item.dataset.productId) {
                window.location.href = `/view/${item.dataset.productId}`;
            }
        });
    }

    // Fetch search results from API
    function fetchSearchResults(keyword) {
        fetch(`/api/search-suggestions?keyword=${encodeURIComponent(keyword)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                updateSearchModal(data, keyword);
            })
            .catch(error => {
                console.error('Search error:', error);
                showErrorState();
            });
    }

    // Update search modal with results
    function updateSearchModal(data, keyword) {
        if (!modalSearchElement) return;
        
        let html = '';

        // Search results section
        if (data.products && data.products.length > 0) {
            data.products.forEach(product => {
                html += `
                    <div class="modal-search-item" data-product-id="${product.maSanPham}">
                        <div class="product-info">
                            <img src="${product.hinhAnh || '/images/default-product.jpg'}" 
                                 alt="${escapeHtml(product.tenSanPham)}" 
                                 class="product-thumb">
                            <div class="product-details">
                                <span class="name">${escapeHtml(product.tenSanPham)}</span>
                                <span class="price">₫${formatPrice(product.giaBan)}</span>
                            </div>
                        </div>
                        <i class="fa-solid fa-arrow-right"></i>
                    </div>
                `;
            });
        } else {
            html += `
                <div class="no-results">
                    <i class="fa-solid fa-search"></i>
                    <span>Không tìm thấy sản phẩm cho "${escapeHtml(keyword)}"</span>
                </div>
            `;
        }

        // Recommended section
        if (data.recommended && data.recommended.length > 0) {
            html += `
                <div class="recommended-section">
                    <h3><i class="fa-solid fa-fire"></i> Sản phẩm gợi ý</h3>
                    <div class="recommended-container">
            `;
            
            data.recommended.forEach(item => {
                html += `
                    <div class="recommended-item" onclick="window.location.href='/view/${item.maSanPham}'">
                        <img src="${item.hinhAnh || '/images/default-product.jpg'}" 
                             alt="${escapeHtml(item.tenSanPham)}" 
                             class="recommended-image">
                        <div class="recommended-info">
                            <h4>${escapeHtml(item.tenSanPham)}</h4>
                            <p class="price">₫${formatPrice(item.giaBan)}</p>
                        </div>
                    </div>
                `;
            });
            
            html += `
                    </div>
                </div>
            `;
        }

        modalSearchElement.innerHTML = html;
        showSearchModal();
        selectedIndex = -1;
    }

    // Show search modal
    function showSearchModal() {
        if (modalSearchElement) {
            modalSearchElement.classList.remove('hide');
            // Add animation class
            modalSearchElement.classList.add('active');
        }
    }

    // Hide search modal
    function hideSearchModal() {
        if (modalSearchElement) {
            modalSearchElement.classList.add('hide');
            modalSearchElement.classList.remove('active');
        }
        selectedIndex = -1;
    }

    // Show error state
    function showErrorState() {
        if (!modalSearchElement) return;
        
        modalSearchElement.innerHTML = `
            <div class="error-state">
                <i class="fa-solid fa-exclamation-triangle"></i>
                <span>Đã có lỗi xảy ra khi tìm kiếm</span>
            </div>
        `;
        showSearchModal();
    }

    // Update selected item for keyboard navigation
    function updateSelectedItem(items) {
        items.forEach((item, index) => {
            item.classList.toggle('selected', index === selectedIndex);
            
            // Scroll into view if needed
            if (index === selectedIndex) {
                item.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
            }
        });
    }

    // Utility functions
    function escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatPrice(price) {
        if (!price) return '0';
        return new Intl.NumberFormat('vi-VN').format(price);
    }

    // Close modal when clicking outside
    document.addEventListener('click', function(e) {
        if (modalSearchElement && 
            !modalSearchElement.classList.contains('hide') &&
            !e.target.closest('.search-box') &&
            !e.target.closest('.modal-search')) {
            hideSearchModal();
        }
    });

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
                if (toggleButton) {
                    toggleButton.querySelector('i').classList.remove('fa', 'fa-times', 'close', 'home-bth');
                    toggleButton.querySelector('i').classList.add('fas', 'fa-bars', 'open');
                }
            }
        });
    });

    // Categories Modal Toggle
    if (categoriesButton && modalCategory) {
        categoriesButton.addEventListener('click', function(e) {
            e.stopPropagation();
            showCategory = !showCategory;
            modalCategory.classList.toggle('show', showCategory);
        });

        // Close categories when clicking outside
        document.addEventListener('click', function() {
            if (showCategory) {
                showCategory = false;
                modalCategory.classList.remove('show');
            }
        });

        // Prevent categories from closing when clicking inside
        modalCategory.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }

    // Initialize
    initSearch();

    // Export functions if needed
    window.searchFunctions = {
        showSearchModal,
        hideSearchModal,
        formatPrice
    };
});