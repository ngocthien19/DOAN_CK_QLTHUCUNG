package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.CuaHang;

import java.util.List;

@Repository
public interface CuaHangRepository extends JpaRepository<CuaHang, Integer> {
    
    // Lấy tất cả các cửa hàng
    List<CuaHang> findAll();
    
    // Lấy cửa hàng theo mã
    CuaHang findByMaCuaHang(Integer maCuaHang);
    
    // Lấy cửa hàng theo tên
    CuaHang findByTenCuaHang(String tenCuaHang);
    
    List<CuaHang> findTop3ByOrderByNgayTaoDesc();
}