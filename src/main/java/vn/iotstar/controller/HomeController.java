package vn.iotstar.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import vn.iotstar.entity.CuaHang;
import vn.iotstar.entity.DanhMuc;
import vn.iotstar.entity.SanPham;
import vn.iotstar.repository.DanhMucRepository;
import vn.iotstar.service.CuaHangService;
import vn.iotstar.service.DanhMucService;
import vn.iotstar.service.SanPhamService;
import vn.iotstar.specification.SanPhamSpecification;

@Controller
public class HomeController {
    
    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private DanhMucRepository danhMucRepository;
    
    @Autowired
    private DanhMucService danhMucService;
    
    @Autowired
    private CuaHangService cuaHangRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<DanhMuc> danhMucs = danhMucRepository.findAll();
        List<CuaHang> cuaHangs = cuaHangRepository.findTop3NewestStores();

        for (DanhMuc danhMuc : danhMucs) {
            List<SanPham> sanPhams = sanPhamService.findTop4ByDanhMucOrderByNgayNhapDesc(danhMuc);
            model.addAttribute("sanPhams_" + danhMuc.getMaDanhMuc(), sanPhams);
        }

        model.addAttribute("danhMucs", danhMucs);
        model.addAttribute("cuaHangs", cuaHangs);
        return "index"; 
    }
    
    @GetMapping("/view/{MaSanPham}")
    public String viewProductDetail(@PathVariable("MaSanPham") Integer maSanPham, Model model) {
        SanPham sanPham = sanPhamService.findByMaSanPham(maSanPham);

        List<SanPham> relatedProducts = sanPhamService.findRelatedProductsByCategoryExcludingCurrent(
            sanPham.getDanhMuc(), maSanPham);
        if (relatedProducts == null) {
            relatedProducts = new ArrayList<>();
        }

        model.addAttribute("ItemProduct", sanPham);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("categoryName", sanPham.getDanhMuc().getTenDanhMuc());

        return "web/productDetail";
    }
    
    @GetMapping("/category/{tenDanhMuc}")
    public String productList(
            @PathVariable("tenDanhMuc") String tenDanhMuc,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) List<String> price,
            @RequestParam(required = false) List<String> store,
            @RequestParam(required = false) List<String> loai,
            @RequestParam(required = false) List<String> star,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            Model model) {
        
        String decodedTenDanhMuc = decodeCategoryName(tenDanhMuc);
        DanhMuc danhMuc = danhMucService.findByTenDanhMuc(decodedTenDanhMuc);
        
        if (danhMuc == null) {
            return "redirect:/";
        }
        
        // Tạo Specification với filters
        Specification<SanPham> spec = SanPhamSpecification.filterProducts(
            danhMuc, price, store, loai, star, search
        );
        
        // Tạo Sort
        Sort sortObj = createSort(sort);
        
        // Tạo Pageable - Database chỉ query đúng page cần thiết
        Pageable pageable = PageRequest.of(page - 1, size, sortObj);
        
        // Query với Specification
        Page<SanPham> productPage = sanPhamService.findAll(spec, pageable);
        
        // Lấy danh sách stores và loại (không filter) cho dropdown
        List<SanPham> allProducts = sanPhamService.findByDanhMuc(danhMuc);
        
        List<String> stores = allProducts.stream()
                .map(sp -> sp.getCuaHang().getTenCuaHang())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        List<String> loaiSanPhams = allProducts.stream()
                .map(SanPham::getLoaiSanPham)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        String bannerUrl = getBannerUrlByCategory(danhMuc.getMaDanhMuc());
         
        // Add attributes
        model.addAttribute("categoryName", danhMuc.getTenDanhMuc());
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("stores", stores);
        model.addAttribute("loaiSanPhams", loaiSanPhams);
        model.addAttribute("bannerUrl", bannerUrl);
        
        // Pagination
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageUrl", "/category/" + tenDanhMuc);
        
        // Selected filters
        model.addAttribute("selectedPrices", price != null ? price : new ArrayList<>());
        model.addAttribute("selectedStores", store != null ? store : new ArrayList<>());
        model.addAttribute("selectedLoais", loai != null ? loai : new ArrayList<>());
        model.addAttribute("selectedStars", star != null ? star : new ArrayList<>());
        model.addAttribute("selectedSort", sort != null ? sort : "default");
        model.addAttribute("searchKeyword", search != null ? search : "");
        
        return "web/productList";
    }
    
    private Sort createSort(String sortType) {
        if (sortType == null || sortType.equals("default")) {
            return Sort.by(Sort.Direction.DESC, "ngayNhap");
        }
        
        switch (sortType) {
            case "asc-name":
                return Sort.by(Sort.Direction.ASC, "tenSanPham");
            case "dsc-name":
                return Sort.by(Sort.Direction.DESC, "tenSanPham");
            case "asc-price":
                return Sort.by(Sort.Direction.ASC, "giaBan");
            case "dsc-price":
                return Sort.by(Sort.Direction.DESC, "giaBan");
            case "asc-like":
                return Sort.by(Sort.Direction.ASC, "luotThich");
            case "dsc-like":
                return Sort.by(Sort.Direction.DESC, "luotThich");
            default:
                return Sort.by(Sort.Direction.DESC, "ngayNhap");
        }
    }

    private String decodeCategoryName(String encodedName) {
        switch (encodedName.toLowerCase()) {
            case "cho-canh":
                return "Chó cảnh";
            case "meo-canh":
                return "Mèo cảnh";
            case "phu-kien":
                return "Phụ kiện";
            default:
                return encodedName.replace("-", " ");
        }
    }

    private String getBannerUrlByCategory(Integer maDanhMuc) {
        switch (maDanhMuc) {
            case 1:
                return "/images/banner-cho-canh.jpg";
            case 2:
                return "/images/banner-meo-canh.jpg";
            case 3:
                return "/images/banner-phu-kien.jpg";
            default:
                return "/images/banner-default.jpg";
        }
    }
    
    @GetMapping("/products")
    public String allProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) List<String> price,
            @RequestParam(required = false) List<String> store,
            @RequestParam(required = false) List<String> loai,
            @RequestParam(required = false) List<String> star,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            Model model) {
        
        // Decode UTF-8 pour le search
        if (search != null && !search.trim().isEmpty()) {
            try {
                search = java.net.URLDecoder.decode(search, "UTF-8");
                search = search.trim();
            } catch (Exception e) {
                // Si déjà décodé, continuer
            }
        }
        
        // Créer Specification (null pour danhMuc = chercher tout)
        Specification<SanPham> spec = SanPhamSpecification.filterProducts(
            null, price, store, loai, star, search
        );
        
        // Créer Sort
        Sort sortObj = createSort(sort);
        
        // Query avec Specification
        Pageable pageable = PageRequest.of(page - 1, size, sortObj);
        Page<SanPham> productPage = sanPhamService.findAll(spec, pageable);
        
        // Récupérer liste stores et loai pour filter
        List<SanPham> allProducts = sanPhamService.findAll();
        
        List<String> stores = allProducts.stream()
                .map(sp -> sp.getCuaHang().getTenCuaHang())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        List<String> loaiSanPhams = allProducts.stream()
                .map(SanPham::getLoaiSanPham)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        // Add attributes
        model.addAttribute("categoryName", "Tất cả sản phẩm");
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("stores", stores);
        model.addAttribute("loaiSanPhams", loaiSanPhams);
        model.addAttribute("bannerUrl", "/images/banner-default.jpg");
        
        // Pagination
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageUrl", "/products");
        
        // Selected filters
        model.addAttribute("selectedPrices", price != null ? price : new ArrayList<>());
        model.addAttribute("selectedStores", store != null ? store : new ArrayList<>());
        model.addAttribute("selectedLoais", loai != null ? loai : new ArrayList<>());
        model.addAttribute("selectedStars", star != null ? star : new ArrayList<>());
        model.addAttribute("selectedSort", sort != null ? sort : "default");
        model.addAttribute("searchKeyword", search != null ? search : "");
        
        return "web/products";
    }
}