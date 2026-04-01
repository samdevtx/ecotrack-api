package br.com.fiap.esg.mobilidade_sustentavel.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private Usuario usuario;
    private final String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Test User");
        usuario.setEmail(userEmail);
        usuario.setSenha("encodedPassword");
        usuario.setRoles("ROLE_USER,ROLE_ADMIN");
        usuario.setEnabled(true);
        usuario.setFailedLoginAttempts(0);
        usuario.setAccountLockedUntil(null);
        usuario.setAccountExpirationDate(null);
    }

    @Test
    @DisplayName("Deve retornar UserDetails quando usuário é encontrado pelo email")
    void loadUserByUsername_quandoUsuarioExiste_deveRetornarUserDetails() {
        // Arrange
        when(usuarioRepository.findByEmail(userEmail)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // Assert
        assertNotNull(userDetails);
        assertEquals(usuario.getEmail(), userDetails.getUsername());
        assertEquals(usuario.getSenha(), userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired()); // Assuming this is always true as per Usuario model
        assertNotNull(userDetails.getAuthorities());
        assertEquals(2, userDetails.getAuthorities().size()); // ROLE_USER, ROLE_ADMIN
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));
        
        verify(usuarioRepository, times(1)).findByEmail(userEmail);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não é encontrado pelo email")
    void loadUserByUsername_quandoUsuarioNaoExiste_deveLancarUsernameNotFoundException() {
        // Arrange
        String nonExistentEmail = "naoexiste@example.com";
        when(usuarioRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistentEmail);
        });

        assertEquals("Usuário não encontrado com o email: " + nonExistentEmail, exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Deve retornar UserDetails corretamente quando conta está desabilitada")
    void loadUserByUsername_quandoUsuarioDesabilitado_deveRefletirNoUserDetails() {
        // Arrange
        usuario.setEnabled(false);
        when(usuarioRepository.findByEmail(userEmail)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // Assert
        assertFalse(userDetails.isEnabled());
    }

    @Test
    @DisplayName("Deve retornar UserDetails corretamente quando conta está bloqueada")
    void loadUserByUsername_quandoUsuarioBloqueado_deveRefletirNoUserDetails() {
        // Arrange
        usuario.setAccountLockedUntil(LocalDateTime.now().plusDays(1));
        when(usuarioRepository.findByEmail(userEmail)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // Assert
        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    @DisplayName("Deve retornar UserDetails corretamente quando conta está expirada")
    void loadUserByUsername_quandoUsuarioExpirado_deveRefletirNoUserDetails() {
        // Arrange
        usuario.setAccountExpirationDate(LocalDate.now().minusDays(1));
        when(usuarioRepository.findByEmail(userEmail)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // Assert
        assertFalse(userDetails.isAccountNonExpired());
    }
} 