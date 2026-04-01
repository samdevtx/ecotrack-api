package br.com.fiap.esg.mobilidade_sustentavel.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;

import br.com.fiap.esg.mobilidade_sustentavel.dto.UsuarioRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.UsuarioResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;

public class UsuarioMapper {

     
    private UsuarioMapper() {}

    public static Usuario toEntity(UsuarioRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        Usuario usuario = new Usuario();
        usuario.setNome(requestDto.getNome());
        usuario.setEmail(requestDto.getEmail());
        usuario.setSenha(requestDto.getSenha());  
        return usuario;
    }

    public static UsuarioResponseDto toResponseDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }

    public static List<UsuarioResponseDto> toResponseDtoList(List<Usuario> usuarios) {
        if (usuarios == null) {
            return List.of();  
        }
        return usuarios.stream()
                .map(UsuarioMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromDto(UsuarioRequestDto dto, Usuario entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.getNome());
        entity.setEmail(dto.getEmail());
         
         
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            entity.setSenha(dto.getSenha());  
        }
    }

    public static UsuarioResponseDto toDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }

    public static List<UsuarioResponseDto> toDto(List<Usuario> usuarios) {
        if (usuarios == null) {
            return null;
        }
        return usuarios.stream()
                .map(UsuarioMapper::toDto)
                .collect(Collectors.toList());
    }
} 