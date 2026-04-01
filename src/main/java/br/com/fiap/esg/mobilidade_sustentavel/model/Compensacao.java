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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "COMPENSACOES")
public class Compensacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    @NotNull(message = "Usuário da compensação não pode ser nulo")
    private Usuario usuario;

    @NotBlank(message = "Tipo de compensação não pode estar em branco")
    @Size(max = 100, message = "Tipo de compensação não pode exceder 100 caracteres")
    @Column(name = "TIPO")  
    private String tipo;

    @NotNull(message = "Quantidade não pode ser nula")
    @PositiveOrZero(message = "Quantidade deve ser positiva ou zero")
    @Column(name = "QUANTIDADE", scale = 2)
    private BigDecimal quantidade;

    @NotNull(message = "Data de registro não pode ser nula")
    @Column(name = "DATA_REGISTRO", nullable = false)
    private LocalDateTime dataRegistro;

     
    public Compensacao() {
    }

    public Compensacao(Usuario usuario, String tipo, BigDecimal quantidade, LocalDateTime dataRegistro) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.dataRegistro = dataRegistro;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

     
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compensacao that = (Compensacao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Compensacao{" +
                "id=" + id +
                ", usuarioId=" + (usuario != null ? usuario.getId() : null) +
                ", tipo='" + tipo + '\'' +
                ", quantidade=" + quantidade +
                ", dataRegistro=" + dataRegistro +
                '}';
    }
} 