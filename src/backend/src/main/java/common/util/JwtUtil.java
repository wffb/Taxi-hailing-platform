package common.util;

import DAO.DriverDAO;
import authentication.AuthenticationEnforcer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import model.LoginUser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String CLAIM_KEY_ID = "sub";
    private static final String CLAIM_KEY_CREATED = "created";
    private static final String CLAIM_KEY_ROLE = "role";

    private String secret = "werb";

    private Long expiration = 3600L;

    private static volatile JwtUtil INSTANCE;
    public JwtUtil() {}
    public static JwtUtil getInstance() {
        if (INSTANCE == null) {
            synchronized (DriverDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new JwtUtil();
                }
            }
        }
        return INSTANCE;
    }



    /**
     * generate token
     */
    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt((Date) claims.get(CLAIM_KEY_CREATED))
                .setExpiration(generateExpirationDate())
                .setNotBefore(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    //generate token for
    private String generateToken(Map<String, Object> claims, boolean isAdmin) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(generateExpirationDate(isAdmin))
                .setNotBefore(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * get user from token
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("[ERROR] get user from token failed");
        }
        return claims;
    }

    /**
     * generate expiration date
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    private Date generateExpirationDate(boolean isAdmin) {
        return new Date(System.currentTimeMillis() + expiration * 2 * 1000);
    }

    /**
     * get username from token
     */
    public String getUserIDFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
//             username= (String) claims.get("sub");

        } catch (Exception e) {
            username = null;
            throw new RuntimeException("Invalid token");
        }
        return username;
    }

    public Date getIatFromToken(String token) {
        Date iat;
        try {
            Claims claims = getClaimsFromToken(token);
            iat = claims.getIssuedAt();
        } catch (Exception e) {
            iat = null;
            throw new RuntimeException("Invalid token");
        }
        return iat;
    }

    /**
     * get If the user is a driver from token
     */
    public Boolean getIsDriverFromToken(String token) {
        Boolean isDriver;
        try {
            Claims claims = getClaimsFromToken(token);
            isDriver = (Boolean) claims.get(CLAIM_KEY_ROLE).equals("driver");
        } catch (Exception e) {
            isDriver = null;
            throw new RuntimeException("Invalid token");
        }
        return isDriver;
    }




    /**
     *  judge if token is valid
     */
    public boolean validateToken(String token, LoginUser loginUser) {
        String userID = getUserIDFromToken(token);
        return userID.equals(loginUser.getId()) && !isTokenExpired(token);
    }

    /**
     * judge if token is expired
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate.before(new Date());
    }

    /**
     * get expired date from token
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * generate token for userID
     */
    public String generateToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_ID, userId);
        claims.put(CLAIM_KEY_ROLE, true);
        return generateToken(claims);
    }

    public String generateToken(LoginUser user, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_ID, user.getId());
        claims.put(CLAIM_KEY_CREATED, new Date());
        claims.put(CLAIM_KEY_ROLE, isAdmin);
        return generateToken(claims);
    }


    public String generateToken(LoginUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_ID, user.getId());
        claims.put(CLAIM_KEY_CREATED, user.getLastLogin());
        claims.put(CLAIM_KEY_ROLE, user.getRole());
        return generateToken(claims);
    }

    /**
     * judge if token can be refreshed
     */
    public boolean canRefresh(String token) {
        return !isTokenExpired(token);
    }

    /**
     * refresh token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

}