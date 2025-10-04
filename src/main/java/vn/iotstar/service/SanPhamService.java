package vn.iotstar.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.iotstar.entity.SanPham;
import vn.iotstar.entity.DanhMuc;

public interface SanPhamService {
    List<SanPham> findTop4ByDanhMucOrderByNgayNhapDesc(DanhMuc danhMuc);
    SanPham findByMaSanPham(Integer maSanPham);
    List<SanPham> findRelatedProductsByCategoryExcludingCurrent(DanhMuc danhMuc, Integer maSanPham);
    
    List<SanPham> findByDanhMuc(DanhMuc danhMuc);
    
    // THÊM PHƯƠNG THỨC MỚI - Phân trang theo DanhMuc
    Page<SanPham> findByDanhMuc(DanhMuc danhMuc, Pageable pageable);
    
}