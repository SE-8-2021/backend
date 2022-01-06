package pvs.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pvs.app.dto.MemberDTO;
import pvs.app.service.AuthService;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    /**
     * 登录
     */
    @PostMapping(value = "/auth/login")
    public String login(@RequestBody MemberDTO memberDTO) {
        // FIXME remove simplified chinese comments
        // 登录成功会返回Token给用户
        return authService.login(memberDTO.getUsername(), memberDTO.getPassword());
    }

    @GetMapping(value = "/auth/memberId")
    public Long getMemberID(@RequestParam("username") String username) {
        return authService.getMemberId(username);
    }
}
