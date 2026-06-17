package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoProtocoloJpaRepository
        extends JpaRepository<NotificacaoProtocoloJpa, String> {

    List<NotificacaoProtocoloJpa> findByProtocoloIdOrderByRegistradoEmDesc(String protocoloId);
}
