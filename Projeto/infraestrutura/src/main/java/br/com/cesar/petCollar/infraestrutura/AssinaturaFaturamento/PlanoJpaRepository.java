package br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoJpaRepository extends JpaRepository<PlanoJpa, String> {
    Optional<PlanoJpa> findByNomeIgnoreCase(String nome);
}
