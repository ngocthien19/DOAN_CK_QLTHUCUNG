package vn.iotstar.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.iotstar.entity.NguoiDung;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private String trangThai; // Thêm field này

    // Constructor mới với trangThai
    public UserDetailsImpl(Integer id, String username, String email, String password,
                          Collection<? extends GrantedAuthority> authorities, String trangThai) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.trangThai = trangThai;
    }

    public static UserDetailsImpl build(NguoiDung user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(
            "ROLE_" + user.getVaiTro().getMaVaiTro().toUpperCase()
        );

        return new UserDetailsImpl(
            user.getMaNguoiDung(),
            user.getEmail(),
            user.getEmail(),
            user.getMatKhau(),
            Collections.singletonList(authority),
            user.getTrangThai() // Truyền trạng thái từ entity
        );
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Kiểm tra trạng thái từ field, không phải method getUserStatus()
        return "Hoạt động".equals(trangThai);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}