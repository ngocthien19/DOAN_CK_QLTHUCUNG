package vn.iotstar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads/images/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // QUAN TRỌNG: Expose upload directory với đường dẫn tuyệt đối
        Path uploadDir = Paths.get(uploadPath);
        String absoluteUploadPath = uploadDir.toFile().getAbsolutePath();
        
        // Sửa lại để chắc chắn có "/" ở cuối
        if (!absoluteUploadPath.endsWith("/") && !absoluteUploadPath.endsWith("\\")) {
            absoluteUploadPath += "/";
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absoluteUploadPath);
        
        // Static resources
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}