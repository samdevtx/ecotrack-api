package br.com.fiap.esg.mobilidade_sustentavel.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.exception.ResourceNotFoundException;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class InsightServiceTest {

    @Mock
    private WebClient aiWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ViagemService viagemService;

    @InjectMocks
    private InsightService insightService;

    private Usuario usuario;
    private final Long usuarioId = 1L;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");

        // WebClient stubs moved to individual tests that need them
    }

    private void mockWebClientChain() { // Helper method for common stubs
        when(aiWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("Deve retornar mensagem padrão quando usuário não tem viagens")
    void gerarInsightsSustentabilidade_usuarioSemViagens_retornaMensagemPadrao() {
        // Arrange
        when(viagemService.listarViagensPorUsuario(usuarioId)).thenReturn(Collections.emptyList());

        // Act
        Mono<String> resultMono = insightService.gerarInsightsSustentabilidade(usuarioId);

        // Assert
        String result = resultMono.block();
        assertEquals("Nenhuma viagem registrada para gerar sugestões.", result);
        verify(viagemService).listarViagensPorUsuario(usuarioId);
        verify(aiWebClient, never()).post();
    }

    @Test
    @DisplayName("Deve gerar insight com sucesso quando usuário tem viagens e API da IA responde corretamente")
    void gerarInsightsSustentabilidade_usuarioComViagens_apiSucesso_retornaInsightGerado() {
        // Arrange
        mockWebClientChain(); 
        ViagemResponseDto viagem1 = new ViagemResponseDto(1L, usuarioId, "Test User", "CARRO", BigDecimal.valueOf(10.5), BigDecimal.valueOf(1.26), LocalDateTime.now());
        ViagemResponseDto viagem2 = new ViagemResponseDto(2L, usuarioId, "Test User", "ONIBUS", BigDecimal.valueOf(20.0), BigDecimal.valueOf(1.00), LocalDateTime.now().minusDays(1));
        List<ViagemResponseDto> viagens = List.of(viagem1, viagem2);
        when(viagemService.listarViagensPorUsuario(usuarioId)).thenReturn(viagens);

        List<String> candidateLabels = List.of(
            "Ótimo uso de transporte de baixa emissão!",
            "Considere alternativas de baixa emissão para algumas viagens.", 
            "Alta pegada de carbono nas viagens recentes.",
            "Bom equilíbrio entre diferentes modos de transporte.",
            "Potencial para otimizar rotas e reduzir emissões.",
            "Viagens curtas podem ser feitas com transporte ativo (bicicleta/caminhada)."
        );
        String expectedInsight = candidateLabels.get(1);
        
        List<Double> scores = List.of(0.1, 0.8, 0.05, 0.03, 0.01, 0.01);
        String mockSequence = "Mocked sequence from API"; 

        InsightService.ZeroShotClassificationResponse classificationResponse = 
            new InsightService.ZeroShotClassificationResponse(mockSequence, candidateLabels, scores);
        // No longer a list, API returns a single object

        when(responseSpec.bodyToMono(InsightService.ZeroShotClassificationResponse.class))
            .thenReturn(Mono.just(classificationResponse)); // Return the single object

        // Act
        Mono<String> resultMono = insightService.gerarInsightsSustentabilidade(usuarioId);
        String result = resultMono.block();

        // Assert
        assertEquals(expectedInsight, result);
        verify(viagemService).listarViagensPorUsuario(usuarioId);
        verify(aiWebClient).post(); 
        org.mockito.ArgumentCaptor<Map<String, Object>> payloadCaptor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(requestBodySpec).bodyValue(payloadCaptor.capture()); // verify on requestBodySpec now
        Map<String, Object> capturedPayload = payloadCaptor.getValue(); 
        String capturedInputString = (String) capturedPayload.get("inputs");
        assertNotNull(capturedInputString);

        // Verify candidate_labels in parameters
        Map<String, Object> capturedParameters = (Map<String, Object>) capturedPayload.get("parameters");
        assertNotNull(capturedParameters, "Parameters map should not be null");
        List<String> capturedCandidateLabels = (List<String>) capturedParameters.get("candidate_labels");
        assertNotNull(capturedCandidateLabels, "Candidate labels list should not be null in parameters");
        assertEquals(candidateLabels.size(), capturedCandidateLabels.size(), "Candidate labels list size should match");
        assertTrue(candidateLabels.containsAll(capturedCandidateLabels) && capturedCandidateLabels.containsAll(candidateLabels), "Captured candidate labels should match the predefined list");


        // Construct the expected parts of the input text using Locale.US for numbers
        String expectedInputTextBreakdownPart1 = String.format(Locale.US, "Viagem 1: CARRO, %.2f km, %.3f kg CO2", 10.5, 1.260);
        String expectedInputTextBreakdownPart2 = String.format(Locale.US, "Viagem 2: ONIBUS, %.2f km, %.3f kg CO2", 20.0, 1.000);
        String expectedTotalEmissions = String.format(Locale.US, "Total de emissões: %.1f g CO2.", 2260.0);

        assertTrue(capturedInputString.contains("Relatório de mobilidade:"), "Input text should contain 'Relatório de mobilidade:'.");
        assertTrue(capturedInputString.contains(expectedInputTextBreakdownPart1), "Input text should contain correct details for viagem 1 with US locale.");
        assertTrue(capturedInputString.contains(expectedInputTextBreakdownPart2), "Input text should contain correct details for viagem 2 with US locale.");
        assertTrue(capturedInputString.contains(expectedTotalEmissions), "Input text should contain correct total CO2 emissions with US locale formatting.");
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro se API da IA retornar resposta nula (Mono.empty() from bodyToMono)")
    void gerarInsightsSustentabilidade_apiRetornaMonoEmpty_retornaMensagemErro() {
        // Arrange
        mockWebClientChain(); 
        ViagemResponseDto viagem1 = new ViagemResponseDto(1L, usuarioId, "Test User", "CARRO", BigDecimal.valueOf(10.0), BigDecimal.valueOf(1.0), LocalDateTime.now());
        when(viagemService.listarViagensPorUsuario(usuarioId)).thenReturn(List.of(viagem1));
        
        when(responseSpec.bodyToMono(InsightService.ZeroShotClassificationResponse.class))
            .thenReturn(Mono.empty()); // Simulate bodyToMono returning empty (e.g. due to 404 or non-parsable)

        // Act
        String result = insightService.gerarInsightsSustentabilidade(usuarioId).block();

        // Assert
        String expectedMessage = "Não foi possível gerar uma sugestão no momento (resposta vazia do modelo).";
        assertEquals(expectedMessage, result);
        verify(aiWebClient).post();
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro se API da IA retornar objeto com lista de labels vazia")
    void gerarInsightsSustentabilidade_apiRetornaObjetoComListaLabelsVazia_retornaMensagemErro() {
        // Arrange
        mockWebClientChain(); 
        ViagemResponseDto viagem1 = new ViagemResponseDto(1L, usuarioId, "Test User", "CARRO", BigDecimal.valueOf(10.0), BigDecimal.valueOf(1.0), LocalDateTime.now());
        when(viagemService.listarViagensPorUsuario(usuarioId)).thenReturn(List.of(viagem1));
        
        InsightService.ZeroShotClassificationResponse responseWithEmptyLabels =
            new InsightService.ZeroShotClassificationResponse("seq", Collections.emptyList(), Collections.emptyList());

        when(responseSpec.bodyToMono(InsightService.ZeroShotClassificationResponse.class))
            .thenReturn(Mono.just(responseWithEmptyLabels));

        // Act
        String result = insightService.gerarInsightsSustentabilidade(usuarioId).block();

        // Assert
        assertEquals("Resposta do modelo de IA em formato inesperado.", result);
        verify(aiWebClient).post();
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro se API da IA retornar resposta malformada (null labels)")
    void gerarInsightsSustentabilidade_apiRetornaRespostaMalformada_retornaMensagemErro() {
        // Arrange
        mockWebClientChain(); 
        ViagemResponseDto viagem1 = new ViagemResponseDto(1L, usuarioId, "Test User", "CARRO", BigDecimal.valueOf(10.0), BigDecimal.valueOf(1.0), LocalDateTime.now());
        when(viagemService.listarViagensPorUsuario(usuarioId)).thenReturn(List.of(viagem1));
        
        InsightService.ZeroShotClassificationResponse malformedResponse = 
            new InsightService.ZeroShotClassificationResponse("seq", null, List.of(0.1));

        when(responseSpec.bodyToMono(InsightService.ZeroShotClassificationResponse.class))
            .thenReturn(Mono.just(malformedResponse));

        // Act
        String result = insightService.gerarInsightsSustentabilidade(usuarioId).block();

        // Assert
        assertEquals("Resposta do modelo de IA em formato inesperado.", result);
        verify(aiWebClient).post();
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro genérica se API da IA falhar com WebClientResponseException")
    void gerarInsightsSustentabilidade_apiFalha_WebClientResponseException_retornaMensagemErroGenerica() {
        // Arrange
        mockWebClientChain(); 
        ViagemResponseDto viagem1 = new ViagemResponseDto(1L, usuarioId, "Test User", "BICICLETA", BigDecimal.valueOf(5.0), BigDecimal.valueOf(0.0), LocalDateTime.now());
        when(viagemService.listarViagensPorUsuario(usuarioId)).thenReturn(List.of(viagem1));
        when(responseSpec.bodyToMono(InsightService.ZeroShotClassificationResponse.class)) // Changed to class
            .thenReturn(Mono.error(new WebClientResponseException("API Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)));

        // Act
        String result = insightService.gerarInsightsSustentabilidade(usuarioId).block();

        // Assert
        assertEquals("Desculpe, ocorreu um erro ao tentar gerar sua sugestão personalizada via IA.", result); // Updated message
        verify(aiWebClient).post();
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro genérica se API da IA falhar com outra exceção")
    void gerarInsightsSustentabilidade_apiFalha_OutraExcecao_retornaMensagemErroGenerica() {
        // Arrange
        mockWebClientChain(); 
        ViagemResponseDto viagem1 = new ViagemResponseDto(1L, usuarioId, "Test User", "PATINETE", BigDecimal.valueOf(2.0), BigDecimal.valueOf(0.01), LocalDateTime.now());
        when(viagemService.listarViagensPorUsuario(usuarioId)).thenReturn(List.of(viagem1));
        when(responseSpec.bodyToMono(InsightService.ZeroShotClassificationResponse.class)) // Changed to class
            .thenReturn(Mono.error(new RuntimeException("Erro inesperado na API")));
        
        // Act
        String result = insightService.gerarInsightsSustentabilidade(usuarioId).block();

        // Assert
        assertEquals("Desculpe, ocorreu um erro ao tentar gerar sua sugestão personalizada via IA.", result); // Updated message
        verify(aiWebClient).post();
    }

    @Test
    @DisplayName("Deve propagar exceção se ViagemService falhar ao listar viagens")
    void gerarInsightsSustentabilidade_viagemServiceFalha_propagaExcecao() {
        // Arrange
        ResourceNotFoundException RNFException = new ResourceNotFoundException("Usuario", "id", usuarioId);
        when(viagemService.listarViagensPorUsuario(usuarioId)).thenThrow(RNFException);

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            insightService.gerarInsightsSustentabilidade(usuarioId).block();
        });

        assertEquals(RNFException.getMessage(), thrown.getMessage());
        verify(viagemService).listarViagensPorUsuario(usuarioId);
        verify(aiWebClient, never()).post();
    }
} 