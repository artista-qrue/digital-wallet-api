package com.wallet.api.security;

import com.wallet.api.entity.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

@Component
public class JwtTokenUtil {

    private String secret;
    private int jwtExpirationInMs;
    private SecretKey secretKey;

    @Autowired
    public JwtTokenUtil(
            @Value("${jwt.secret:walletApiSecretKey2023ThisIsAVeryLongSecretKeyForJwtSecurity}") String secret, 
            @Value("${jwt.expiration:86400000}") int jwtExpirationInMs) {
        this.secret = secret;
        this.jwtExpirationInMs = jwtExpirationInMs;
        // Generate a secure key for HS512
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    public String generateToken(CustomerPrincipal userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("isEmployee", userDetails.isEmployee());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateToken(Long customerId, Boolean isEmployee) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customerId", customerId);
        claims.put("isEmployee", isEmployee);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(customerId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, CustomerPrincipal userDetails) {
        try {
            // Check if token is expired
            if (isTokenExpired(token)) {
                return false;
            }
            
            // Get customerId from token
            Long tokenCustomerId = getCustomerId(token);
            
            // Compare with userDetails
            return tokenCustomerId.equals(userDetails.getCustomerId());
        } catch (Exception e) {
            return false;
        }
    }
    
    public Boolean isEmployee(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("isEmployee", Boolean.class);
    }
    
    public Long getCustomerId(String token) {
        Claims claims = extractAllClaims(token);
        
        // First, try to get the customerId directly from claims (for tokens created by direct-token endpoint)
        if (claims.containsKey("customerId")) {
            return ((Number) claims.get("customerId")).longValue();
        }
        
        // Fallback to using the subject (for tokens created by the normal login flow)
        String username = extractUsername(token);
        return Long.parseLong(username);
    }
} 