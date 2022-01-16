package pvs.app.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;
import pvs.app.Application;
import pvs.app.dao.MemberDAO;
import pvs.app.entity.Member;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserDetailServiceImplTest {

    @MockBean
    private MemberDAO mockMemberDAO;

    @Autowired
    @Qualifier("userDetailsServiceImpl")
    private UserDetailsService userDetailsServiceImpl;

    private Member member;

    @Before
    public void setup() {
        this.userDetailsServiceImpl = new UserDetailsServiceImpl(mockMemberDAO);

        member = new Member();

        member.setMemberId(1L);
        member.setUsername("test");
        member.setPassword(DigestUtils.md5DigestAsHex("test".getBytes()));
    }

    @Test
    public void loadUserByUsername_found() {
        //given
        Mockito.when(mockMemberDAO.findByUsername("test")).thenReturn(member);
        //when
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername("test");
        //then
        Assert.assertEquals(member, userDetails);
    }

    @Test
    public void loadUserByUsername_notFound() {
        //given
        Mockito.when(mockMemberDAO.findByUsername("test")).thenThrow(new UsernameNotFoundException("not found"));
        //when
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername("notFound");
        //then
        Assert.assertNull(userDetails);
    }
}
