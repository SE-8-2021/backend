package pvs.app.service.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;
import pvs.app.Application;
import pvs.app.entity.Member;
import pvs.app.utils.JwtTokenUtil;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class JwtTokenUtilTest {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private Member memberUser;

    @Before
    public void setup() throws IOException {
        memberUser = new Member();

        memberUser.setMemberId(1L);
        memberUser.setUsername("user");
        memberUser.setPassword(DigestUtils.md5DigestAsHex("user".getBytes()));

        Member memberAdmin = new Member();

        memberAdmin.setMemberId(2L);
        memberAdmin.setUsername("admin");
        memberAdmin.setPassword(DigestUtils.md5DigestAsHex("admin".getBytes()));
    }

    @Test
    public void validToken() {
        //given
        UserDetails userDetails = memberUser;
        //when
        String token = jwtTokenUtil.generateToken(userDetails);
        boolean tokenValidated = jwtTokenUtil.isValidToken(token);
        //then
        Assert.assertTrue(tokenValidated);
    }

    @Test
    public void invalidToken() {
        //given
        UserDetails authenticatedUser = memberUser;

        //when
        String token = jwtTokenUtil.generateToken(authenticatedUser);
        boolean tokenValidated = jwtTokenUtil.isValidToken(token);

        //then
        Assert.assertFalse(tokenValidated);
    }


}
