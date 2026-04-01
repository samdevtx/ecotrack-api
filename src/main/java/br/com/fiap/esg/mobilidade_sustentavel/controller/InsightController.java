package br.com.fiap.esg.mobilidade_sustentavel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.service.InsightService;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private static final Logger log = LoggerFactory.getLogger(InsightController.class);

    private final InsightService insightService;

    @Autowired
    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN') or #usuarioId == authentication.principal.id")
    public Mono<String> getInsightsPorUsuario(@PathVariable Long usuarioId,
                                              @AuthenticationPrincipal Usuario usuarioAutenticado) {
        log.info("CONTROLLER: Iniciando getInsightsPorUsuario para usuário ID: {}", usuarioId);
        return insightService.gerarInsightsSustentabilidade(usuarioId)
            .doOnSuccess(result -> log.info("CONTROLLER: Mono<String> success for user ID: {}. Result: {}...", usuarioId, result.substring(0, Math.min(result.length(), 50))))
            .doOnError(error -> log.error("CONTROLLER: Mono<String> error for user ID: {}: {}", usuarioId, error.getMessage()));
    }

     
     
     
} 