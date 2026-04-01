package br.com.fiap.esg.mobilidade_sustentavel.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import br.com.fiap.esg.mobilidade_sustentavel.model.Usuario;

@Component  
public class UsuarioSpecification {

     
    public static Specification<Usuario> nomeContains(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;  
        }
        return (root, query, criteriaBuilder) -> 
               criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase().trim() + "%");
    }

    public static Specification<Usuario> emailContains(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> 
               criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase().trim() + "%");
    }
} 