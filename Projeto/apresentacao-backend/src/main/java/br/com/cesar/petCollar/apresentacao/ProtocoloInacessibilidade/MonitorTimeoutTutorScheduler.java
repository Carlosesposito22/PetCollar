package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.AtivacaoProtocoloService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Gatilho periódico da ativação automática por timeout (RN 1). É responsabilidade
 * <b>da infraestrutura/apresentação</b>, não do domínio: o domínio só expõe
 * {@link AtivacaoProtocoloService#verificarTodosAtendimentosAtivos()}, que este
 * scheduler invoca a cada minuto sobre os atendimentos em andamento.
 *
 * <p>A verificação é idempotente, então rodar repetidamente não cria protocolos
 * duplicados para o mesmo atendimento.
 */
@Component
public class MonitorTimeoutTutorScheduler {

    private static final Logger log = LoggerFactory.getLogger(MonitorTimeoutTutorScheduler.class);

    private final AtivacaoProtocoloService ativacaoService;

    public MonitorTimeoutTutorScheduler(AtivacaoProtocoloService ativacaoService) {
        this.ativacaoService = ativacaoService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void monitorar() {
        try {
            int ativos = ativacaoService.verificarTodosAtendimentosAtivos();
            log.debug("[MONITOR F-03] varredura de timeout concluída — protocolos ativos: {}", ativos);
        } catch (Exception e) {
            log.warn("[MONITOR F-03] falha na varredura de timeout: {}", e.getMessage());
        }
    }
}
