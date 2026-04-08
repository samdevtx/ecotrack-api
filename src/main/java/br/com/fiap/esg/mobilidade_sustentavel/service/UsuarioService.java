package br.com.fiap.esg.mobilidade_sustentavel.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.esg.mobilidade_sustentavel.dto.UsuarioRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.UsuarioResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.mapper.UsuarioMapper;
import br.com.fiap.esg.mobilidade_sustentavel.exception.EmailJaCadastradoException;
import br.com.fiap.esg.mobilidade_sustentavel.exception.ResourceNotFoundException;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.repository.UsuarioRepository;
import br.com.fiap.esg.mobilidade_sustentavel.repository.specification.UsuarioSpecification;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDto criarUsuario(UsuarioRequestDto usuarioRequestDto) {
        if (usuarioRepository.findByEmail(usuarioRequestDto.getEmail()).isPresent()) {
            throw new EmailJaCadastradoException(usuarioRequestDto.getEmail());
        }
        Usuario usuario = UsuarioMapper.toEntity(usuarioRequestDto);
        usuario.setSenha(passwordEncoder.encode(usuarioRequestDto.getSenha()));
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return UsuarioMapper.toResponseDto(usuarioSalvo);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDto> buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).map(UsuarioMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDto getUsuarioByIdOrThrow(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioResponseDto> buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).map(UsuarioMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listarTodosUsuariosSemPaginacao() {
        return UsuarioMapper.toResponseDtoList(usuarioRepository.findAll());
    }

    @Transactional
    public UsuarioResponseDto atualizarUsuario(Long id, UsuarioRequestDto usuarioRequestDto) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (!usuarioExistente.getEmail().equals(usuarioRequestDto.getEmail()) && 
            usuarioRepository.findByEmail(usuarioRequestDto.getEmail()).filter(u -> !u.getId().equals(id)).isPresent()) {
            throw new EmailJaCadastradoException(usuarioRequestDto.getEmail());
        }

        UsuarioMapper.updateEntityFromDto(usuarioRequestDto, usuarioExistente);

        if (usuarioRequestDto.getSenha() != null && !usuarioRequestDto.getSenha().isBlank()) {
            usuarioExistente.setSenha(passwordEncoder.encode(usuarioRequestDto.getSenha()));
        }
        Usuario usuarioAtualizado = usuarioRepository.save(usuarioExistente);
        return UsuarioMapper.toResponseDto(usuarioAtualizado);
    }

    @Transactional
    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDto> listarUsuarios(Pageable pageable, String nomeFilter, String emailFilter) {
        Specification<Usuario> spec = null;

        if (nomeFilter != null && !nomeFilter.trim().isEmpty()) {
            spec = spec.and(UsuarioSpecification.nomeContains(nomeFilter));
        }

        if (emailFilter != null && !emailFilter.trim().isEmpty()) {
            spec = spec.and(UsuarioSpecification.emailContains(emailFilter));
        }
        
        Page<Usuario> usuariosPage = usuarioRepository.findAll(spec, pageable);
        return usuariosPage.map(UsuarioMapper::toResponseDto);
    }
} 