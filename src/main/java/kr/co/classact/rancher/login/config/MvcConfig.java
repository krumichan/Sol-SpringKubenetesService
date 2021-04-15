package kr.co.classact.rancher.login.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    public void addViewControllers(ViewControllerRegistry registry) {
        // TODO: User 관련 View 이후 수정 필요.
        registry.addViewController("/").setViewName("user/main");
        registry.addViewController("/login").setViewName("/user/login_two");
        registry.addViewController("/admin").setViewName("/user/admin");
        registry.addViewController("/signup").setViewName("/user/register");
    }
}
