package br.com.fiap.esg.mobilidade_sustentavel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.esg.mobilidade_sustentavel.model.Compensacao;

@Repository
public interface CompensacaoRepository extends JpaRepository<Compensacao, Long> {

    List<Compensacao> findByUsuarioId(Long usuarioId);

     
     
} 