package br.com.fiap.esg.mobilidade_sustentavel.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private TokenService tokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;
    
    @Mock
    private Usuario usuario;

    private final String testSecret = "5ccd07cf9c8edec899ca60624d24bbfda9e95b7a516387eb3468a25be799a752770006e9566d7c0cdb667a7cd63e45f21a87ddfab7e64b5dc27e528f33eaac1b";
    private final long testExpirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "jwtSecretString", testSecret);
        ReflectionTestUtils.setField(tokenService, "jwtExpirationMs", testExpirationMs);
    }

    @Test
    void generateToken_fromAuthentication_shouldReturnValidToken() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser@example.com");

        String token = tokenService.generateToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("testuser@example.com", tokenService.getUsernameFromToken(token));
    }
    
    @Test
    void generateToken_fromUsuario_shouldReturnValidToken() {
        when(usuario.getEmail()).thenReturn("usuario@example.com");

        String token = tokenService.generateToken(usuario);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("usuario@example.com", tokenService.getUsernameFromToken(token));
    }

    @Test
    void validateToken_withValidTokenAndMatchingUserDetails_shouldReturnTrue() {
        when(userDetails.getUsername()).thenReturn("testuser@example.com");
        // Generate a token that will be used for validation
        String token = Jwts.builder()
            .subject("testuser@example.com")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + testExpirationMs))
            .signWith(ReflectionTestUtils.invokeMethod(tokenService, "getSigningKey"))
            .compact();

        assertTrue(tokenService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_withValidTokenAndNonMatchingUserDetails_shouldReturnFalse() {
        when(userDetails.getUsername()).thenReturn("anotheruser@example.com");
        // Generate a token for "testuser@example.com"
        String token = Jwts.builder()
            .subject("testuser@example.com")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + testExpirationMs))
            .signWith(ReflectionTestUtils.invokeMethod(tokenService, "getSigningKey"))
            .compact();

        assertFalse(tokenService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalse() {
        // Generate an expired token
        String token = Jwts.builder()
            .subject("testuser@example.com")
            .issuedAt(new Date(System.currentTimeMillis() - testExpirationMs * 2))
            .expiration(new Date(System.currentTimeMillis() - testExpirationMs))
            .signWith(ReflectionTestUtils.invokeMethod(tokenService, "getSigningKey"))
            .compact();

        assertFalse(tokenService.validateToken(token, userDetails));
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String expectedUsername = "user@test.com";
        String token = Jwts.builder()
            .subject(expectedUsername)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + testExpirationMs))
            .signWith(ReflectionTestUtils.invokeMethod(tokenService, "getSigningKey"))
            .compact();

        String actualUsername = tokenService.getUsernameFromToken(token);
        assertEquals(expectedUsername, actualUsername);
    }
    
    @Test
    void getExpirationDateFromToken_shouldReturnCorrectDate() {
        Date expectedExpirationDate = new Date(System.currentTimeMillis() + testExpirationMs);
        // Allow for a small delta in timestamp comparisons due to execution time
        long delta = 1000; // 1 second

        String token = Jwts.builder()
            .subject("user@test.com")
            .issuedAt(new Date())
            .expiration(expectedExpirationDate)
            .signWith(ReflectionTestUtils.invokeMethod(tokenService, "getSigningKey"))
            .compact();

        Date actualExpirationDate = tokenService.getExpirationDateFromToken(token);
        // Check if the actual date is within the delta of the expected date
        assertTrue(Math.abs(expectedExpirationDate.getTime() - actualExpirationDate.getTime()) < delta);
    }
} 