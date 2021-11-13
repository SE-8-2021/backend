package pvs.app.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.DigestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pvs.app.filter.JwtTokenFilter;
import pvs.app.service.CustomOAuth2UserService;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    static final Logger logger = LogManager.getLogger(SecurityConfig.class.getName());
    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, @Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {

        //校驗使用者
        try {
            auth.userDetailsService(userDetailsService).passwordEncoder(new PasswordEncoder() {
                //對密碼進行加密
                @Override
                public String encode(CharSequence charSequence) {
                    return DigestUtils.md5DigestAsHex(charSequence.toString().getBytes());
                }

                //對密碼進行判斷匹配
                @Override
                public boolean matches(CharSequence charSequence, String s) {
                    String encode = DigestUtils.md5DigestAsHex(charSequence.toString().getBytes());

                    return s.equals(encode);
                }
            });
        } catch (Exception e) {
            logger.debug(e);
            e.printStackTrace();
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and()
                .csrf().disable()
                //因為使用JWT，所以不需要HttpSession
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                //OPTIONS請求全部放行
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                //登入介面放行
                .antMatchers("/auth/login").permitAll()
                .antMatchers("/oauth2/authorization/github").permitAll()
                .antMatchers(HttpMethod.POST, "/member").permitAll()
                .antMatchers("/project/**", "/github/**", "/sonar/**").hasAuthority("USER")
                .and()
                .oauth2Login()
                .loginPage("/auth/login")
                .userInfoEndpoint().userService(customOAuth2UserService);

        //使用自定義的 Token過濾器 驗證請求的Token是否合法
        http.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
        http.headers().cacheControl();
    }

    @Bean
    public JwtTokenFilter authenticationTokenFilterBean() {
        return new JwtTokenFilter();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3001"));
        configuration.setAllowedMethods(List.of(HttpMethod.OPTIONS.toString()));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
