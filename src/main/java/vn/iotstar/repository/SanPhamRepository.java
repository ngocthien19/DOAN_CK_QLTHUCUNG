package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.iotstar.entity.DanhMuc;
import vn.iotstar.entity.SanPham;

import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    
    @Query("SELECT s FROM SanPham s WHERE s.danhMuc = :danhMuc ORDER BY s.ngayNhap DESC")
    List<SanPham> findTop4ByDanhMucOrderByNgayNhapDesc(DanhMuc danhMuc, Pageable pageable);
    
    SanPham findByMaSanPham(Integer maSanPham);
    
    @Query("SELECT s FROM SanPham s WHERE s.danhMuc = :danhMuc AND s.maSanPham != :maSanPham")
    List<SanPham> findByDanhMucAndNotMaSanPham(DanhMuc danhMuc, Integer maSanPham);
    
    @Query("SELECT s FROM SanPham s WHERE s.danhMuc = :danhMuc")
    List<SanPham> findByDanhMuc(DanhMuc danhMuc);
    
    // THÊM PHƯƠNG THỨC MỚI - Phân trang theo DanhMuc object
    Page<SanPham> findByDanhMuc(DanhMuc danhMuc, Pageable pageable);
    
}