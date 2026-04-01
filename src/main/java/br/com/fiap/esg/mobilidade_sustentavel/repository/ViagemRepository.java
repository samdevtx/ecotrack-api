package br.com.fiap.esg.mobilidade_sustentavel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.esg.mobilidade_sustentavel.model.Viagem;

@Repository
public interface ViagemRepository extends JpaRepository<Viagem, Long> {

    List<Viagem> findByUsuarioId(Long usuarioId);

     
     
} 