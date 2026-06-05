package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IServicoNotificacao;

import java.time.LocalDateTime;

/**
 * Serviço de domínio responsável pela remarcação (RN 15) e cancelamento de
 * consultas, sempre respeitando a antecedência mínima (RN 16), preservando os
 * vínculos clínicos e a auditoria (RN 18/19), e notificando os envolvidos (RN 14).
 */
public class GestaoAgendamentoService {

    public static final int ANTECEDENCIA_MINIMA_HORAS = 24;

    private final IConsultaRepositorio consultaRepositorio;
    private final IServicoNotificacao notificacao;

    public GestaoAgendamentoService(IConsultaRepositorio consultaRepositorio,
                                    IServicoNotificacao notificacao) {
        if (consultaRepositorio == null)
            throw new IllegalArgumentException("Repositório de consultas não pode ser nulo.");
        if (notificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo.");
        this.consultaRepositorio = consultaRepositorio;
        this.notificacao = notificacao;
    }

    public Consulta remarcar(ConsultaId consultaId, HorarioConsulta novoHorario) {
        if (consultaId == null)
            throw new IllegalArgumentException("Id da consulta não pode ser nulo.");
        if (novoHorario == null)
            throw new IllegalArgumentException("Novo horário não pode ser nulo.");

        Consulta consulta = obrigatoria(consultaId);
        garantirAntecedenciaMinima(consulta, "remarcar");

        consulta.remarcar(novoHorario);             // RN 15/18 — preserva vínculos, audita
        consultaRepositorio.salvar(consulta);

        // RN 14 — notifica tutor e médico
        notificacao.notificarTutor(consulta.getTutorId(),
            "Sua consulta foi remarcada para " + novoHorario + ".");
        notificacao.notificarMedico(consulta.getMedicoId(),
            "Uma consulta foi remarcada para " + novoHorario + ".");

        return consulta;
    }

    public Consulta cancelar(ConsultaId consultaId) {
        if (consultaId == null)
            throw new IllegalArgumentException("Id da consulta não pode ser nulo.");

        Consulta consulta = obrigatoria(consultaId);
        garantirAntecedenciaMinima(consulta, "cancelar");

        consulta.cancelar();
        consultaRepositorio.salvar(consulta);

        // RN 14 — notifica tutor e médico
        notificacao.notificarTutor(consulta.getTutorId(),
            "Sua consulta de " + consulta.getHorario() + " foi cancelada.");
        notificacao.notificarMedico(consulta.getMedicoId(),
            "Uma consulta de " + consulta.getHorario() + " foi cancelada.");

        return consulta;
    }

    private Consulta obrigatoria(ConsultaId consultaId) {
        return consultaRepositorio.buscarPorId(consultaId)
            .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada."));
    }

    private void garantirAntecedenciaMinima(Consulta consulta, String operacao) {
        long horas = consulta.getHorario().calcularAntecedenciaEmHoras(LocalDateTime.now());
        if (horas < ANTECEDENCIA_MINIMA_HORAS)
            throw new IllegalStateException(
                "Para " + operacao + " é necessária antecedência mínima de "
                + ANTECEDENCIA_MINIMA_HORAS + " horas.");
    }
}
