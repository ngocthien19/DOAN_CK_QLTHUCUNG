package vn.iotstar.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        // Lấy danh sách các danh mục
        List<DanhMuc> danhMucs = danhMucRepository.findAll();
        
        List<CuaHang> cuaHangs = cuaHangRepository.findTop3NewestStores();

        // Lấy 4 sản phẩm cho mỗi danh mục
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
        String storeName = sanPham.getCuaHang().getTenCuaHang();

        List<SanPham> relatedProducts = sanPhamService.findRelatedProductsByCategoryExcludingCurrent(sanPham.getDanhMuc(), maSanPham);
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
            Model model) {
        
        // Lấy danh mục theo tên (đã chuyển đổi từ URL)
        String decodedTenDanhMuc = decodeCategoryName(tenDanhMuc);
        DanhMuc danhMuc = danhMucService.findByTenDanhMuc(decodedTenDanhMuc);
        
        if (danhMuc == null) {
            return "redirect:/";
        }
        
        // Phân trang sản phẩm - SỬA DÒNG NÀY
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<SanPham> productPage = sanPhamService.findByDanhMuc(danhMuc, pageable);
        
        // Lấy TẤT CẢ sản phẩm thuộc danh mục này (cho filter)
        List<SanPham> allProducts = sanPhamService.findByDanhMuc(danhMuc);
        
        // Lấy danh sách cửa hàng duy nhất từ tất cả sản phẩm
        List<String> stores = allProducts.stream()
                .map(sp -> sp.getCuaHang().getTenCuaHang())
                .distinct()
                .collect(Collectors.toList());
        
        // Lấy danh sách loại sản phẩm duy nhất từ tất cả sản phẩm
        List<String> loaiSanPhams = allProducts.stream()
                .map(SanPham::getLoaiSanPham)
                .distinct()
                .collect(Collectors.toList());
        
        // Set banner URL
        String bannerUrl = getBannerUrlByCategory(danhMuc.getMaDanhMuc());
         
        // Add attributes to model
        model.addAttribute("categoryName", danhMuc.getTenDanhMuc());
        model.addAttribute("products", productPage.getContent()); // Sản phẩm của trang hiện tại
        model.addAttribute("stores", stores);
        model.addAttribute("loaiSanPhams", loaiSanPhams);
        model.addAttribute("bannerUrl", bannerUrl);
        
        // Phân trang attributes
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageUrl", "/category/" + tenDanhMuc);
        
        return "web/productList";
    }

    private String decodeCategoryName(String encodedName) {
        // Chuyển đổi từ URL-friendly name về tên gốc
        switch (encodedName.toLowerCase()) {
            case "cho-canh":
                return "Chó cảnh";
            case "meo-canh":
                return "Mèo cảnh";
            case "phu-kien":
                return "Phụ kiện";
            default:
                // Nếu không khớp, trả về encodedName (cho trường hợp tên khác)
                return encodedName.replace("-", " ");
        }
    }

    private String getBannerUrlByCategory(Integer maDanhMuc) {
        switch (maDanhMuc) {
            case 1: // Chó cảnh
                return "/images/banner-cho-canh.jpg";
            case 2: // Mèo cảnh
                return "/images/banner-meo-canh.jpg";
            case 3: // Phụ kiện
                return "/images/banner-phu-kien.jpg";
            default:
                return "/images/banner-default.jpg";
        }
    }
}