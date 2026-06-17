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

        List<ProtocoloInacessibilidade> ativos = protocoloRepositorio.listarAtivos();
        for (ProtocoloInacessibilidade protocolo : ativos) {
            try {
                orquestrador.executarProximaEtapa(protocolo);

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
