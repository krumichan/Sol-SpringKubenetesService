package kr.co.classact.rancher.login.config;

import kr.co.classact.rancher.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(
                "/css/**"
                , "/js/**"
                , "/img/**"
                , "/bootstrap/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .antMatchers("/login", "/signup", "/user").permitAll()
                    .antMatchers("/").hasRole("USER")
                    .antMatchers("/admin").hasRole("ADMIN")
                    .anyRequest().authenticated()
                .and()
                    .formLogin()
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                .and()
                    .logout()
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
        ;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService)
                .passwordEncoder(new BCryptPasswordEncoder());
    }
}

/*
 * line 13
 *     # Spring Security 를 활성화.
 *
 * line 15
 *     # WebSecurityConfigureAdapter : Spring Security 의 설정파일로서의 역할을 수행하기 위해 상속하는 클래스.
 *
 * line 20
 *     # 인증을 무시할 경로들을 설정.
 *     # ( static 의 하위 폴더(css, js, img 등)는 무조건 접근이 가능해야하기 때문에 인증 무시 )
 *
 * line 25
 *     # http 관련 인증 설정.
 *
 * line 27
 *     # 접근에 대한 인증 설정.
 *     # anyMatchers             : 경로 설정 및 해당 경로의 권한 설정.
 *     # permitAll()             : 누구나 접근 가능.
 *     # hasRole()               : 특정 권한을 가진 유저만 접근 가능.
 *     # authenticated()         : 권한이 있다면 무조건 접근 가능.
 *
 * line 31
 *     # 위 줄에 정의된 경로 외의 모든 요청들은 권한의 종류에 상관 없이 권한이 있어야만 접근 가능.
 *
 * line 33
 *     # 로그인에 대한 설정.
 *     # loginPage()             : 로그인 페이지 링크 설정.
 *     # defaultSuccessUrl()     : 로그인 성공 후 redirect 할 경로.
 *
 * line 37
 *     # 로그아웃에 대한 설정.
 *     # logoutSuccessUrl()      : 로그아웃 성공 후 redirect 할 경로.
 *     # invalidateHttpSession() : 로그아웃 이후 세션 전체 삭제 여부.
 *
 * line 44
 *     # 로그인 할 때 필요한 정보를 가지고 오는 곳.
 *
 * line 45
 *     # 유저 정보를 가지고 올 서비스를 지정.
 *
 * line 46
 *     # 패스워드 인코더를 결정.
 */