package pvs.app.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
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

        // Encode Password with Argon2 Algorithm
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        String encodedPassword = argon2.hash(4, 1024 * 1024, 8, memberDTO.getPassword());
        memberDTO.setPassword(encodedPassword);
        Member member = modelMapper.map(memberDTO, Member.class);
        this.memberDAO.save(member);
        return true;
    }

    /**
     * Ensure that we have met the following password criteria:
     *  1. At least one number
     *  2. At least one lowercase
     *  3. At least one uppercase
     *  4. At least one special character
     *  5. More than 8 digits
     */
    public boolean passwordValidates( String password ) {
        String passwordRegex = "^(?=.*?[0-9])(?=.*?[A-Za-z])(?=(?=.*?[`!@#$%^&*()_+-])|(?=.*?[=\\[\\]{};'\":|,.<>/?~])).{8,}$";
        return password.matches(passwordRegex);
    }

    public Long getMemberId(String username) {
        Member member = this.memberDAO.findByUsername(username);
        return member == null ? null : member.getMemberId();
    }
}
