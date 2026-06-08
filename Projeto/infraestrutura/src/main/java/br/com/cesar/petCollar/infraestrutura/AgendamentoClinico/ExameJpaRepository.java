package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExameJpaRepository extends JpaRepository<ExameJpa, String> {

    List<ExameJpa> findByConsultaOrigemId(String consultaOrigemId);

    long countByConsultaOrigemIdAndStatus(String consultaOrigemId, String status);
}
