package br.com.fiap.esg.mobilidade_sustentavel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "VIAGENS")
public class Viagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    @NotNull(message = "Usuário da viagem não pode ser nulo")
    private Usuario usuario;

    @Size(max = 100, message = "Transporte não pode exceder 100 caracteres")
    @Column(name = "TRANSPORTE")
    private String transporte;

    @PositiveOrZero(message = "Distância deve ser positiva ou zero")
    @Column(name = "DISTANCIA_KM", scale = 2)  
    private BigDecimal distanciaKm;

    @Column(name = "CO2_EMITIDO", scale = 2)
    private BigDecimal co2;

    @NotNull(message = "Data e hora da viagem não podem ser nulas")
    @Column(name = "DATA_HORA", nullable = false)
    private LocalDateTime dataHora;

     
    public Viagem() {
    }

    public Viagem(Usuario usuario, String transporte, BigDecimal distanciaKm, BigDecimal co2, LocalDateTime dataHora) {
        this.usuario = usuario;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

     
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Viagem viagem = (Viagem) o;
        return Objects.equals(id, viagem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Viagem{" +
                "id=" + id +
                ", usuarioId=" + (usuario != null ? usuario.getId() : null) +
                ", transporte='" + transporte + '\'' +
                ", distanciaKm=" + distanciaKm +
                ", co2Emitido=" + co2 +
                ", dataHora=" + dataHora +
                '}';
    }
} 