package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa.OrquestradorEtapasProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.IProtocoloInacessibilidadeRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.AtivacaoProtocoloService;
import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.FilaAtendimentoEmMemoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Gatilho periódico da F-03 (RN 1). É responsabilidade <b>da infraestrutura/
 * apresentação</b>, não do domínio: a cada minuto ele (1) ativa por timeout os
 * atendimentos sem resposta, via {@link AtivacaoProtocoloService}, e (2) faz cada
 * protocolo ativo avançar uma etapa, via {@link OrquestradorEtapasProtocolo} — que
 * por baixo despacha a subclasse adequada do Template Method (tutor → secundários
 * → escalonamento).
 *
 * <p>Tanto a ativação quanto o avanço são idempotentes em relação ao estado: rodar
 * repetidamente não cria protocolos duplicados nem executa etapas em estados
 * encerrados (o esqueleto do Template valida o estado de entrada).
 */
@Component
public class MonitorTimeoutTutorScheduler {

    private static final Logger log = LoggerFactory.getLogger(MonitorTimeoutTutorScheduler.class);

    private final AtivacaoProtocoloService ativacaoService;
    private final OrquestradorEtapasProtocolo orquestrador;
    private final IProtocoloInacessibilidadeRepositorio protocoloRepositorio;
    private final FilaAtendimentoEmMemoria fila;

    public MonitorTimeoutTutorScheduler(AtivacaoProtocoloService ativacaoService,
                                        OrquestradorEtapasProtocolo orquestrador,
                                        IProtocoloInacessibilidadeRepositorio protocoloRepositorio,
                                        FilaAtendimentoEmMemoria fila) {
        this.ativacaoService = ativacaoService;
        this.orquestrador = orquestrador;
        this.protocoloRepositorio = protocoloRepositorio;
        this.fila = fila;
    }

    @Scheduled(fixedDelay = 60_000)
    public void monitorar() {
        try {
            int ativos = ativacaoService.verificarTodosAtendimentosAtivos();
            log.debug("[MONITOR F-03] varredura de timeout concluída — protocolos ativos: {}", ativos);
        } catch (Exception e) {
            log.warn("[MONITOR F-03] falha na varredura de timeout: {}", e.getMessage());
        }

        // Avança cada protocolo ativo uma etapa (Template Method via orquestrador).
        List<ProtocoloInacessibilidade> ativos = protocoloRepositorio.listarAtivos();
        for (ProtocoloInacessibilidade protocolo : ativos) {
            try {
                orquestrador.executarProximaEtapa(protocolo);
                // O serviço de etapa recarrega o agregado internamente; relemos do banco
                // para verificar se o protocolo foi encerrado por esgotamento.
                protocoloRepositorio.buscarPorId(protocolo.getId()).ifPresent(atualizado -> {
                    if (atualizado.getStatus() == StatusProtocolo.ENCERRADO_POR_ESGOTAMENTO) {
                        fila.removerPorPaciente(atualizado.getPacienteId().getValor());
                        log.info("[MONITOR F-03] paciente {} removido da fila por esgotamento do protocolo {}.",
                            atualizado.getPacienteId().getValor(), atualizado.getId().getValor());
                    }
                });
            } catch (Exception e) {
                log.warn("[MONITOR F-03] falha ao avançar o protocolo {}: {}",
                    protocolo.getId().getValor(), e.getMessage());
            }
        }
    }
}
