package br.com.fiap.esg.mobilidade_sustentavel.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.ArrayList;
import java.util.Comparator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemResponseDto;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InsightService {

    private static final Logger log = LoggerFactory.getLogger(InsightService.class);

    private final WebClient aiWebClient;
    private final ViagemService viagemService;

    private static final String ZERO_SHOT_MODEL_PATH = "/models/MoritzLaurer/DeBERTa-v3-large-mnli-fever-anli-ling-wanli";
    private static final List<String> CANDIDATE_LABELS = List.of(
            "Ótimo uso de transporte de baixa emissão!",
            "Considere alternativas de baixa emissão para algumas viagens.",
            "Alta pegada de carbono nas viagens recentes.",
            "Bom equilíbrio entre diferentes modos de transporte.",
            "Potencial para otimizar rotas e reduzir emissões.",
            "Viagens curtas podem ser feitas com transporte ativo (bicicleta/caminhada)."
    );

    // Simple DTO for deserializing the zero-shot classification response
    // Assuming a single input, the API might return a single object or a list with one object.
    // For simplicity, let's assume it directly returns the object if not a list.
    // If it's a list, bodyToMono would need new ParameterizedTypeReference<List<ZeroShotClassificationResponse>>(){}
    // and then we'd take the first element.
    // Let's try bodyToMono(ZeroShotClassificationResponse.class) first.
    record ZeroShotClassificationResponse(
        @JsonProperty("sequence") String sequence,
        @JsonProperty("labels") List<String> labels,
        @JsonProperty("scores") List<Double> scores
    ) {}

    @Autowired
    public InsightService(WebClient aiWebClient, ViagemService viagemService) {
        this.aiWebClient = aiWebClient;
        this.viagemService = viagemService;
    }

    public Mono<String> gerarInsightsSustentabilidade(Long usuarioId) {
        List<ViagemResponseDto> viagens = viagemService.listarViagensPorUsuario(usuarioId);
        return gerarSugestaoComBaseNasViagens(viagens);
    }

    private Mono<String> gerarSugestaoComBaseNasViagens(List<ViagemResponseDto> viagens) {
        if (viagens == null || viagens.isEmpty()) {
            return Mono.just("Nenhuma viagem registrada para gerar sugestões.");
        }

        String travelSummary = IntStream.range(0, viagens.size())
            .mapToObj(i -> String.format(Locale.US, "Viagem %d: %s, %.2f km, %.3f kg CO2",
                i + 1,
                viagens.get(i).getTransporte(),
                viagens.get(i).getDistanciaKm(),
                viagens.get(i).getCo2() != null ? viagens.get(i).getCo2().doubleValue() : 0.0))
            .collect(Collectors.joining("\\n"));
        
        double totalCo2Kg = viagens.stream()
            .filter(v -> v.getCo2() != null)
            .mapToDouble(v -> v.getCo2().doubleValue())
            .sum();
        double totalCo2Grams = totalCo2Kg * 1000;

        String inputText = String.format(Locale.US,
            "Relatório de mobilidade:\\n%s\\nTotal de emissões: %.1f g CO2.",
            travelSummary, totalCo2Grams
        );

        Map<String, Object> parameters = Map.of("candidate_labels", CANDIDATE_LABELS);
        Map<String, Object> payload = Map.of(
            "inputs", inputText,
            "parameters", parameters
            // "options", Map.of("wait_for_model", true) // Useful if model might be loading
        );

        return aiWebClient
            .post()
            .uri(ZERO_SHOT_MODEL_PATH) // Specific model path
            .bodyValue(payload)
            .retrieve()
            // Expect a single object, not a list, based on curl test
            .bodyToMono(ZeroShotClassificationResponse.class) 
            .flatMap(response -> { // response is now a single ZeroShotClassificationResponse object
                // No need to check responseList == null or responseList.isEmpty() or get(0)
                if (response.labels() == null || response.scores() == null || response.labels().size() != response.scores().size() || response.labels().isEmpty()) {
                    log.warn("Response from AI is malformed. Labels or scores are problematic. Response: {}", response);
                    return Mono.just("Resposta do modelo de IA em formato inesperado.");
                }

                // Find the label with the highest score
                int bestLabelIndex = IntStream.range(0, response.scores().size())
                                            .boxed()
                                            .max(Comparator.comparing(response.scores()::get))
                                            .orElse(0); 

                return Mono.just(response.labels().get(bestLabelIndex));
            })
            .switchIfEmpty(Mono.defer(() -> {
                 log.warn("Response Mono from AI was empty after bodyToMono (should not happen if API returned 200 and valid JSON).");
                 return Mono.just("Não foi possível gerar uma sugestão no momento (resposta vazia do modelo).");
            }))
            .onErrorResume(e -> {
                log.error("Error in AI insight generation pipeline: {}", e.getMessage(), e);
                return Mono.just("Desculpe, ocorreu um erro ao tentar gerar sua sugestão personalizada via IA.");
            });
    }
} 