package si.um.feri.soa.services;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private static final String SECRET_ENV = getSecretFromEnv();
    private static final long CLOCK_SKEW_SECONDS = 60; // Allow 60 seconds clock skew tolerance

    static {
        if (SECRET_ENV != null && !SECRET_ENV.isEmpty()) {
            logger.info("JWT_SECRET_KEY loaded successfully (length: {})", SECRET_ENV.length());
            if (SECRET_ENV.equals("your-secret-key-change-in-production")) {
                logger.info("JWT_SECRET_KEY is using default value: 'your-secret-key-change-in-production'");
            } else {
                String preview = SECRET_ENV.length() > 6
                        ? SECRET_ENV.substring(0, 3) + "..." + SECRET_ENV.substring(SECRET_ENV.length() - 3)
                        : "***";
                logger.info("JWT_SECRET_KEY preview: {}", preview);
            }
        } else {
            logger.warn("JWT_SECRET_KEY is not set! Using default value.");
        }
    }

    private static String getSecretFromEnv() {
        String secret = System.getenv("JWT_SECRET_KEY");

        if (secret == null || secret.isEmpty()) {
            try {
                Dotenv dotenv = Dotenv.configure()
                        .ignoreIfMissing()
                        .load();
                secret = dotenv.get("JWT_SECRET_KEY");
            } catch (Exception e) {
            }
        }

        if (secret == null || secret.isEmpty()) {
            secret = "your-secret-key-change-in-production";
        }

        return secret;
    }

    private SecretKey getSigningKey(String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("JWT secret key is not set");
        }

        byte[] secretBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);

        String username = claims.get("username", String.class);
        if (username != null && !username.isEmpty()) {
            return username;
        }

        Object userIdObj = claims.get("user_id");
        if (userIdObj != null) {
            return userIdObj.toString();
        }

        return claims.getSubject();
    }

    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);

        Object userIdObj = claims.get("user_id");
        if (userIdObj != null) {
            return userIdObj.toString();
        }

        return claims.getSubject();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        Exception lastException = null;

        String secretKey = SECRET_ENV;
        if (secretKey == null || secretKey.isEmpty()) {
            secretKey = "your-secret-key-change-in-production";
            logger.warn("JWT_SECRET_KEY was null, using default value");
        }

        logger.debug("Validating token with secret key (length: {})", secretKey.length());

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secretKey))
                    .setAllowedClockSkewSeconds(CLOCK_SKEW_SECONDS)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.debug("Token validated successfully with primary key");
            return claims;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("JWT signature mismatch with primary key. Secret key length: {}, value: '{}'",
                    secretKey.length(),
                    secretKey.equals("your-secret-key-change-in-production") ? "your-secret-key-change-in-production"
                            : "***");
            lastException = e;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            throw new RuntimeException("JWT token expired: " + e.getMessage());
        } catch (Exception e) {
            logger.error("JWT validation error with primary key: {}", e.getMessage());
            lastException = e;
        }

        String errorMsg = "Invalid JWT token: "
                + (lastException != null ? lastException.getMessage() : "unable to verify signature");
        logger.error(errorMsg);
        throw new RuntimeException(errorMsg);
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Boolean validateToken(String token) {
        return validateToken(token, "access");
    }

    public Boolean validateToken(String token, String tokenType) {
        try {
            Claims claims = extractAllClaims(token);

            if (tokenType != null) {
                String type = claims.get("type", String.class);
                if (type == null || !type.equals(tokenType)) {
                    return false;
                }
            }

            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> validateTokenWithDetails(String token) {
        Map<String, Object> result = new HashMap<>();

        try {
            Claims claims = extractAllClaims(token);

            boolean expired = isTokenExpired(token);
            if (expired) {
                result.put("valid", false);
                result.put("error", "Token expired");
                result.put("expiration", claims.getExpiration());
                return result;
            }

            String type = claims.get("type", String.class);
            if (type == null || !type.equals("access")) {
                result.put("valid", false);
                result.put("error", "Invalid token type. Expected 'access', got: " + type);
                return result;
            }

            result.put("valid", true);
            result.put("username", claims.get("username", String.class));
            result.put("user_id", claims.getSubject());
            result.put("type", type);
            result.put("expiration", claims.getExpiration());
            result.put("issued_at", claims.getIssuedAt());

        } catch (io.jsonwebtoken.security.SignatureException e) {
            result.put("valid", false);
            result.put("error",
                    "JWT signature does not match. The secret key used to sign this token is different from the one configured in this application.");
            result.put("error_type", "SIGNATURE_MISMATCH");
            logger.error("Token signature mismatch: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            result.put("valid", false);
            result.put("error", "Token expired");
            result.put("error_type", "EXPIRED");
            result.put("expiration", e.getClaims().getExpiration());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            result.put("valid", false);
            result.put("error", "Malformed JWT token: " + e.getMessage());
            result.put("error_type", "MALFORMED");
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", "Token validation failed: " + e.getMessage());
            result.put("error_type", "VALIDATION_ERROR");
            logger.error("Token validation error: {}", e.getMessage());
        }

        return result;
    }

    public Map<String, Object> getSecretKeyInfo() {
        Map<String, Object> info = new HashMap<>();
        if (SECRET_ENV != null && !SECRET_ENV.isEmpty()) {
            info.put("configured", true);
            info.put("length", SECRET_ENV.length());
            info.put("is_default", SECRET_ENV.equals("your-secret-key-change-in-production"));
            if (SECRET_ENV.length() > 6) {
                info.put("preview", SECRET_ENV.substring(0, 3) + "..." + SECRET_ENV.substring(SECRET_ENV.length() - 3));
            } else {
                info.put("preview", "***");
            }
        } else {
            info.put("configured", false);
            info.put("error", "JWT_SECRET_KEY is not set");
        }
        return info;
    }
}
