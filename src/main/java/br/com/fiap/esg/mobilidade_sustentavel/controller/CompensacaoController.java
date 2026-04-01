package br.com.fiap.esg.mobilidade_sustentavel.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.esg.mobilidade_sustentavel.dto.CompensacaoRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.CompensacaoResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.service.CompensacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/compensacoes")
public class CompensacaoController {

    private final CompensacaoService compensacaoService;

    @Autowired
    public CompensacaoController(CompensacaoService compensacaoService) {
        this.compensacaoService = compensacaoService;
    }

    @PostMapping
    public ResponseEntity<CompensacaoResponseDto> registrarCompensacao(@RequestBody @Valid CompensacaoRequestDto compensacaoRequestDto,
                                                                      @AuthenticationPrincipal Usuario usuarioAutenticado) {
        CompensacaoResponseDto novaCompensacao = compensacaoService.registrarCompensacao(compensacaoRequestDto, usuarioAutenticado);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCompensacao);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #usuarioId == null or #usuarioId == authentication.principal.id")
    public ResponseEntity<List<CompensacaoResponseDto>> listarCompensacoes(
            @RequestParam(required = false) Long usuarioId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        
        List<CompensacaoResponseDto> compensacoes;
        if (usuarioId != null) {
            compensacoes = compensacaoService.listarCompensacoesPorUsuario(usuarioId);
        } else {
            if (usuarioAutenticado.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"))) {
                compensacoes = compensacaoService.listarTodasCompensacoes();
            } else {
                compensacoes = compensacaoService.listarCompensacoesPorUsuario(usuarioAutenticado.getId());
            }
        }
        return ResponseEntity.ok(compensacoes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompensacaoResponseDto> buscarCompensacaoPorId(@PathVariable Long id,
                                                                        @AuthenticationPrincipal Usuario usuarioAutenticado) {
        CompensacaoResponseDto compensacao = compensacaoService.getCompensacaoByIdOrThrow(id);
        boolean isAdmin = usuarioAutenticado.getAuthorities().stream()
                            .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !compensacao.usuarioId().equals(usuarioAutenticado.getId())) {
            throw new SecurityException("Usuário não autorizado a visualizar esta compensação.");
        }
        return ResponseEntity.ok(compensacao);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CompensacaoResponseDto> atualizarCompensacao(@PathVariable Long id,
                                                                       @RequestBody @Valid CompensacaoRequestDto compensacaoRequestDto,
                                                                       @AuthenticationPrincipal Usuario usuarioAutenticado) {
        CompensacaoResponseDto compensacaoAtualizada = compensacaoService.atualizarCompensacao(id, compensacaoRequestDto, usuarioAutenticado);
        return ResponseEntity.ok(compensacaoAtualizada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletarCompensacao(@PathVariable Long id,
                                                   @AuthenticationPrincipal Usuario usuarioAutenticado) {
        compensacaoService.deletarCompensacao(id, usuarioAutenticado);
        return ResponseEntity.noContent().build();
    }
} 