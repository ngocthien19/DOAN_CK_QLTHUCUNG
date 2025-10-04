let allProducts = Array.from(document.querySelectorAll('.product-card'));
let productsContainer = document.getElementById('products-container');
const productCount = document.getElementById('product-count');
const productHeader = document.getElementById('product-header');
const noProducts = document.getElementById('no-products');
const sortSelect = document.getElementById('sort-select');

let filteredProducts = [...allProducts];

// Filter function 
function filterProducts() {
	const selectedPrices = Array.from(document.querySelectorAll('input[name="price"]:checked')).map(cb => cb.value);
	const selectedStores = Array.from(document.querySelectorAll('input[name="store"]:checked')).map(cb => cb.value);
	const selectedLoai = Array.from(document.querySelectorAll('input[name="loai"]:checked')).map(cb => cb.value);
	const selectedStars = Array.from(document.querySelectorAll('input[name="star"]:checked')).map(cb => cb.value);

	filteredProducts = allProducts.filter(product => {
		const price = parseFloat(product.dataset.price);
		const store = product.dataset.store;
		const loai = product.dataset.loai;
		const star = parseFloat(product.dataset.star);

		// Price filter
		const matchesPrice = selectedPrices.length === 0 || selectedPrices.some(range => {
			if (range === 'below10') return price < 10000000;
			if (range === '1015') return price >= 10000000 && price <= 15000000;
			if (range === '1520') return price >= 15000000 && price <= 20000000;
			if (range === '2025') return price >= 20000000 && price <= 25000000;
			if (range === 'above25') return price >= 25000000;
			return false;
		});

		// Store filter
		const matchesStore = selectedStores.length === 0 || selectedStores.includes(store);

		// Loai san pham filter
		const matchesLoai = selectedLoai.length === 0 || selectedLoai.includes(loai);

		// Star filter
		const matchesStar = selectedStars.length === 0 || selectedStars.some(s => {
			if (s === '0') return star === 0;
			if (s === '12') return star >= 1 && star <= 2;
			if (s === '23') return star > 2 && star <= 3;
			if (s === '34') return star > 3 && star <= 4;
			if (s === '45') return star > 4 && star <= 5;
			return false;
		});

		return matchesPrice && matchesStore && matchesLoai && matchesStar;
	});

	displayProducts();
}

// Sort function
function sortProducts(sortValue) {
	const sorted = [...filteredProducts];

	if (sortValue === 'asc-name') {
		sorted.sort((a, b) => a.dataset.name.localeCompare(b.dataset.name));
	} else if (sortValue === 'dsc-name') {
		sorted.sort((a, b) => b.dataset.name.localeCompare(a.dataset.name));
	} else if (sortValue === 'asc-price') {
		sorted.sort((a, b) => parseFloat(a.dataset.price) - parseFloat(b.dataset.price));
	} else if (sortValue === 'dsc-price') {
		sorted.sort((a, b) => parseFloat(b.dataset.price) - parseFloat(a.dataset.price));
	} else if (sortValue === 'asc-like') {
		sorted.sort((a, b) => parseInt(a.dataset.like) - parseInt(b.dataset.like));
	} else if (sortValue === 'dsc-like') {
		sorted.sort((a, b) => parseInt(b.dataset.like) - parseInt(a.dataset.like));
	}

	filteredProducts = sorted;
	displayProducts();
}

// Display products
function displayProducts() {
	// Clear container
	while (productsContainer.firstChild) {
		productsContainer.removeChild(productsContainer.firstChild);
	}

	if (filteredProducts.length === 0) {
		productHeader.style.display = 'none';
		noProducts.style.display = 'flex';
	} else {
		productHeader.style.display = 'flex';
		noProducts.style.display = 'none';
		productCount.textContent = `${filteredProducts.length} items`;

		// Add filtered products
		filteredProducts.forEach(product => {
			productsContainer.appendChild(product.cloneNode(true));
		});

		// Re-initialize AOS for new elements
		if (typeof AOS !== 'undefined') {
			AOS.refresh();
		}
	}
}

// Event listeners
document.querySelectorAll('input[type="checkbox"]').forEach(checkbox => {
	checkbox.addEventListener('change', filterProducts);
});

sortSelect.addEventListener('change', (e) => {
	sortProducts(e.target.value);
});

// Add to cart function
function addToCart(productId) {
	console.log('Add to cart:', productId);
	// Implement your add to cart logic here
	alert('Product added to cart!');
}