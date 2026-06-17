package br.com.cesar.petCollar.dominio.AgendamentoClinico.agendamento;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.DisponibilidadeAgendaService;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaExame;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;

public class AgendamentoRetornoService extends AgendamentoService {

    private final IConsultaRepositorio consultaRepositorio;
    private final IConsultaExame exames;

    public AgendamentoRetornoService(
            IConsultaProntuario prontuario,
            IConsultaRepositorio consultaRepositorio,
            DisponibilidadeAgendaService disponibilidadeAgenda,
            IServicoNotificacao servicoNotificacao,
            IConsultaExame exames) {
        super(prontuario, consultaRepositorio, disponibilidadeAgenda, servicoNotificacao);
        if (exames == null)
            throw new IllegalArgumentException("Porta de exames não pode ser nula.");
        this.consultaRepositorio = consultaRepositorio;
        this.exames = exames;
    }

    @Override
    protected void validarPreCondicoesEspecificas(RequisicaoAgendamento requisicao) {

        ConsultaId origemId = requisicao.getConsultaOrigemId()
                .orElseThrow(() -> new IllegalArgumentException(
                        "O agendamento de retorno exige uma consulta de origem."));

        Consulta origem = consultaRepositorio.buscarPorId(origemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Consulta de origem não encontrada."));

        if (origem.getStatus() == StatusConsulta.RETORNO_AGENDADO)
            throw new IllegalStateException(
                    "Esta consulta já possui um retorno agendado.");

        boolean elegivel =
                origem.getStatus() == StatusConsulta.AGUARDANDO_RETORNO
                || origem.getStatus() == StatusConsulta.EXAMES_SOLICITADOS;

        if (!elegivel)
            throw new IllegalStateException(
                    "A consulta de origem não está elegível para retorno. "
                    + "Status atual: " + origem.getStatus().name());
    }

    @Override
    protected void executarValidacoesExtras(RequisicaoAgendamento requisicao) {

        ConsultaId origemId = requisicao.getConsultaOrigemId()
                .orElseThrow(() -> new IllegalArgumentException(
                        "O agendamento de retorno exige uma consulta de origem."));

        Consulta origem = consultaRepositorio.buscarPorId(origemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Consulta de origem não encontrada."));

        if (origem.getStatus() == StatusConsulta.EXAMES_SOLICITADOS
                && exames.contarConcluidosPorConsultaOrigem(origemId) < 1)
            throw new IllegalStateException(
                    "O agendamento de retorno requer que ao menos um exame "
                    + "solicitado na consulta de origem esteja concluído.");
    }

    @Override
    protected Consulta criarConsulta(RequisicaoAgendamento requisicao) {

        ConsultaId origemId = requisicao.getConsultaOrigemId()
                .orElseThrow(() -> new IllegalArgumentException(
                        "O agendamento de retorno exige uma consulta de origem."));
        return new Consulta(
                ConsultaId.gerar(),
                requisicao.getPacienteId(),
                requisicao.getTutorId(),
                requisicao.getMedicoId(),
                requisicao.getEspecialidadeId(),
                requisicao.getMotivo(),
                requisicao.getHorario(),
                origemId);
    }
}
