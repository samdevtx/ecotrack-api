package br.com.fiap.esg.mobilidade_sustentavel.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.esg.mobilidade_sustentavel.dto.CompensacaoRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.CompensacaoResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.mapper.CompensacaoMapper;
import br.com.fiap.esg.mobilidade_sustentavel.exception.ResourceNotFoundException;
import br.com.fiap.esg.mobilidade_sustentavel.model.Compensacao;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.repository.CompensacaoRepository;
import br.com.fiap.esg.mobilidade_sustentavel.repository.UsuarioRepository;

@Service
public class CompensacaoService {

    private final CompensacaoRepository compensacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CompensacaoService(CompensacaoRepository compensacaoRepository, UsuarioRepository usuarioRepository) {
        this.compensacaoRepository = compensacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public CompensacaoResponseDto registrarCompensacao(CompensacaoRequestDto compensacaoRequestDto, Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuário autenticado inválido ou não persistido.");
        }
        
        Compensacao compensacao = CompensacaoMapper.toEntity(compensacaoRequestDto, usuario);
         

        Compensacao compensacaoSalva = compensacaoRepository.save(compensacao);
        return CompensacaoMapper.toResponseDto(compensacaoSalva);
    }

    @Transactional(readOnly = true)
    public Optional<CompensacaoResponseDto> buscarCompensacaoPorId(Long id) {
        return compensacaoRepository.findById(id).map(CompensacaoMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public CompensacaoResponseDto getCompensacaoByIdOrThrow(Long id) {
        return compensacaoRepository.findById(id)
            .map(CompensacaoMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Compensacao", "id", id));
    }

    @Transactional(readOnly = true)
    public List<CompensacaoResponseDto> listarCompensacoesPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario", "id", usuarioId);
        }
        return CompensacaoMapper.toResponseDtoList(compensacaoRepository.findByUsuarioId(usuarioId));
    }

    @Transactional(readOnly = true)
    public List<CompensacaoResponseDto> listarTodasCompensacoes() {
        return CompensacaoMapper.toResponseDtoList(compensacaoRepository.findAll());
    }

    @Transactional
    public CompensacaoResponseDto atualizarCompensacao(Long id, CompensacaoRequestDto compensacaoRequestDto, Usuario usuarioLogado) {
        Compensacao compensacaoExistente = compensacaoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Compensacao", "id", id));

        if (!compensacaoExistente.getUsuario().getId().equals(usuarioLogado.getId())) {
            throw new AccessDeniedException("Usuário não autorizado a atualizar esta compensação.");
        }

        CompensacaoMapper.updateEntityFromDto(compensacaoRequestDto, compensacaoExistente);
        
        Compensacao compensacaoAtualizada = compensacaoRepository.save(compensacaoExistente);
        return CompensacaoMapper.toResponseDto(compensacaoAtualizada);
    }

    @Transactional
    public void deletarCompensacao(Long id, Usuario usuarioLogado) {
        Compensacao compensacaoExistente = compensacaoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Compensacao", "id", id));

        if (!compensacaoExistente.getUsuario().getId().equals(usuarioLogado.getId())) {
            throw new AccessDeniedException("Usuário não autorizado a deletar esta compensação.");
        }
        compensacaoRepository.deleteById(id);
    }
} 