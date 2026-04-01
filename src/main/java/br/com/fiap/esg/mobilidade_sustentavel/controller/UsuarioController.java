package br.com.fiap.esg.mobilidade_sustentavel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import br.com.fiap.esg.mobilidade_sustentavel.dto.UsuarioRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.UsuarioResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> criarUsuario(@RequestBody @Valid UsuarioRequestDto usuarioRequestDto) {
        UsuarioResponseDto novoUsuario = usuarioService.criarUsuario(usuarioRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioResponseDto>> listarUsuarios(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email) {
        Page<UsuarioResponseDto> usuariosPage = usuarioService.listarUsuarios(pageable, nome, email);
        return ResponseEntity.ok(usuariosPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UsuarioResponseDto> buscarUsuarioPorId(@PathVariable Long id,
                                                                 @AuthenticationPrincipal Usuario usuarioAutenticado) {
        UsuarioResponseDto usuario = usuarioService.getUsuarioByIdOrThrow(id);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UsuarioResponseDto> atualizarUsuario(@PathVariable Long id,
                                                               @RequestBody @Valid UsuarioRequestDto usuarioRequestDto,
                                                               @AuthenticationPrincipal Usuario usuarioAutenticado) {
        UsuarioResponseDto usuarioAtualizado = usuarioService.atualizarUsuario(id, usuarioRequestDto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id,
                                               @AuthenticationPrincipal Usuario usuarioAutenticado) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }

     
} 