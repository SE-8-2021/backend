package pvs.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
        // return jwt if login success
        return authService.login(memberDTO.getUsername(), memberDTO.getPassword());
    }

    @GetMapping(value = "/auth/memberId")
    public Long getMemberID(@RequestParam("username") String username) {
        return authService.getMemberId(username);
    }
}
