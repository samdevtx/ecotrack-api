package br.com.fiap.esg.mobilidade_sustentavel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioRequestDto {

    @NotBlank(message = "Nome não pode estar em branco")
    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    private String nome;

    @NotBlank(message = "Email não pode estar em branco")
    @Email(message = "Formato de email inválido")
    @Size(max = 255, message = "Email não pode exceder 255 caracteres")
    private String email;

    @NotBlank(message = "Senha não pode estar em branco")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")  
    private String senha;

     
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
} 