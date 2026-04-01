package br.com.fiap.esg.mobilidade_sustentavel.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.esg.mobilidade_sustentavel.config.Co2EmissionConfig;
import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemRequestDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.ViagemResponseDto;
import br.com.fiap.esg.mobilidade_sustentavel.dto.mapper.ViagemMapper;
import br.com.fiap.esg.mobilidade_sustentavel.exception.ResourceNotFoundException;
import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;
import br.com.fiap.esg.mobilidade_sustentavel.model.Viagem;
import br.com.fiap.esg.mobilidade_sustentavel.repository.ViagemRepository;

@Service
public class ViagemService {

    private final ViagemRepository viagemRepository;
    private final Co2EmissionConfig co2EmissionConfig;

    @Autowired
    public ViagemService(ViagemRepository viagemRepository, Co2EmissionConfig co2EmissionConfig) {
        this.viagemRepository = viagemRepository;
        this.co2EmissionConfig = co2EmissionConfig;
    }

    @Transactional
    public ViagemResponseDto criarViagem(ViagemRequestDto viagemRequestDto, Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuário autenticado inválido ou não persistido.");
        }
        
        Viagem viagem = ViagemMapper.toEntity(viagemRequestDto, usuario);
        
        BigDecimal co2Calculado = calcularCO2(viagem.getTransporte(), viagem.getDistanciaKm());
        viagem.setCo2(co2Calculado);

        Viagem viagemSalva = viagemRepository.save(viagem);
        return ViagemMapper.toResponseDto(viagemSalva);
    }

    @Transactional(readOnly = true)
    public Optional<ViagemResponseDto> buscarViagemPorId(Long id) {
        return viagemRepository.findById(id).map(ViagemMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public ViagemResponseDto getViagemByIdOrThrow(Long id) {
        return viagemRepository.findById(id)
            .map(ViagemMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Viagem", "id", id));
    }

    @Transactional(readOnly = true)
    public List<ViagemResponseDto> listarViagensPorUsuario(Long usuarioId) {
        return ViagemMapper.toResponseDtoList(viagemRepository.findByUsuarioId(usuarioId));
    }

    @Transactional(readOnly = true)
    public List<ViagemResponseDto> listarTodasViagens() {
        return ViagemMapper.toResponseDtoList(viagemRepository.findAll());
    }

    @Transactional
    public ViagemResponseDto atualizarViagem(Long id, ViagemRequestDto viagemRequestDto, Usuario usuarioLogado) {
        Viagem viagemExistente = viagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Viagem", "id", id));
        
        if (!viagemExistente.getUsuario().getId().equals(usuarioLogado.getId())) {
            throw new SecurityException("Usuário não autorizado a atualizar esta viagem."); 
        }

        ViagemMapper.updateEntityFromDto(viagemRequestDto, viagemExistente);

        BigDecimal co2Recalculado = calcularCO2(viagemExistente.getTransporte(), viagemExistente.getDistanciaKm());
        viagemExistente.setCo2(co2Recalculado);

        Viagem viagemAtualizada = viagemRepository.save(viagemExistente);
        return ViagemMapper.toResponseDto(viagemAtualizada);
    }

    @Transactional
    public void deletarViagem(Long id, Usuario usuarioLogado) {
        Viagem viagemExistente = viagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Viagem", "id", id));
        
        if (!viagemExistente.getUsuario().getId().equals(usuarioLogado.getId())) {
            throw new SecurityException("Usuário não autorizado a deletar esta viagem.");
        }

        viagemRepository.deleteById(id);
    }

    private BigDecimal calcularCO2(String transporte, BigDecimal distanciaKm) {
        if (transporte == null || distanciaKm == null || distanciaKm.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        
        BigDecimal factor = co2EmissionConfig.getFactorForTransport(transporte);
        return distanciaKm.multiply(factor).setScale(3, RoundingMode.HALF_UP);
    }
} 