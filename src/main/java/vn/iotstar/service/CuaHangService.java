package vn.iotstar.service;

import java.util.List;

import vn.iotstar.entity.CuaHang;

public interface CuaHangService {
	// Lấy tất cả các cửa hàng
    List<CuaHang> findAll();
    
    // Lấy cửa hàng theo mã
    CuaHang findByMaCuaHang(Integer maCuaHang);
    
    // Lấy cửa hàng theo tên
    CuaHang findByTenCuaHang(String tenCuaHang);
     
    List<CuaHang> findTop3NewestStores();
}
