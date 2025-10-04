package vn.iotstar.entity;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "CuaHang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuaHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCuaHang")
    private Integer maCuaHang;
    
    @NotNull(message = "Chủ cửa hàng không được để trống")
    @ManyToOne
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;
    
    @NotBlank(message = "Tên cửa hàng không được để trống")
    @Size(min = 2, max = 100, message = "Tên cửa hàng phải từ 2 đến 100 ký tự")
    @Column(name = "TenCuaHang", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String tenCuaHang;
    
    @Size(max = 2000, message = "Mô tả không được quá 2000 ký tự")
    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NgayTao")
    @Builder.Default
    private Date ngayTao = new Date();
    
    @Column(name = "HinhAnh", columnDefinition = "NVARCHAR(255)")
    private String hinhAnh;
    
    @OneToMany(mappedBy = "cuaHang", cascade = CascadeType.ALL)
    private List<SanPham> sanPhams;
    
    @OneToMany(mappedBy = "cuaHang", cascade = CascadeType.ALL)
    private List<KhuyenMai> khuyenMais;
}