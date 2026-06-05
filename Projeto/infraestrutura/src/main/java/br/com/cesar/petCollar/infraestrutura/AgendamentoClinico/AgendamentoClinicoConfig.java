package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.IAgendaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoConsultaInicialService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoRetornoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.GestaoAgendamentoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring canônico (§6.5 do guia) dos serviços de domínio do AgendamentoClinico como
 * beans, a partir das interfaces {@code IXxxRepositorio} e das portas de saída, que
 * o Spring resolve para os adapters JPA deste módulo.
 *
 * <p>Enquanto o DataSource/JPA estiver desligado (ver application.yml), esta
 * configuração permanece como referência: a infraestrutura não é varrida pelo
 * component-scan do backend, cujos beans em memória sobem a API. Ao ligar a
 * persistência, importe/escaneie este pacote para ativar os adapters JPA.
 */
@Configuration
public class AgendamentoClinicoConfig {

    @Bean
    public DisponibilidadeAgendaService disponibilidadeAgendaService(
            IConsultaRepositorio consultaRepositorio, IAgendaRepositorio agendaRepositorio) {
        return new DisponibilidadeAgendaService(consultaRepositorio, agendaRepositorio);
    }

    @Bean
    public AgendamentoConsultaInicialService agendamentoConsultaInicialService(
            IConsultaProntuario prontuario, IConsultaRepositorio consultaRepositorio,
            DisponibilidadeAgendaService disponibilidade, IServicoNotificacao notificacao) {
        return new AgendamentoConsultaInicialService(prontuario, consultaRepositorio,
            disponibilidade, notificacao);
    }

    @Bean
    public AgendamentoRetornoService agendamentoRetornoService(
            IConsultaProntuario prontuario, IConsultaRepositorio consultaRepositorio,
            DisponibilidadeAgendaService disponibilidade, IServicoNotificacao notificacao,
            IConsultaExame exames) {
        return new AgendamentoRetornoService(prontuario, consultaRepositorio,
            disponibilidade, notificacao, exames);
    }

    @Bean
    public GestaoAgendamentoService gestaoAgendamentoService(
            IConsultaRepositorio consultaRepositorio, IServicoNotificacao notificacao) {
        return new GestaoAgendamentoService(consultaRepositorio, notificacao);
    }
}
