package br.com.fiap.esg.mobilidade_sustentavel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.PageImpl;
import org.mockito.ArgumentMatchers;

import br.com.fiap.esg.mobilidade_sustentavel.dto.UsuarioRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.UsuarioResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.exception.EmailJaCadastradoException;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.repository.UsuarioRepository;
import br.com.fiap.esg.mobilidade_sustentavel.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRequestDto usuarioRequestDto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioRequestDto = new UsuarioRequestDto();
        usuarioRequestDto.setNome("Test User");
        usuarioRequestDto.setEmail("test@example.com");
        usuarioRequestDto.setSenha("password123");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome(usuarioRequestDto.getNome());
        usuario.setEmail(usuarioRequestDto.getEmail());
        usuario.setSenha("encodedPassword");
        usuario.setRoles("ROLE_USER");
        usuario.setEnabled(true);
        usuario.setFailedLoginAttempts(0);
        usuario.setAccountLockedUntil(null);
        usuario.setAccountExpirationDate(null);
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso quando e-mail não existe")
    void criarUsuario_quandoEmailNaoExiste_deveRetornarUsuarioResponseDto() {
        // Arrange
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(1L);
        usuarioSalvo.setNome(usuarioRequestDto.getNome());
        usuarioSalvo.setEmail(usuarioRequestDto.getEmail());
        usuarioSalvo.setSenha("encodedPassword");
        usuarioSalvo.setRoles("ROLE_USER");
        usuarioSalvo.setEnabled(true);

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        // Act
        UsuarioResponseDto result = usuarioService.criarUsuario(usuarioRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(usuarioSalvo.getId(), result.getId());
        assertEquals(usuarioRequestDto.getNome(), result.getNome());
        assertEquals(usuarioRequestDto.getEmail(), result.getEmail());
        
        verify(usuarioRepository, times(1)).findByEmail(usuarioRequestDto.getEmail());
        verify(passwordEncoder, times(1)).encode(usuarioRequestDto.getSenha());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar EmailJaCadastradoException quando e-mail já existe")
    void criarUsuario_quandoEmailJaExiste_deveLancarEmailJaCadastradoException() {
        // Arrange
        when(usuarioRepository.findByEmail(usuarioRequestDto.getEmail())).thenReturn(Optional.of(new Usuario()));

        // Act & Assert
        EmailJaCadastradoException exception = assertThrows(EmailJaCadastradoException.class, () -> {
            usuarioService.criarUsuario(usuarioRequestDto);
        });
        
        assertEquals("O email '" + usuarioRequestDto.getEmail() + "' já está cadastrado.", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve retornar UsuarioResponseDto quando buscarUsuarioPorId encontra usuário")
    void buscarUsuarioPorId_quandoUsuarioExiste_deveRetornarOptionalComUsuarioResponseDto() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        Optional<UsuarioResponseDto> result = usuarioService.buscarUsuarioPorId(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(usuario.getNome(), result.get().getNome());
        assertEquals(usuario.getEmail(), result.get().getEmail());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando buscarUsuarioPorId não encontra usuário")
    void buscarUsuarioPorId_quandoUsuarioNaoExiste_deveRetornarOptionalVazio() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<UsuarioResponseDto> result = usuarioService.buscarUsuarioPorId(1L);

        // Assert
        assertTrue(result.isEmpty());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar UsuarioResponseDto quando getUsuarioByIdOrThrow encontra usuário")
    void getUsuarioByIdOrThrow_quandoUsuarioExiste_deveRetornarUsuarioResponseDto() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        UsuarioResponseDto result = usuarioService.getUsuarioByIdOrThrow(1L);

        // Assert
        assertNotNull(result);
        assertEquals(usuario.getNome(), result.getNome());
        assertEquals(usuario.getEmail(), result.getEmail());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando getUsuarioByIdOrThrow não encontra usuário")
    void getUsuarioByIdOrThrow_quandoUsuarioNaoExiste_deveLancarResourceNotFoundException() {
        // Arrange
        Long usuarioId = 1L;
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.getUsuarioByIdOrThrow(usuarioId);
        });
        
        assertEquals(String.format("Usuario não encontrado com id : '%s'", usuarioId), exception.getMessage());
        verify(usuarioRepository, times(1)).findById(usuarioId);
    }

    @Test
    @DisplayName("Deve retornar UsuarioResponseDto quando buscarUsuarioPorEmail encontra usuário")
    void buscarUsuarioPorEmail_quandoUsuarioExiste_deveRetornarOptionalComUsuarioResponseDto() {
        // Arrange
        String emailExistente = "test@example.com";
        when(usuarioRepository.findByEmail(emailExistente)).thenReturn(Optional.of(usuario)); // Reutilizando 'usuario' do setUp

        // Act
        Optional<UsuarioResponseDto> result = usuarioService.buscarUsuarioPorEmail(emailExistente);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(usuario.getNome(), result.get().getNome());
        assertEquals(usuario.getEmail(), result.get().getEmail());
        verify(usuarioRepository, times(1)).findByEmail(emailExistente);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando buscarUsuarioPorEmail não encontra usuário")
    void buscarUsuarioPorEmail_quandoUsuarioNaoExiste_deveRetornarOptionalVazio() {
        // Arrange
        String emailNaoExistente = "naoexiste@example.com";
        when(usuarioRepository.findByEmail(emailNaoExistente)).thenReturn(Optional.empty());

        // Act
        Optional<UsuarioResponseDto> result = usuarioService.buscarUsuarioPorEmail(emailNaoExistente);

        // Assert
        assertTrue(result.isEmpty());
        verify(usuarioRepository, times(1)).findByEmail(emailNaoExistente);
    }

    @Test
    @DisplayName("Deve retornar lista de UsuarioResponseDto quando listarTodosUsuariosSemPaginacao com usuários existentes")
    void listarTodosUsuariosSemPaginacao_quandoExistemUsuarios_deveRetornarListaDeResponseDto() {
        // Arrange
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setNome("Outro User");
        outroUsuario.setEmail("outro@example.com");
        outroUsuario.setSenha("encodedPassword2");
        outroUsuario.setRoles("ROLE_USER");
        outroUsuario.setEnabled(true);

        when(usuarioRepository.findAll()).thenReturn(java.util.List.of(usuario, outroUsuario));

        // Act
        java.util.List<UsuarioResponseDto> result = usuarioService.listarTodosUsuariosSemPaginacao();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(usuario.getNome(), result.get(0).getNome());
        assertEquals(outroUsuario.getNome(), result.get(1).getNome());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando listarTodosUsuariosSemPaginacao sem usuários existentes")
    void listarTodosUsuariosSemPaginacao_quandoNaoExistemUsuarios_deveRetornarListaVazia() {
        // Arrange
        when(usuarioRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        // Act
        java.util.List<UsuarioResponseDto> result = usuarioService.listarTodosUsuariosSemPaginacao();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso sem alterar e-mail ou senha")
    void atualizarUsuario_semMudancaDeEmailOuSenha_deveRetornarUsuarioAtualizado() {
        // Arrange
        Long usuarioId = 1L;
        UsuarioRequestDto requestDtoUpdate = new UsuarioRequestDto();
        requestDtoUpdate.setNome("Updated Test User");
        requestDtoUpdate.setEmail(usuario.getEmail()); // Mesmo e-mail
        // Nenhuma senha no requestDtoUpdate significa que não deve ser alterada

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retorna o usuário que foi "salvo"

        // Act
        UsuarioResponseDto result = usuarioService.atualizarUsuario(usuarioId, requestDtoUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(requestDtoUpdate.getNome(), result.getNome());
        assertEquals(usuario.getEmail(), result.getEmail()); // Email não mudou
        verify(usuarioRepository, times(1)).findById(usuarioId);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString()); // Senha não deve ser codificada
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso e alterar a senha")
    void atualizarUsuario_comMudancaDeSenha_deveRetornarUsuarioAtualizadoComSenhaNova() {
        // Arrange
        Long usuarioId = 1L;
        UsuarioRequestDto requestDtoUpdate = new UsuarioRequestDto();
        requestDtoUpdate.setNome("Test User");
        requestDtoUpdate.setEmail(usuario.getEmail());
        requestDtoUpdate.setSenha("novaSenha123");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("encodedNovaSenha");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario userArg = invocation.getArgument(0);
            assertEquals("encodedNovaSenha", userArg.getSenha()); // Verifica se a senha foi atualizada antes de salvar
            return userArg;
        });

        // Act
        UsuarioResponseDto result = usuarioService.atualizarUsuario(usuarioId, requestDtoUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(usuario.getEmail(), result.getEmail());
        verify(usuarioRepository, times(1)).findById(usuarioId);
        verify(passwordEncoder, times(1)).encode("novaSenha123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso e alterar o e-mail para um não existente")
    void atualizarUsuario_comMudancaDeEmailParaNaoExistente_deveRetornarUsuarioAtualizado() {
        // Arrange
        Long usuarioId = 1L;
        String novoEmail = "novoemail@example.com";
        UsuarioRequestDto requestDtoUpdate = new UsuarioRequestDto();
        requestDtoUpdate.setNome("Updated Name");
        requestDtoUpdate.setEmail(novoEmail);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail(novoEmail)).thenReturn(Optional.empty()); // Novo e-mail não existe
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UsuarioResponseDto result = usuarioService.atualizarUsuario(usuarioId, requestDtoUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(novoEmail, result.getEmail());
        assertEquals(requestDtoUpdate.getNome(), result.getNome());
        verify(usuarioRepository, times(1)).findById(usuarioId);
        verify(usuarioRepository, times(1)).findByEmail(novoEmail);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar EmailJaCadastradoException ao tentar atualizar para e-mail de outro usuário")
    void atualizarUsuario_comMudancaDeEmailParaEmailExistenteEmOutroUsuario_deveLancarEmailJaCadastradoException() {
        // Arrange
        Long usuarioId = 1L;
        String emailExistenteOutroUsuario = "existente@outro.com";
        UsuarioRequestDto requestDtoUpdate = new UsuarioRequestDto();
        requestDtoUpdate.setNome("Any Name");
        requestDtoUpdate.setEmail(emailExistenteOutroUsuario);

        Usuario outroUsuarioComEmail = new Usuario();
        outroUsuarioComEmail.setId(2L); // ID diferente
        outroUsuarioComEmail.setEmail(emailExistenteOutroUsuario);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario)); // Usuário a ser atualizado
        when(usuarioRepository.findByEmail(emailExistenteOutroUsuario)).thenReturn(Optional.of(outroUsuarioComEmail));

        // Act & Assert
        EmailJaCadastradoException exception = assertThrows(EmailJaCadastradoException.class, () -> {
            usuarioService.atualizarUsuario(usuarioId, requestDtoUpdate);
        });

        assertEquals("O email '" + emailExistenteOutroUsuario + "' já está cadastrado.", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(usuarioId);
        verify(usuarioRepository, times(1)).findByEmail(emailExistenteOutroUsuario);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar usuário inexistente")
    void atualizarUsuario_quandoUsuarioNaoExiste_deveLancarResourceNotFoundException() {
        // Arrange
        Long usuarioIdInexistente = 99L;
        UsuarioRequestDto requestDtoUpdate = new UsuarioRequestDto();
        requestDtoUpdate.setNome("Any Name");
        requestDtoUpdate.setEmail("any@email.com");

        when(usuarioRepository.findById(usuarioIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.atualizarUsuario(usuarioIdInexistente, requestDtoUpdate);
        });

        assertEquals(String.format("Usuario não encontrado com id : '%s'", usuarioIdInexistente), exception.getMessage());
        verify(usuarioRepository, times(1)).findById(usuarioIdInexistente);
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso quando usuário existe")
    void deletarUsuario_quandoUsuarioExiste_deveChamarDeleteById() {
        // Arrange
        Long usuarioId = 1L;
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(usuarioId); // Configura o mock para não fazer nada em deleteById

        // Act
        assertDoesNotThrow(() -> usuarioService.deletarUsuario(usuarioId));

        // Assert
        verify(usuarioRepository, times(1)).existsById(usuarioId);
        verify(usuarioRepository, times(1)).deleteById(usuarioId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar usuário inexistente")
    void deletarUsuario_quandoUsuarioNaoExiste_deveLancarResourceNotFoundException() {
        // Arrange
        Long usuarioIdInexistente = 99L;
        when(usuarioRepository.existsById(usuarioIdInexistente)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            usuarioService.deletarUsuario(usuarioIdInexistente);
        });

        assertEquals(String.format("Usuario não encontrado com id : '%s'", usuarioIdInexistente), exception.getMessage());
        verify(usuarioRepository, times(1)).existsById(usuarioIdInexistente);
        verify(usuarioRepository, never()).deleteById(anyLong());
    }

    @Mock
    private Pageable pageable; // Mock para Pageable

    @Mock
    private Page<Usuario> usuarioPage; // Mock para Page<Usuario>

    @Test
    @DisplayName("Deve retornar Page de UsuarioResponseDto quando listarUsuarios sem filtros")
    void listarUsuarios_semFiltros_deveRetornarPageDeResponseDto() {
        // Arrange
        when(usuarioRepository.findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable))).thenReturn(usuarioPage);
        when(usuarioPage.map(any())).thenReturn(Page.empty());

        // Act
        Page<UsuarioResponseDto> result = usuarioService.listarUsuarios(pageable, null, null);

        // Assert
        assertNotNull(result);
        verify(usuarioRepository, times(1)).findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable));
        verify(usuarioPage, times(1)).map(any());
    }

    @Test
    @DisplayName("Deve retornar Page de UsuarioResponseDto quando listarUsuarios com filtro de nome")
    void listarUsuarios_comFiltroDeNome_deveRetornarPageDeResponseDto() {
        // Arrange
        when(usuarioRepository.findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable))).thenReturn(usuarioPage);
        when(usuarioPage.map(any())).thenReturn(new PageImpl<>(java.util.List.of(new UsuarioResponseDto(1L, "Test User", "test@example.com")))); 

        // Act
        Page<UsuarioResponseDto> result = usuarioService.listarUsuarios(pageable, "Test", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test User", result.getContent().get(0).getNome());
        verify(usuarioRepository, times(1)).findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable));
    }

    @Test
    @DisplayName("Deve retornar Page de UsuarioResponseDto quando listarUsuarios com filtro de email")
    void listarUsuarios_comFiltroDeEmail_deveRetornarPageDeResponseDto() {
        // Arrange
        when(usuarioRepository.findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable))).thenReturn(usuarioPage);
        when(usuarioPage.map(any())).thenReturn(Page.empty());

        // Act
        Page<UsuarioResponseDto> result = usuarioService.listarUsuarios(pageable, null, "example.com");

        // Assert
        assertNotNull(result);
        verify(usuarioRepository, times(1)).findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable));
    }

    @Test
    @DisplayName("Deve retornar Page de UsuarioResponseDto quando listarUsuarios com ambos os filtros")
    void listarUsuarios_comAmbosFiltros_deveRetornarPageDeResponseDto() {
        // Arrange
        when(usuarioRepository.findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable))).thenReturn(usuarioPage);
        when(usuarioPage.map(any())).thenReturn(Page.empty());

        // Act
        Page<UsuarioResponseDto> result = usuarioService.listarUsuarios(pageable, "Test", "example.com");

        // Assert
        assertNotNull(result);
        verify(usuarioRepository, times(1)).findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable));
    }

    @Test
    @DisplayName("Deve retornar Page vazia de UsuarioResponseDto quando repositório retorna Page vazia")
    void listarUsuarios_quandoRepositorioRetornaPaginaVazia_deveRetornarPageVazia() {
        // Arrange
        when(usuarioRepository.findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable))).thenReturn(Page.empty());

        // Act
        Page<UsuarioResponseDto> result = usuarioService.listarUsuarios(pageable, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepository, times(1)).findAll(ArgumentMatchers.<Specification<Usuario>>any(), eq(pageable));
    }
} 