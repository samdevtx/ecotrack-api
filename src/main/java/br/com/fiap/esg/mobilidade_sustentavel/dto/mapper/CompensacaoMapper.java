package br.com.fiap.esg.mobilidade_sustentavel.dto.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import br.com.fiap.esg.mobilidade_sustentavel.dto.CompensacaoRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.CompensacaoResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.model.Compensacao;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;

public class CompensacaoMapper {

    private CompensacaoMapper() {}

    public static Compensacao toEntity(CompensacaoRequestDto dto, Usuario usuario) {
        if (dto == null || usuario == null) {
            return null;
        }
        Compensacao compensacao = new Compensacao();
        compensacao.setUsuario(usuario);
        compensacao.setTipo(dto.tipo());
        compensacao.setQuantidade(dto.quantidade());
        compensacao.setDataRegistro(LocalDateTime.now());  
        return compensacao;
    }

    public static CompensacaoResponseDto toResponseDto(Compensacao compensacao) {
        if (compensacao == null) {
            return null;
        }
        return new CompensacaoResponseDto(
                compensacao.getId(),
                compensacao.getUsuario() != null ? compensacao.getUsuario().getId() : null,
                compensacao.getTipo(),
                compensacao.getQuantidade(),
                compensacao.getDataRegistro()
        );
    }

    public static List<CompensacaoResponseDto> toResponseDtoList(List<Compensacao> compensacoes) {
        if (compensacoes == null) {
            return null;
        }
        return compensacoes.stream()
                .map(CompensacaoMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
     
    public static void updateEntityFromDto(CompensacaoRequestDto dto, Compensacao entity) {
        if (dto == null || entity == null) {
            return;
        }
         
         
        entity.setTipo(dto.tipo());
        entity.setQuantidade(dto.quantidade());

         
         
    }
} 