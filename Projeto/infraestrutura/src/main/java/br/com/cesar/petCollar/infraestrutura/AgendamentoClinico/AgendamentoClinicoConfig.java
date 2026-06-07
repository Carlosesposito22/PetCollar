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

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Wiring canônico (§6.5) dos serviços de domínio do AgendamentoClinico como
 * beans, a partir dos adapters JPA deste módulo.
 */
@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.AgendamentoClinico")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.AgendamentoClinico")
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
