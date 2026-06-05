package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.IAgendaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoConsultaInicialService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento.AgendamentoRetornoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.GestaoAgendamentoService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.Especialidade;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Wiring (§6.5) dos serviços de domínio do AgendamentoClinico como beans, a partir
 * dos adapters em memória deste módulo, e seed de dados de demonstração. Espelha o
 * {@code AgendamentoClinicoConfig} da infraestrutura (que entra em cena quando o
 * banco é ligado).
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

    /** Seed de especialidades e médicos para demonstração da API. */
    @Bean
    public CommandLineRunner seedAgendamentoClinico(EspecialidadeRepositorioEmMemoria especialidades) {
        return args -> {
            cadastrar(especialidades, "Cardiologia",
                "Diagnóstico e tratamento de doenças cardiovasculares", 2);
            cadastrar(especialidades, "Dermatologia",
                "Cuidados com a pele, pelagem e anexos", 1);
            cadastrar(especialidades, "Ortopedia",
                "Sistema musculoesquelético e locomoção", 3);
        };
    }

    private void cadastrar(EspecialidadeRepositorioEmMemoria repositorio,
                           String nome, String descricao, int quantidadeMedicos) {
        Especialidade especialidade = new Especialidade(EspecialidadeId.gerar(), nome, descricao);
        List<MedicoId> medicos = java.util.stream.Stream
            .generate(MedicoId::gerar).limit(quantidadeMedicos).toList();
        repositorio.cadastrar(especialidade, medicos);
    }
}
