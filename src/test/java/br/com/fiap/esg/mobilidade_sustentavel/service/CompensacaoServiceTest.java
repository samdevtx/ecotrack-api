package br.com.fiap.esg.mobilidade_sustentavel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException; // For authorization tests

import br.com.fiap.esg.mobilidade_sustentavel.dto.CompensacaoRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.CompensacaoResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.exception.ResourceNotFoundException;
import br.com.fiap.esg.mobilidade_sustentavel.model.Compensacao;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.repository.CompensacaoRepository;
import br.com.fiap.esg.mobilidade_sustentavel.repository.UsuarioRepository; // May be needed if service interacts with it beyond Usuario object

@ExtendWith(MockitoExtension.class)
class CompensacaoServiceTest {

    @Mock
    private CompensacaoRepository compensacaoRepository;

    // Mock UsuarioRepository if CompensacaoService directly interacts with it
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CompensacaoService compensacaoService;

    private Usuario usuarioLogado;
    private CompensacaoRequestDto compensacaoRequestDto;
    private Compensacao compensacaoSalva;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario();
        usuarioLogado.setId(1L);
        usuarioLogado.setNome("Test User");
        usuarioLogado.setEmail("test@example.com");
        usuarioLogado.setRoles("ROLE_USER");

        compensacaoRequestDto = new CompensacaoRequestDto(
            "PLANTIO_ARVORES",
            BigDecimal.valueOf(5.0)
        );

