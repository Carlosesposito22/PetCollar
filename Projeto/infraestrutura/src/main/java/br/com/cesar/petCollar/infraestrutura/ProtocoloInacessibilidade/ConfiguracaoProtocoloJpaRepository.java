package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório Spring Data do agregado {@link ConfiguracaoProtocoloJpa}. A
 * configuração vigente é a de maior versão.
 */
public interface ConfiguracaoProtocoloJpaRepository
        extends JpaRepository<ConfiguracaoProtocoloJpa, String> {

    Optional<ConfiguracaoProtocoloJpa> findTopByOrderByVersaoDesc();
}
