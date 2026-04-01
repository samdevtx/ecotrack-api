package br.com.fiap.esg.mobilidade_sustentavel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ViagemResponseDto {

    private Long id;
    private Long usuarioId;  
    private String nomeUsuario;  
    private String transporte;
    private BigDecimal distanciaKm;
    private BigDecimal co2;
    private LocalDateTime dataHora;

     
    public ViagemResponseDto() {
    }

    public ViagemResponseDto(Long id, Long usuarioId, String nomeUsuario, String transporte, BigDecimal distanciaKm, BigDecimal co2, LocalDateTime dataHora) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nomeUsuario = nomeUsuario;
        this.transporte = transporte;
        this.distanciaKm = distanciaKm;
        this.co2 = co2;
        this.dataHora = dataHora;
    }

     
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getTransporte() {
        return transporte;
    }

    public void setTransporte(String transporte) {
        this.transporte = transporte;
    }

    public BigDecimal getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(BigDecimal distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public BigDecimal getCo2() {
        return co2;
    }

    public void setCo2(BigDecimal co2) {
        this.co2 = co2;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
} 