package br.com.fiap.esg.mobilidade_sustentavel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

import br.com.fiap.esg.mobilidade_sustentavel.config.Co2EmissionConfig;
import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.model.Viagem;
import br.com.fiap.esg.mobilidade_sustentavel.repository.ViagemRepository;
import br.com.fiap.esg.mobilidade_sustentavel.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class ViagemServiceTest {

    @Mock
    private ViagemRepository viagemRepository;

    @Mock
    private Co2EmissionConfig co2EmissionConfig;

    @InjectMocks
    private ViagemService viagemService;

    private Usuario usuarioLogado;
    private ViagemRequestDto viagemRequestDto;
    private Viagem viagemSalva;

    @BeforeEach
    void setUp() {
        usuarioLogado = new Usuario();
        usuarioLogado.setId(1L);
        usuarioLogado.setNome("Test User");
        usuarioLogado.setEmail("test@example.com");

        viagemRequestDto = new ViagemRequestDto(
            "CARRO", 
            BigDecimal.valueOf(10.5),
            LocalDateTime.now()
        );

        viagemSalva = new Viagem();
        viagemSalva.setId(1L);
        viagemSalva.setUsuario(usuarioLogado);
        viagemSalva.setTransporte(viagemRequestDto.transporte());
        viagemSalva.setDistanciaKm(viagemRequestDto.distanciaKm());
        viagemSalva.setDataHora(viagemRequestDto.dataHora());
        // CO2 será calculado e setado nos testes
    }

    // Testes para o método privado calcularCO2, acessado através de criarViagem ou atualizarViagem
    // Ou podemos torná-lo public/package-private para teste direto, ou testar seu efeito indiretamente.
    // Por enquanto, vamos testar o efeito através de criarViagem.

    @Test
    @DisplayName("Deve calcular CO2 corretamente para transporte conhecido")
    void calcularCO2_transporteConhecido_retornaValorCalculado() {
        // Arrange
        when(co2EmissionConfig.getFactorForTransport("CARRO")).thenReturn(new BigDecimal("0.120"));
        // Act
        // BigDecimal co2 = viagemService.calcularCO2("CARRO", BigDecimal.valueOf(10.0)); // Se fosse public
        // Para testar indiretamente via criarViagem:
        when(viagemRepository.save(any(Viagem.class))).thenAnswer(invocation -> {
            Viagem v = invocation.getArgument(0);
            // O cálculo de CO2 é feito dentro de criarViagem ANTES do save
            // Portanto, o valor de CO2 já deve estar na viagem mockada para o save
            return v; // Retorna a própria viagem que seria salva
        });

        viagemService.criarViagem(viagemRequestDto, usuarioLogado);

        // Assert
        // Capturar o argumento passado para viagemRepository.save para verificar o CO2
        org.mockito.ArgumentCaptor<Viagem> viagemArgumentCaptor = org.mockito.ArgumentCaptor.forClass(Viagem.class);
        verify(viagemRepository).save(viagemArgumentCaptor.capture());
        Viagem viagemCapturada = viagemArgumentCaptor.getValue();

        BigDecimal expectedCo2 = BigDecimal.valueOf(10.5).multiply(new BigDecimal("0.120")).setScale(3, RoundingMode.HALF_UP);
        assertEquals(expectedCo2, viagemCapturada.getCo2());
    }

    @Test
    @DisplayName("Deve retornar CO2 zero para transporte nulo")
    void calcularCO2_transporteNulo_retornaZero() {
        when(viagemRepository.save(any(Viagem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ViagemRequestDto dtoTransporteNulo = new ViagemRequestDto(null, BigDecimal.valueOf(10.0), LocalDateTime.now());
        viagemService.criarViagem(dtoTransporteNulo, usuarioLogado);

        org.mockito.ArgumentCaptor<Viagem> captor = org.mockito.ArgumentCaptor.forClass(Viagem.class);
        verify(viagemRepository).save(captor.capture());
        assertEquals(BigDecimal.ZERO.setScale(3), captor.getValue().getCo2());
    }

    @Test
    @DisplayName("Deve criar viagem com sucesso e calcular CO2")
    void criarViagem_dadosValidos_deveRetornarViagemResponseDtoComCo2() {
        // Arrange
        BigDecimal fatorCarro = new BigDecimal("0.120");
        BigDecimal co2Esperado = viagemRequestDto.distanciaKm().multiply(fatorCarro).setScale(3, RoundingMode.HALF_UP);
        
        viagemSalva.setCo2(co2Esperado); // Definimos o CO2 esperado na entidade que será retornada pelo save

        when(co2EmissionConfig.getFactorForTransport(viagemRequestDto.transporte())).thenReturn(fatorCarro);
        when(viagemRepository.save(any(Viagem.class))).thenReturn(viagemSalva);

        // Act
        ViagemResponseDto result = viagemService.criarViagem(viagemRequestDto, usuarioLogado);

        // Assert
        assertNotNull(result);
        assertEquals(viagemRequestDto.transporte(), result.getTransporte());
        assertEquals(viagemRequestDto.distanciaKm().doubleValue(), result.getDistanciaKm().doubleValue());
        assertEquals(co2Esperado.doubleValue(), result.getCo2().doubleValue(), 0.001);
        assertEquals(usuarioLogado.getId(), result.getUsuarioId());

        verify(co2EmissionConfig, times(1)).getFactorForTransport(viagemRequestDto.transporte());
        verify(viagemRepository, times(1)).save(any(Viagem.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao criar viagem com usuário nulo")
    void criarViagem_comUsuarioNulo_deveLancarIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            viagemService.criarViagem(viagemRequestDto, null);
        });
        assertEquals("Usuário autenticado inválido ou não persistido.", exception.getMessage());
        verify(viagemRepository, never()).save(any(Viagem.class));
    }

    @Test
    @DisplayName("Deve retornar ViagemResponseDto quando buscarViagemPorId encontra viagem")
    void buscarViagemPorId_quandoViagemExiste_deveRetornarOptionalComViagemResponseDto() {
        // Arrange
        Long viagemId = 1L;
        // Reutilizar 'viagemSalva' do setUp, que já tem um ID e dados
        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemSalva)); 

        // Act
        Optional<ViagemResponseDto> result = viagemService.buscarViagemPorId(viagemId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(viagemSalva.getTransporte(), result.get().getTransporte());
        assertEquals(viagemSalva.getDistanciaKm(), result.get().getDistanciaKm());
        verify(viagemRepository, times(1)).findById(viagemId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando buscarViagemPorId não encontra viagem")
    void buscarViagemPorId_quandoViagemNaoExiste_deveRetornarOptionalVazio() {
        // Arrange
        Long viagemId = 99L;
        when(viagemRepository.findById(viagemId)).thenReturn(Optional.empty());

        // Act
        Optional<ViagemResponseDto> result = viagemService.buscarViagemPorId(viagemId);

        // Assert
        assertTrue(result.isEmpty());
        verify(viagemRepository, times(1)).findById(viagemId);
    }

    @Test
    @DisplayName("Deve retornar ViagemResponseDto quando getViagemByIdOrThrow encontra viagem")
    void getViagemByIdOrThrow_quandoViagemExiste_deveRetornarViagemResponseDto() {
        // Arrange
        Long viagemId = 1L;
        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemSalva));

        // Act
        ViagemResponseDto result = viagemService.getViagemByIdOrThrow(viagemId);

        // Assert
        assertNotNull(result);
        assertEquals(viagemSalva.getTransporte(), result.getTransporte());
        verify(viagemRepository, times(1)).findById(viagemId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando getViagemByIdOrThrow não encontra viagem")
    void getViagemByIdOrThrow_quandoViagemNaoExiste_deveLancarResourceNotFoundException() {
        // Arrange
        Long viagemId = 99L;
        when(viagemRepository.findById(viagemId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            viagemService.getViagemByIdOrThrow(viagemId);
        });
        
        assertEquals(String.format("Viagem não encontrado com id : '%s'", viagemId), exception.getMessage());
        verify(viagemRepository, times(1)).findById(viagemId);
    }

    @Test
    @DisplayName("Deve listar viagens de um usuário específico")
    void listarViagensPorUsuario_quandoUsuarioTemViagens_deveRetornarListaDeViagemResponseDto() {
        // Arrange
        Long usuarioId = 1L;
        Viagem viagem1 = new Viagem();
        viagem1.setId(1L);
        viagem1.setUsuario(usuarioLogado); // usuarioLogado has ID 1L
        viagem1.setTransporte("CARRO");
        viagem1.setDistanciaKm(BigDecimal.valueOf(10.0));
        viagem1.setCo2(BigDecimal.valueOf(1.2));
        viagem1.setDataHora(LocalDateTime.now());

        Viagem viagem2 = new Viagem();
        viagem2.setId(2L);
        viagem2.setUsuario(usuarioLogado);
        viagem2.setTransporte("ONIBUS");
        viagem2.setDistanciaKm(BigDecimal.valueOf(20.0));
        viagem2.setCo2(BigDecimal.valueOf(1.0)); 
        viagem2.setDataHora(LocalDateTime.now().minusDays(1));

        List<Viagem> viagensDoUsuario = List.of(viagem1, viagem2);
        when(viagemRepository.findByUsuarioId(usuarioId)).thenReturn(viagensDoUsuario);

        // Act
        List<ViagemResponseDto> result = viagemService.listarViagensPorUsuario(usuarioId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(viagem1.getId(), result.get(0).getId());
        assertEquals(viagem1.getTransporte(), result.get(0).getTransporte());
        assertEquals(viagem2.getId(), result.get(1).getId());
        assertEquals(viagem2.getTransporte(), result.get(1).getTransporte());

        verify(viagemRepository, times(1)).findByUsuarioId(usuarioId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem viagens")
    void listarViagensPorUsuario_quandoUsuarioNaoTemViagens_deveRetornarListaVazia() {
        // Arrange
        Long usuarioId = 2L; // Um usuário diferente que não tem viagens
        when(viagemRepository.findByUsuarioId(usuarioId)).thenReturn(Collections.emptyList());

        // Act
        List<ViagemResponseDto> result = viagemService.listarViagensPorUsuario(usuarioId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(viagemRepository, times(1)).findByUsuarioId(usuarioId);
    }

    @Test
    @DisplayName("Deve listar todas as viagens quando existem viagens")
    void listarTodasViagens_quandoExistemViagens_deveRetornarListaDeViagemResponseDto() {
        // Arrange
        Viagem viagem1 = new Viagem(); // Dados podem ser genéricos, já que não filtramos por usuário
        viagem1.setId(1L);
        Usuario u1 = new Usuario(); u1.setId(1L);
        viagem1.setUsuario(u1);
        viagem1.setTransporte("AVIAO");
        viagem1.setDistanciaKm(BigDecimal.valueOf(1000.0));
        viagem1.setCo2(BigDecimal.valueOf(250.0));
        viagem1.setDataHora(LocalDateTime.now());

        Viagem viagem2 = new Viagem();
        viagem2.setId(2L);
        Usuario u2 = new Usuario(); u2.setId(2L);
        viagem2.setUsuario(u2);
        viagem2.setTransporte("TREM");
        viagem2.setDistanciaKm(BigDecimal.valueOf(300.0));
        viagem2.setCo2(BigDecimal.valueOf(15.0));
        viagem2.setDataHora(LocalDateTime.now().minusHours(5));

        List<Viagem> todasAsViagens = List.of(viagem1, viagem2);
        when(viagemRepository.findAll()).thenReturn(todasAsViagens);

        // Act
        List<ViagemResponseDto> result = viagemService.listarTodasViagens();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(viagem1.getId(), result.get(0).getId());
        assertEquals(viagem2.getId(), result.get(1).getId());

        verify(viagemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não existem viagens no sistema")
    void listarTodasViagens_quandoNaoExistemViagens_deveRetornarListaVazia() {
        // Arrange
        when(viagemRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ViagemResponseDto> result = viagemService.listarTodasViagens();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(viagemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve atualizar viagem com sucesso e recalcular CO2")
    void atualizarViagem_dadosValidos_deveRetornarViagemResponseDtoAtualizadaComNovoCO2() {
        // Arrange
        Long viagemId = 1L;
        // 'viagemSalva' from setUp is the existing trip
        // 'usuarioLogado' from setUp is the owner
        
        ViagemRequestDto requestDtoAtualizacao = new ViagemRequestDto(
            "ONIBUS", // Transporte mudou
            BigDecimal.valueOf(20.0), // Distância mudou
            viagemSalva.getDataHora() // Data/Hora pode ser a mesma ou diferente
        );

        // Mock para encontrar a viagem existente
        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemSalva));

        // Mock para o fator de CO2 do novo transporte
        BigDecimal fatorOnibus = new BigDecimal("0.050");
        when(co2EmissionConfig.getFactorForTransport("ONIBUS")).thenReturn(fatorOnibus);

        // Mock para o save (deve capturar a entidade atualizada)
        when(viagemRepository.save(any(Viagem.class))).thenAnswer(invocation -> {
            Viagem viagemParaSalvar = invocation.getArgument(0);
            // Simula o ID sendo atribuído ou mantido pelo save
            viagemParaSalvar.setId(viagemId); 
            return viagemParaSalvar;
        });
        
        BigDecimal co2Esperado = requestDtoAtualizacao.distanciaKm().multiply(fatorOnibus).setScale(3, RoundingMode.HALF_UP);

        // Act
        ViagemResponseDto result = viagemService.atualizarViagem(viagemId, requestDtoAtualizacao, usuarioLogado);

        // Assert
        assertNotNull(result);
        assertEquals(viagemId, result.getId());
        assertEquals(requestDtoAtualizacao.transporte(), result.getTransporte());
        assertEquals(requestDtoAtualizacao.distanciaKm().doubleValue(), result.getDistanciaKm().doubleValue(), 0.001);
        assertEquals(co2Esperado.doubleValue(), result.getCo2().doubleValue(), 0.001); // Verifica CO2 recalculado
        assertEquals(usuarioLogado.getId(), result.getUsuarioId());

        // Verificar se o save foi chamado com os dados corretos (especialmente CO2)
        org.mockito.ArgumentCaptor<Viagem> viagemCaptor = org.mockito.ArgumentCaptor.forClass(Viagem.class);
        verify(viagemRepository).save(viagemCaptor.capture());
        Viagem viagemSalvaCapturada = viagemCaptor.getValue();

        assertEquals(requestDtoAtualizacao.transporte(), viagemSalvaCapturada.getTransporte());
        assertEquals(requestDtoAtualizacao.distanciaKm(), viagemSalvaCapturada.getDistanciaKm());
        assertEquals(co2Esperado, viagemSalvaCapturada.getCo2());
        
        verify(viagemRepository, times(1)).findById(viagemId);
        verify(co2EmissionConfig, times(1)).getFactorForTransport("ONIBUS");
        verify(viagemRepository, times(1)).save(any(Viagem.class));
    }

    @Test
    @DisplayName("Deve recalcular CO2 para zero se transporte for nulo na atualização")
    void atualizarViagem_transporteNuloNaAtualizacao_deveRecalcularCO2ParaZero() {
        // Arrange
        Long viagemId = 1L;
        viagemSalva.setTransporte("CARRO"); // Transporte inicial
        viagemSalva.setDistanciaKm(BigDecimal.valueOf(10.0));
        viagemSalva.setCo2(BigDecimal.valueOf(1.2)); // CO2 inicial

        ViagemRequestDto requestDtoAtualizacao = new ViagemRequestDto(
            null, // Transporte se torna nulo
            BigDecimal.valueOf(15.0),
            viagemSalva.getDataHora()
        );

        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemSalva));
        when(viagemRepository.save(any(Viagem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ViagemResponseDto result = viagemService.atualizarViagem(viagemId, requestDtoAtualizacao, usuarioLogado);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(3), result.getCo2());

        org.mockito.ArgumentCaptor<Viagem> viagemCaptor = org.mockito.ArgumentCaptor.forClass(Viagem.class);
        verify(viagemRepository).save(viagemCaptor.capture());
        assertEquals(BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP), viagemCaptor.getValue().getCo2());
        verify(co2EmissionConfig, never()).getFactorForTransport(anyString());
    }
    
    @Test
    @DisplayName("Deve recalcular CO2 para zero se distancia for nula na atualização")
    void atualizarViagem_distanciaNulaNaAtualizacao_deveRecalcularCO2ParaZero() {
        // Arrange
        Long viagemId = 1L;
        viagemSalva.setTransporte("CARRO"); 
        viagemSalva.setDistanciaKm(BigDecimal.valueOf(10.0));
        viagemSalva.setCo2(BigDecimal.valueOf(1.2));

        ViagemRequestDto requestDtoAtualizacao = new ViagemRequestDto(
            "MOTO",
            null, // Distancia se torna nula
            viagemSalva.getDataHora()
        );

        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemSalva));
        when(viagemRepository.save(any(Viagem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Mesmo que o transporte seja válido, com distância nula o CO2 deve ser zero
        // Não é necessário mockar co2EmissionConfig.getFactorForTransport pois será curto-circuitado

        // Act
        ViagemResponseDto result = viagemService.atualizarViagem(viagemId, requestDtoAtualizacao, usuarioLogado);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(3), result.getCo2());
        
        org.mockito.ArgumentCaptor<Viagem> viagemCaptor = org.mockito.ArgumentCaptor.forClass(Viagem.class);
        verify(viagemRepository).save(viagemCaptor.capture());
        assertEquals(BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP), viagemCaptor.getValue().getCo2());
        verify(co2EmissionConfig, never()).getFactorForTransport(anyString());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar viagem inexistente")
    void atualizarViagem_viagemNaoExistente_deveLancarResourceNotFoundException() {
        // Arrange
        Long viagemIdInexistente = 99L;
        ViagemRequestDto requestDtoAtualizacao = new ViagemRequestDto(
            "CARRO", 
            BigDecimal.valueOf(5.0), 
            LocalDateTime.now()
        );
        when(viagemRepository.findById(viagemIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            viagemService.atualizarViagem(viagemIdInexistente, requestDtoAtualizacao, usuarioLogado);
        });

        assertEquals(String.format("Viagem não encontrado com id : '%s'", viagemIdInexistente), exception.getMessage());
        verify(viagemRepository, times(1)).findById(viagemIdInexistente);
        verify(viagemRepository, never()).save(any(Viagem.class));
        verify(co2EmissionConfig, never()).getFactorForTransport(anyString());
    }

    @Test
    @DisplayName("Deve lançar SecurityException ao tentar atualizar viagem de outro usuário")
    void atualizarViagem_usuarioNaoAutorizado_deveLancarSecurityException() {
        // Arrange
        Long viagemId = 1L;
        // viagemSalva pertence a usuarioLogado (ID 1L)
        
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L); // ID diferente do proprietário da viagem
        outroUsuario.setEmail("outro@example.com");

        ViagemRequestDto requestDtoAtualizacao = new ViagemRequestDto(
            "BICICLETA", 
            BigDecimal.valueOf(2.0), 
            LocalDateTime.now()
        );

        // Mock para encontrar a viagem existente, que pertence a usuarioLogado
        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemSalva)); 

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            viagemService.atualizarViagem(viagemId, requestDtoAtualizacao, outroUsuario);
        });

        assertEquals("Usuário não autorizado a atualizar esta viagem.", exception.getMessage());
        verify(viagemRepository, times(1)).findById(viagemId);
        verify(viagemRepository, never()).save(any(Viagem.class)); 
        verify(co2EmissionConfig, never()).getFactorForTransport(anyString());
    }

    @Test
    @DisplayName("Deve deletar viagem com sucesso")
    void deletarViagem_viagemExistenteEUsuarioAutorizado_deveChamarDeleteById() {
        // Arrange
        Long viagemId = 1L;
        // viagemSalva from setUp belongs to usuarioLogado (ID 1L)
        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemSalva));
        doNothing().when(viagemRepository).deleteById(viagemId);

        // Act
        assertDoesNotThrow(() -> {
            viagemService.deletarViagem(viagemId, usuarioLogado);
        });

        // Assert
        verify(viagemRepository, times(1)).findById(viagemId);
        verify(viagemRepository, times(1)).deleteById(viagemId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar viagem inexistente")
    void deletarViagem_viagemNaoExistente_deveLancarResourceNotFoundException() {
        // Arrange
        Long viagemIdInexistente = 99L;
        when(viagemRepository.findById(viagemIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            viagemService.deletarViagem(viagemIdInexistente, usuarioLogado);
        });

        assertEquals(String.format("Viagem não encontrado com id : '%s'", viagemIdInexistente), exception.getMessage());
        verify(viagemRepository, times(1)).findById(viagemIdInexistente);
        verify(viagemRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve lançar SecurityException ao tentar deletar viagem de outro usuário")
    void deletarViagem_usuarioNaoAutorizado_deveLancarSecurityException() {
        // Arrange
        Long viagemId = 1L;
        // viagemSalva pertence a usuarioLogado (ID 1L)

        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L); // ID diferente do proprietário da viagem
        outroUsuario.setEmail("outro@example.com");

        when(viagemRepository.findById(viagemId)).thenReturn(Optional.of(viagemSalva)); 

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            viagemService.deletarViagem(viagemId, outroUsuario);
        });

        assertEquals("Usuário não autorizado a deletar esta viagem.", exception.getMessage());
        verify(viagemRepository, times(1)).findById(viagemId);
        verify(viagemRepository, never()).deleteById(anyLong());
    }
} 