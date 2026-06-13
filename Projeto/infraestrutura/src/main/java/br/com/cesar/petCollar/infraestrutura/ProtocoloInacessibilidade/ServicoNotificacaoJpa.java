package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaNotificacaoProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.RegistroNotificacaoProtocolo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementação primária da porta {@link IServicoNotificacao} com persistência
 * real em banco de dados (RN 16 — auditabilidade completa). Implementa também
 * {@link IConsultaNotificacaoProtocolo} para que o controller consulte o histórico
 * sem acoplamento direto à infraestrutura.
 */
@Primary
@Service
public class ServicoNotificacaoJpa implements IServicoNotificacao, IConsultaNotificacaoProtocolo {

    private static final Logger log = LoggerFactory.getLogger(ServicoNotificacaoJpa.class);

    private final NotificacaoProtocoloJpaRepository repositorio;

    public ServicoNotificacaoJpa(NotificacaoProtocoloJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    // ── IServicoNotificacao ──────────────────────────────────────────────────

    @Override
    public void notificar(String destinatarioId, ConteudoNotificacao conteudo,
                          NivelCriticidade criticidade) {
        // Sem contexto de protocolo: apenas loga — não persiste pois não há chave de auditoria.
        log.info("[NOTIFICAÇÃO {} → {}] {} — {}",
            criticidade, destinatarioId, conteudo.getTitulo(), conteudo.getCorpo());
    }

    @Override
    @Transactional
    public void notificar(String destinatarioId, ConteudoNotificacao conteudo,
                          NivelCriticidade criticidade, String protocoloId) {
        log.info("[NOTIFICAÇÃO {} | protocolo:{} → {}] {} — {}",
            criticidade, protocoloId, destinatarioId, conteudo.getTitulo(), conteudo.getCorpo());

        repositorio.save(NotificacaoProtocoloJpa.criar(
            UUID.randomUUID().toString(),
            protocoloId,
            destinatarioId,
            conteudo.getTitulo(),
            conteudo.getCorpo(),
            criticidade.name(),
            LocalDateTime.now()));
    }

    // ── IConsultaNotificacaoProtocolo ────────────────────────────────────────

    @Override
    public List<RegistroNotificacaoProtocolo> listarPorProtocolo(String protocoloId) {
        return repositorio.findByProtocoloIdOrderByRegistradoEmDesc(protocoloId)
            .stream()
            .map(NotificacaoProtocoloJpa::toDomain)
            .toList();
    }
}
