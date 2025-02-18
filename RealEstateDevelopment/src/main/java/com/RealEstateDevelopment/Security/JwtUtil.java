package com.RealEstateDevelopment.Security;

import com.RealEstateDevelopment.Repository.BlacklistedTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512); // This generates a 512-bit key
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public JwtUtil(BlacklistedTokenRepository blacklistedTokenRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    // Generate Token with role
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setClaims(Map.of("role", role)) // Include role in token
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS512) // Use HS512 algorithm with the correct key size
                .compact();
    }

    // Extract Username from Token
    public String extractUsername(String token) {
        token = token.replaceAll("\\s", ""); // Remove spaces
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Token Format");
        }
    }

    // Extract Role from Token
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Extract Expiration Date
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //************ try new methods ********************************

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token); // If this line fails, it means the token is invalid
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Invalid or expired token
        }
    }

    // Parse the token and return claims
    public Claims parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Log the expiration date for debugging
        System.out.println("Token Expiration: " + claims.getExpiration());

        return claims;
    }

    // Extract loginId from token claims
    private String getLoginIdFromToken(String token) {
        Claims claims = parseToken(token); // Use parseToken for consistent claims extraction
        return claims.getSubject();
    }

    // Extract userId directly from token claims
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token); // Use parseToken for consistent claims extraction
        return claims.get("userId", Long.class);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        // Check if token is blacklisted
        if (blacklistedTokenRepository.findByToken(token).isPresent()) {
            return false;
        }

        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

}
