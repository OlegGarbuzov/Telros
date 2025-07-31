package com.telros.telros.security.jwt;

import com.telros.telros.security.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Утилитарный класс для работы с JWT токенами
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Генерирует JWT токен на основе данных аутентификации
     *
     * @param authentication данные аутентификации
     * @return JWT токен
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Получает ключ для подписи JWT токена
     *
     * @return ключ
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Извлекает имя пользователя из JWT токена
     *
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Проверяет валидность JWT токена
     *
     * @param authToken JWT токен
     * @return true если токен валиден, иначе false
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Некорректный JWT токен: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT токен истек: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT токен не поддерживается: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims строка пуста: {}", e.getMessage());
        }

        return false;
    }
}