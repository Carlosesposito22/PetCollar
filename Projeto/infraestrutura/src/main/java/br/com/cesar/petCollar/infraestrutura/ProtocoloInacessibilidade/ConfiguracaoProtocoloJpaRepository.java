package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoProtocoloJpaRepository
        extends JpaRepository<ConfiguracaoProtocoloJpa, String> {

    Optional<ConfiguracaoProtocoloJpa> findTopByOrderByVersaoDesc();
}
