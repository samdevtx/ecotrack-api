package br.com.fiap.esg.mobilidade_sustentavel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "USUARIOS")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Nome não pode estar em branco")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(name = "NOME", nullable = false)
    private String nome;

    @NotBlank(message = "Email não pode estar em branco")
    @Email(message = "Formato de email inválido")
    @Size(max = 100, message = "Email não pode exceder 100 caracteres")
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Senha não pode estar em branco")
     
    @Column(name = "SENHA", nullable = false)
    private String senha;

    @Column(name = "ROLES", nullable = false)
    private String roles = "ROLE_USER";  

    @Column(name = "ENABLED", nullable = false)
    private boolean enabled = true;  

    @Column(name = "FAILED_LOGIN_ATTEMPTS")
    private int failedLoginAttempts = 0;  

    @Column(name = "ACCOUNT_LOCKED_UNTIL")
    private LocalDateTime accountLockedUntil;  

    @Column(name = "ACCOUNT_EXPIRATION_DATE")
    private LocalDate accountExpirationDate;  

     
    public Usuario() {
    }

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.enabled = true;  
        this.failedLoginAttempts = 0;  
        this.accountLockedUntil = null;  
        this.accountExpirationDate = null;  
    }

    public Usuario(String nome, String email, String senha, String roles) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.roles = roles;
        this.enabled = true; 
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
        this.accountExpirationDate = null;  
    }

     
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public boolean getEnabled() {  
        return enabled;
    }

    public void setEnabled(boolean enabled) {  
        this.enabled = enabled;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getAccountLockedUntil() {
        return accountLockedUntil;
    }

    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }

    public LocalDate getAccountExpirationDate() {
        return accountExpirationDate;
    }

    public void setAccountExpirationDate(LocalDate accountExpirationDate) {
        this.accountExpirationDate = accountExpirationDate;
    }

     
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.roles == null || this.roles.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(this.roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
         
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
         
        return this.accountExpirationDate == null || LocalDate.now().isBefore(this.accountExpirationDate) || LocalDate.now().isEqual(this.accountExpirationDate);
    }

    @Override
    public boolean isAccountNonLocked() {
         
        return this.accountLockedUntil == null || LocalDateTime.now().isAfter(this.accountLockedUntil);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;  
    }

     
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", email='" + email + '\'' +
               ", roles='" + roles + '\'' +
               ", enabled=" + enabled +
               ", failedLoginAttempts=" + failedLoginAttempts +
               ", accountLockedUntil=" + accountLockedUntil +
               ", accountExpirationDate=" + accountExpirationDate +
               '}';
    }
} 