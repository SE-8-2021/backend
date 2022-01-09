package pvs.app.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {
    private static final String SALT = System.getenv("JWT_SALT");
    private static final int ACCESS_TOKEN_VALIDITY_SECONDS = 172800; // 2 days
    private static final Key SIGNING_KEY = new SecretKeySpec(SALT.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

    private final UserDetailsService userDetailsService;

    public JwtTokenUtil(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, @NotNull Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SIGNING_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    @NotNull
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(@NotNull UserDetails userDetails) {
        return doGenerateToken(userDetails.getUsername());
    }

    private String doGenerateToken(String subject) {
        Claims claims = Jwts.claims().setSubject(subject);
        final Date currentTime = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer("https://pvs.xcc.tw")
                .setIssuedAt(currentTime)
                .setExpiration(new Date(currentTime.getTime() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000))
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY)
                .compact();
    }

    public Boolean isValidToken(String token) {
        if (!isJWT(token)) return false;
        final String username = getUsernameFromToken(token);
        try {
            final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (!Objects.equals(username, userDetails.getUsername())) return false;
        } catch (UsernameNotFoundException err) {
            return false;
        }
        return !isTokenExpired(token);
    }

    public boolean isJWT(@Nullable String jwt) {
        if (jwt == null) return false;
        final String[] seperatedJwt = jwt.split("\\.");
        if (seperatedJwt.length != 3) return false;
        try {
            final String jsonFirstPart = new String(Base64.getDecoder().decode(seperatedJwt[0]));
            final JSONObject firstPart = new JSONObject(jsonFirstPart);
            if (!(firstPart.has("alg") && firstPart.get("alg").equals("HS512"))) return false;
            final String jsonSecondPart = new String(Base64.getDecoder().decode(seperatedJwt[1]));
            final JSONObject secondPart = new JSONObject(jsonSecondPart);
            if (!secondPart.has("exp") || !secondPart.has("sub") || !secondPart.has("iss") || !secondPart.has("iat"))
                return false;
        } catch (JSONException err) {
            return false;
        }
        return true;
    }
}
