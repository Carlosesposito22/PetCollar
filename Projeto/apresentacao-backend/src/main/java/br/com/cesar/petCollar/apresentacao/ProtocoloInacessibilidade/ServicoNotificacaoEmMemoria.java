package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelCriticidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ConteudoNotificacao;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IServicoNotificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementação em memória desativada (não é bean Spring). A persistência real é
 * feita por {@code ServicoNotificacaoJpa} na camada de infraestrutura (RN 16).
 * Mantida apenas como referência/fallback local de desenvolvimento.
 */
public class ServicoNotificacaoEmMemoria implements IServicoNotificacao {

    private static final Logger log = LoggerFactory.getLogger(ServicoNotificacaoEmMemoria.class);

    /** Registros por protocoloId. CopyOnWriteArrayList garante iteração segura. */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<RegistroNotificacao>> registros =
        new ConcurrentHashMap<>();

    // ── IServicoNotificacao ───────────────────────────────────────────────────

    @Override
    public void notificar(String destinatarioId, ConteudoNotificacao conteudo,
                          NivelCriticidade criticidade) {
        // Chamada sem contexto de protocolo: apenas loga (não persiste).
        log.info("[NOTIFICAÇÃO {} → {}] {} — {}",
            criticidade, destinatarioId, conteudo.getTitulo(), conteudo.getCorpo());
    }

    @Override
    public void notificar(String destinatarioId, ConteudoNotificacao conteudo,
                          NivelCriticidade criticidade, String protocoloId) {
        log.info("[NOTIFICAÇÃO {} | protocolo:{} → {}] {} — {}",
            criticidade, protocoloId, destinatarioId, conteudo.getTitulo(), conteudo.getCorpo());

        RegistroNotificacao registro = new RegistroNotificacao(
            UUID.randomUUID().toString(),
            protocoloId,
            destinatarioId,
            conteudo.getTitulo(),
            conteudo.getCorpo(),
            criticidade.name(),
            LocalDateTime.now()
        );

        registros.computeIfAbsent(protocoloId, k -> new CopyOnWriteArrayList<>())
                 .add(registro);
    }

    // ── Consulta (usada pelo NotificacaoController) ───────────────────────────

    public List<RegistroNotificacao> listarPorProtocolo(String protocoloId) {
        List<RegistroNotificacao> lista = registros.getOrDefault(protocoloId, new CopyOnWriteArrayList<>());
        List<RegistroNotificacao> ordenada = new ArrayList<>(lista);
        ordenada.sort((a, b) -> b.registradoEm().compareTo(a.registradoEm()));
        return Collections.unmodifiableList(ordenada);
    }

    // ── VO interno ──────────────────────────────────────────────────────────

    public record RegistroNotificacao(
        String id,
        String protocoloId,
        String destinatarioId,
        String titulo,
        String corpo,
        String criticidade,
        LocalDateTime registradoEm
    ) {}
}
