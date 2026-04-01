package br.com.fiap.esg.mobilidade_sustentavel.dto.mapper;

import java.util.List;
import java.util.stream.Collectors;

import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.model.Viagem;

public class ViagemMapper {

    private ViagemMapper() {}

    public static Viagem toEntity(ViagemRequestDto requestDto, Usuario usuario) {
        if (requestDto == null || usuario == null) {
            return null;
        }
        Viagem viagem = new Viagem();
        viagem.setUsuario(usuario);
        viagem.setTransporte(requestDto.transporte());
        viagem.setDistanciaKm(requestDto.distanciaKm());
        viagem.setDataHora(requestDto.dataHora());
         
        return viagem;
    }

    public static ViagemResponseDto toResponseDto(Viagem viagem) {
        if (viagem == null) {
            return null;
        }
        return new ViagemResponseDto(
                viagem.getId(),
                viagem.getUsuario() != null ? viagem.getUsuario().getId() : null,
                viagem.getUsuario() != null ? viagem.getUsuario().getNome() : "Usuário Desconhecido",
                viagem.getTransporte(),
                viagem.getDistanciaKm(),
                viagem.getCo2(),
                viagem.getDataHora()
        );
    }

    public static List<ViagemResponseDto> toResponseDtoList(List<Viagem> viagens) {
        if (viagens == null) {
            return null;
        }
        return viagens.stream()
                .map(ViagemMapper::toResponseDto)
                .collect(Collectors.toList());
    }

     
     
    public static void updateEntityFromDto(ViagemRequestDto dto, Viagem entity) {
        if (dto == null || entity == null) {
            return;
        }
         
         
        entity.setTransporte(dto.transporte());
        entity.setDistanciaKm(dto.distanciaKm());
        entity.setDataHora(dto.dataHora());
        
         
         
    }
} 