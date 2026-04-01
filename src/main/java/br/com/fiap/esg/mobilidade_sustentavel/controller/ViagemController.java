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

import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.service.ViagemService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/viagens")
public class ViagemController {

    private final ViagemService viagemService;

    @Autowired
    public ViagemController(ViagemService viagemService) {
        this.viagemService = viagemService;
    }

    @PostMapping
    public ResponseEntity<ViagemResponseDto> registrarViagem(@RequestBody @Valid ViagemRequestDto viagemRequestDto,
                                                             @AuthenticationPrincipal Usuario usuarioAutenticado) {
        ViagemResponseDto novaViagem = viagemService.criarViagem(viagemRequestDto, usuarioAutenticado);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaViagem);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #usuarioId == null or #usuarioId == authentication.principal.id")
    public ResponseEntity<List<ViagemResponseDto>> listarViagensPorUsuario(
            @RequestParam(required = false) Long usuarioId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        
        Long idParaConsulta = (usuarioId == null) ? usuarioAutenticado.getId() : usuarioId;
         
         
         
         
         
         
         
         
         
         
         

        List<ViagemResponseDto> viagens = viagemService.listarViagensPorUsuario(idParaConsulta);
        return ResponseEntity.ok(viagens);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @viagemService.getViagemByIdOrThrow(#id).getUsuarioId() == authentication.principal.id")
    public ResponseEntity<ViagemResponseDto> buscarViagemPorId(@PathVariable Long id,
                                                                 @AuthenticationPrincipal Usuario usuarioAutenticado) {
        ViagemResponseDto viagem = viagemService.getViagemByIdOrThrow(id);
        return ResponseEntity.ok(viagem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ViagemResponseDto> atualizarViagem(@PathVariable Long id, 
                                                             @RequestBody @Valid ViagemRequestDto viagemRequestDto,
                                                             @AuthenticationPrincipal Usuario usuarioAutenticado) {
        ViagemResponseDto viagemAtualizada = viagemService.atualizarViagem(id, viagemRequestDto, usuarioAutenticado);
        return ResponseEntity.ok(viagemAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarViagem(@PathVariable Long id, 
                                              @AuthenticationPrincipal Usuario usuarioAutenticado) {
        viagemService.deletarViagem(id, usuarioAutenticado);
        return ResponseEntity.noContent().build();
    }
} 