        compensacaoSalva = new Compensacao();
        compensacaoSalva.setId(1L);
        compensacaoSalva.setUsuario(usuarioLogado);
        compensacaoSalva.setTipo(compensacaoRequestDto.tipo());
        compensacaoSalva.setQuantidade(compensacaoRequestDto.quantidade());
        compensacaoSalva.setDataRegistro(LocalDateTime.now());
    }

    // Placeholder for future tests
    @Test
    @DisplayName("Placeholder Test")
    void placeholder() {
        assertTrue(true);
    }

    @Test
    @DisplayName("Deve registrar compensação com sucesso")
    void registrarCompensacao_dadosValidos_deveRetornarCompensacaoResponseDto() {
        // Arrange
        when(compensacaoRepository.save(any(Compensacao.class))).thenReturn(compensacaoSalva);

        // Act
        CompensacaoResponseDto result = compensacaoService.registrarCompensacao(compensacaoRequestDto, usuarioLogado);

        // Assert
        assertNotNull(result);
        assertEquals(compensacaoSalva.getId(), result.id());
        assertEquals(compensacaoRequestDto.tipo(), result.tipo());
        assertEquals(compensacaoRequestDto.quantidade().doubleValue(), result.quantidade().doubleValue(), 0.001);
        assertNotNull(result.dataRegistro());
        assertEquals(usuarioLogado.getId(), result.usuarioId());

        verify(compensacaoRepository, times(1)).save(any(Compensacao.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao registrar compensação com usuário nulo")
    void registrarCompensacao_comUsuarioNulo_deveLancarIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            compensacaoService.registrarCompensacao(compensacaoRequestDto, null);
        });
        assertEquals("Usuário autenticado inválido ou não persistido.", exception.getMessage());
        verify(compensacaoRepository, never()).save(any(Compensacao.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao registrar compensação com usuário sem ID")
    void registrarCompensacao_comUsuarioSemId_deveLancarIllegalArgumentException() {
        // Arrange
        Usuario usuarioSemId = new Usuario();
        usuarioSemId.setEmail("semid@example.com");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            compensacaoService.registrarCompensacao(compensacaoRequestDto, usuarioSemId);
        });
        assertEquals("Usuário autenticado inválido ou não persistido.", exception.getMessage());
        verify(compensacaoRepository, never()).save(any(Compensacao.class));
    }

    @Test
    @DisplayName("Deve retornar CompensacaoResponseDto quando buscarCompensacaoPorId encontra compensação")
    void buscarCompensacaoPorId_quandoCompensacaoExiste_deveRetornarOptionalComCompensacaoResponseDto() {
        // Arrange
        Long compensacaoId = 1L;
        // compensacaoSalva from setUp
        when(compensacaoRepository.findById(compensacaoId)).thenReturn(Optional.of(compensacaoSalva));

        // Act
        Optional<CompensacaoResponseDto> result = compensacaoService.buscarCompensacaoPorId(compensacaoId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(compensacaoSalva.getId(), result.get().id());
        assertEquals(compensacaoSalva.getTipo(), result.get().tipo());
        verify(compensacaoRepository, times(1)).findById(compensacaoId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando buscarCompensacaoPorId não encontra compensação")
    void buscarCompensacaoPorId_quandoCompensacaoNaoExiste_deveRetornarOptionalVazio() {
        // Arrange
        Long compensacaoId = 99L;
        when(compensacaoRepository.findById(compensacaoId)).thenReturn(Optional.empty());

        // Act
        Optional<CompensacaoResponseDto> result = compensacaoService.buscarCompensacaoPorId(compensacaoId);

        // Assert
        assertTrue(result.isEmpty());
        verify(compensacaoRepository, times(1)).findById(compensacaoId);
    }

    @Test
    @DisplayName("Deve retornar CompensacaoResponseDto quando getCompensacaoByIdOrThrow encontra compensação")
    void getCompensacaoByIdOrThrow_quandoCompensacaoExiste_deveRetornarCompensacaoResponseDto() {
        // Arrange
        Long compensacaoId = 1L;
        when(compensacaoRepository.findById(compensacaoId)).thenReturn(Optional.of(compensacaoSalva));

        // Act
        CompensacaoResponseDto result = compensacaoService.getCompensacaoByIdOrThrow(compensacaoId);

        // Assert
        assertNotNull(result);
        assertEquals(compensacaoSalva.getId(), result.id());
        assertEquals(compensacaoSalva.getTipo(), result.tipo());
        verify(compensacaoRepository, times(1)).findById(compensacaoId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando getCompensacaoByIdOrThrow não encontra compensação")
    void getCompensacaoByIdOrThrow_quandoCompensacaoNaoExiste_deveLancarResourceNotFoundException() {
        // Arrange
        Long compensacaoId = 99L;
        when(compensacaoRepository.findById(compensacaoId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            compensacaoService.getCompensacaoByIdOrThrow(compensacaoId);
        });
        
        assertEquals(String.format("Compensacao não encontrado com id : '%s'", compensacaoId), exception.getMessage());
        verify(compensacaoRepository, times(1)).findById(compensacaoId);
    }

    @Test
    @DisplayName("Deve listar compensações de um usuário específico")
    void listarCompensacoesPorUsuario_quandoUsuarioExisteTemCompensacoes_deveRetornarListaDeCompensacaoResponseDto() {
        // Arrange
        Long usuarioId = usuarioLogado.getId();
        Compensacao c1 = new Compensacao();
        c1.setId(1L); c1.setUsuario(usuarioLogado); c1.setTipo("T1"); c1.setQuantidade(BigDecimal.ONE);
        Compensacao c2 = new Compensacao();
        c2.setId(2L); c2.setUsuario(usuarioLogado); c2.setTipo("T2"); c2.setQuantidade(BigDecimal.TEN);
        List<Compensacao> compensacoesDoUsuario = List.of(c1, c2);

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(compensacaoRepository.findByUsuarioId(usuarioId)).thenReturn(compensacoesDoUsuario);

        // Act
        List<CompensacaoResponseDto> result = compensacaoService.listarCompensacoesPorUsuario(usuarioId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(c1.getId(), result.get(0).id());
        assertEquals(c2.getId(), result.get(1).id());

        verify(usuarioRepository, times(1)).existsById(usuarioId);
        verify(compensacaoRepository, times(1)).findByUsuarioId(usuarioId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário existe mas não tem compensações")
    void listarCompensacoesPorUsuario_quandoUsuarioExisteNaoTemCompensacoes_deveRetornarListaVazia() {
        // Arrange
        Long usuarioId = usuarioLogado.getId();
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(compensacaoRepository.findByUsuarioId(usuarioId)).thenReturn(Collections.emptyList());

        // Act
        List<CompensacaoResponseDto> result = compensacaoService.listarCompensacoesPorUsuario(usuarioId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(usuarioRepository, times(1)).existsById(usuarioId);
        verify(compensacaoRepository, times(1)).findByUsuarioId(usuarioId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao listar compensações de usuário inexistente")
    void listarCompensacoesPorUsuario_quandoUsuarioNaoExiste_deveLancarResourceNotFoundException() {
        // Arrange
        Long usuarioIdInexistente = 999L;
        when(usuarioRepository.existsById(usuarioIdInexistente)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            compensacaoService.listarCompensacoesPorUsuario(usuarioIdInexistente);
        });

        assertEquals(String.format("Usuario não encontrado com id : '%s'", usuarioIdInexistente), exception.getMessage());
        verify(usuarioRepository, times(1)).existsById(usuarioIdInexistente);
        verify(compensacaoRepository, never()).findByUsuarioId(anyLong());
    }

    @Test
    @DisplayName("Deve listar todas as compensações quando existem compensações")
    void listarTodasCompensacoes_quandoExistemCompensacoes_deveRetornarListaDeCompensacaoResponseDto() {
        // Arrange
        Compensacao c1 = new Compensacao(); c1.setId(1L); c1.setTipo("T1");
        Usuario u1 = new Usuario(); u1.setId(1L);
        c1.setUsuario(u1);
        Compensacao c2 = new Compensacao(); c2.setId(2L); c2.setTipo("T2");
        Usuario u2 = new Usuario(); u2.setId(2L);
        c2.setUsuario(u2);
        List<Compensacao> todasAsCompensacoes = List.of(c1, c2);

        when(compensacaoRepository.findAll()).thenReturn(todasAsCompensacoes);

        // Act
        List<CompensacaoResponseDto> result = compensacaoService.listarTodasCompensacoes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(c1.getId(), result.get(0).id());
        assertEquals(c2.getId(), result.get(1).id());

        verify(compensacaoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não existem compensações no sistema")
    void listarTodasCompensacoes_quandoNaoExistemCompensacoes_deveRetornarListaVazia() {
        // Arrange
        when(compensacaoRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<CompensacaoResponseDto> result = compensacaoService.listarTodasCompensacoes();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(compensacaoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar compensação com sucesso")
    void atualizarCompensacao_dadosValidosEUsuarioAutorizado_deveRetornarCompensacaoResponseDtoAtualizada() {
        // Arrange
        Long compensacaoId = compensacaoSalva.getId(); // ID da compensação existente (do setUp)
        // usuarioLogado (do setUp) é o proprietário

        CompensacaoRequestDto requestDtoAtualizacao = new CompensacaoRequestDto(
            "CREDITO_CARBONO", // Tipo mudou
            BigDecimal.valueOf(12.75)  // Quantidade mudou
        );
        
        // Mock para encontrar a compensação existente
        when(compensacaoRepository.findById(compensacaoId)).thenReturn(Optional.of(compensacaoSalva));

        // Mock para o save (capturar a entidade atualizada)
        // Importante: o CompensacaoMapper.updateEntityFromDto ATUALIZA o objeto 'compensacaoSalva' DIRETAMENTE.
        // Então, o 'compensacaoSalva' que é retornado pelo save já estará atualizado.
        when(compensacaoRepository.save(any(Compensacao.class))).thenAnswer(invocation -> {
            Compensacao compensacaoParaSalvar = invocation.getArgument(0);
            // Simula que o save mantém/retorna o mesmo ID
            compensacaoParaSalvar.setId(compensacaoId); 
            // As alterações do DTO já foram aplicadas à 'compensacaoSalva' antes do save ser chamado
            // pelo CompensacaoMapper.updateEntityFromDto.
            // Para garantir que o mock do save retorne a entidade com os dados atualizados pelo mapper:
            assertEquals(requestDtoAtualizacao.tipo(), compensacaoParaSalvar.getTipo());
            assertEquals(requestDtoAtualizacao.quantidade(), compensacaoParaSalvar.getQuantidade());
            return compensacaoParaSalvar; 
        });

        // Act
        CompensacaoResponseDto result = compensacaoService.atualizarCompensacao(compensacaoId, requestDtoAtualizacao, usuarioLogado);

        // Assert
        assertNotNull(result);
        assertEquals(compensacaoId, result.id());
        assertEquals(requestDtoAtualizacao.tipo(), result.tipo());
        assertEquals(requestDtoAtualizacao.quantidade().doubleValue(), result.quantidade().doubleValue(), 0.001);
        // Data de registro deve ser a original, não é alterada na atualização
        assertEquals(compensacaoSalva.getDataRegistro(), result.dataRegistro()); 
        assertEquals(usuarioLogado.getId(), result.usuarioId());

        // Verificar se o save foi chamado com os dados atualizados
        org.mockito.ArgumentCaptor<Compensacao> captor = org.mockito.ArgumentCaptor.forClass(Compensacao.class);
        verify(compensacaoRepository).save(captor.capture());
        Compensacao compensacaoSalvaCapturada = captor.getValue();

        assertEquals(requestDtoAtualizacao.tipo(), compensacaoSalvaCapturada.getTipo());
        assertEquals(requestDtoAtualizacao.quantidade(), compensacaoSalvaCapturada.getQuantidade());
        
        verify(compensacaoRepository, times(1)).findById(compensacaoId);
        verify(compensacaoRepository, times(1)).save(any(Compensacao.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar compensação inexistente")
    void atualizarCompensacao_compensacaoNaoExistente_deveLancarResourceNotFoundException() {
        // Arrange
        Long compensacaoIdInexistente = 99L;
        CompensacaoRequestDto requestDtoAtualizacao = new CompensacaoRequestDto("TIPO_NOVO", BigDecimal.TEN);
        when(compensacaoRepository.findById(compensacaoIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            compensacaoService.atualizarCompensacao(compensacaoIdInexistente, requestDtoAtualizacao, usuarioLogado);
        });

        assertEquals(String.format("Compensacao não encontrado com id : '%s'", compensacaoIdInexistente), exception.getMessage());
        verify(compensacaoRepository, times(1)).findById(compensacaoIdInexistente);
        verify(compensacaoRepository, never()).save(any(Compensacao.class));
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao tentar atualizar compensação de outro usuário")
    void atualizarCompensacao_usuarioNaoAutorizado_deveLancarAccessDeniedException() {
        // Arrange
        Long compensacaoId = compensacaoSalva.getId(); // Pertence a usuarioLogado
        
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setEmail("outro@example.com");
        outroUsuario.setRoles("ROLE_USER");

        CompensacaoRequestDto requestDtoAtualizacao = new CompensacaoRequestDto("TIPO_NOVO", BigDecimal.ONE);

        when(compensacaoRepository.findById(compensacaoId)).thenReturn(Optional.of(compensacaoSalva)); 

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            compensacaoService.atualizarCompensacao(compensacaoId, requestDtoAtualizacao, outroUsuario);
        });

        assertEquals("Usuário não autorizado a atualizar esta compensação.", exception.getMessage());
        verify(compensacaoRepository, times(1)).findById(compensacaoId);
        verify(compensacaoRepository, never()).save(any(Compensacao.class)); 
    }

    @Test
    @DisplayName("Deve deletar compensação com sucesso")
    void deletarCompensacao_compensacaoExistenteEUsuarioAutorizado_deveChamarDeleteById() {
        // Arrange
        Long compensacaoId = compensacaoSalva.getId(); // do setUp, pertence a usuarioLogado
        when(compensacaoRepository.findById(compensacaoId)).thenReturn(Optional.of(compensacaoSalva));
        doNothing().when(compensacaoRepository).deleteById(compensacaoId);

        // Act
        assertDoesNotThrow(() -> {
            compensacaoService.deletarCompensacao(compensacaoId, usuarioLogado);
        });

        // Assert
        verify(compensacaoRepository, times(1)).findById(compensacaoId);
        verify(compensacaoRepository, times(1)).deleteById(compensacaoId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar compensação inexistente")
    void deletarCompensacao_compensacaoNaoExistente_deveLancarResourceNotFoundException() {
        // Arrange
        Long compensacaoIdInexistente = 99L;
        when(compensacaoRepository.findById(compensacaoIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            compensacaoService.deletarCompensacao(compensacaoIdInexistente, usuarioLogado);
        });

        assertEquals(String.format("Compensacao não encontrado com id : '%s'", compensacaoIdInexistente), exception.getMessage());
        verify(compensacaoRepository, times(1)).findById(compensacaoIdInexistente);
        verify(compensacaoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao tentar deletar compensação de outro usuário")
    void deletarCompensacao_usuarioNaoAutorizado_deveLancarAccessDeniedException() {
        // Arrange
        Long compensacaoId = compensacaoSalva.getId(); // Pertence a usuarioLogado

        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setEmail("outro@example.com");
        outroUsuario.setRoles("ROLE_USER");

        when(compensacaoRepository.findById(compensacaoId)).thenReturn(Optional.of(compensacaoSalva)); 

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            compensacaoService.deletarCompensacao(compensacaoId, outroUsuario);
        });

        assertEquals("Usuário não autorizado a deletar esta compensação.", exception.getMessage());
        verify(compensacaoRepository, times(1)).findById(compensacaoId);
        verify(compensacaoRepository, never()).deleteById(anyLong());
    }

} 