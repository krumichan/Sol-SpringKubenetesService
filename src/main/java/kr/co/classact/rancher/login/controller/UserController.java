package kr.co.classact.rancher.login.controller;

import kr.co.classact.rancher.login.domain.UserDto;
import kr.co.classact.rancher.login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    /**
     * 회원 추가.
     */
    @PostMapping("/user")
    public String signup(UserDto userDto) {
        userService.save(userDto);
        return "redirect:/login";
    }

//    @GetMapping("/")
//    public String main(Model model) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        model.addAttribute(authentication.getPrincipal());
//        return "user/main";
//    }

    /**
     * 로그아웃.
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler()
                .logout(
                        request
                        , response
                        , SecurityContextHolder.getContext().getAuthentication()
                );
        return "redirect:/login";
    }
}
