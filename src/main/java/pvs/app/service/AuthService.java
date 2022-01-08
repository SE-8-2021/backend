package pvs.app.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import pvs.app.dao.MemberDAO;
import pvs.app.dto.MemberDTO;
import pvs.app.entity.Member;
import pvs.app.utils.JwtTokenUtil;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final JwtTokenUtil jwtTokenUtil;

    private final MemberDAO memberDAO;

    AuthService(AuthenticationManager authenticationManager,
                @Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService,
                JwtTokenUtil jwtTokenUtil,
                MemberDAO memberDAO) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.memberDAO = memberDAO;
    }

    public String login(String username, String password) {
        try {
            UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(upToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtTokenUtil.generateToken(userDetails);
        } catch (AuthenticationException  e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return null;
        }
    }

    public boolean register(MemberDTO memberDTO) {
        if (memberDAO.findByUsername(memberDTO.getUsername()) != null) return false;

        ModelMapper modelMapper = new ModelMapper();

        // encode password with md5
        String encodedPassword = DigestUtils.md5DigestAsHex(memberDTO.getPassword().getBytes());
        memberDTO.setPassword(encodedPassword);
        Member member = modelMapper.map(memberDTO, Member.class);
        this.memberDAO.save(member);
        return true;
    }

    public Long getMemberId(String username) {
        Member member = this.memberDAO.findByUsername(username);
        return member == null ? null : member.getMemberId();
    }
}
