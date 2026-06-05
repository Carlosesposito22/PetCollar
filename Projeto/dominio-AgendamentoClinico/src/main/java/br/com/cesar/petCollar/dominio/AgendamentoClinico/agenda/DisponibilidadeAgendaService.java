package br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de domínio que calcula os horários livres de um médico (RN 4) cruzando o
 * seu expediente, os bloqueios da agenda e as consultas já agendadas. Stateless;
 * dependências recebidas por construtor.
 */
public class DisponibilidadeAgendaService {

    private final IConsultaRepositorio consultaRepositorio;
    private final IAgendaRepositorio agendaRepositorio;

    public DisponibilidadeAgendaService(IConsultaRepositorio consultaRepositorio,
                                        IAgendaRepositorio agendaRepositorio) {
        if (consultaRepositorio == null)
            throw new IllegalArgumentException("Repositório de consultas não pode ser nulo.");
        if (agendaRepositorio == null)
            throw new IllegalArgumentException("Repositório de agenda não pode ser nulo.");
        this.consultaRepositorio = consultaRepositorio;
        this.agendaRepositorio = agendaRepositorio;
    }

    /** Horários livres do médico dentro da janela [inicio, fim]. */
    public List<HorarioConsulta> listarHorariosLivres(MedicoId medicoId,
                                                       LocalDateTime inicio, LocalDateTime fim) {
        if (medicoId == null)
            throw new IllegalArgumentException("Id do médico não pode ser nulo.");
        if (inicio == null || fim == null)
            throw new IllegalArgumentException("Início e fim do período são obrigatórios.");
        if (!fim.isAfter(inicio))
            throw new IllegalArgumentException("O fim do período deve ser posterior ao início.");

        Expediente expediente = agendaRepositorio.obterExpediente(medicoId);
        if (expediente == null)
            throw new IllegalStateException("Médico sem expediente cadastrado.");

        HorarioConsulta janela = new HorarioConsulta(inicio, fim);
        List<BloqueioAgenda> bloqueios = agendaRepositorio.listarBloqueios(medicoId, janela);
        List<Consulta> consultas = consultaRepositorio.listarPorMedicoEPeriodo(medicoId, inicio, fim);

        List<HorarioConsulta> livres = new ArrayList<>();
        for (HorarioConsulta candidato : gerarSlots(expediente, inicio, fim)) {
            if (estaLivre(candidato, bloqueios, consultas)) {
                livres.add(candidato);
            }
        }
        return livres;
    }

    /** Indica se um horário específico está livre para o médico (usado ao agendar — RN 4). */
    public boolean estaDisponivel(MedicoId medicoId, HorarioConsulta horario) {
        if (medicoId == null)
            throw new IllegalArgumentException("Id do médico não pode ser nulo.");
        if (horario == null)
            throw new IllegalArgumentException("Horário não pode ser nulo.");

        Expediente expediente = agendaRepositorio.obterExpediente(medicoId);
        if (expediente == null)
            throw new IllegalStateException("Médico sem expediente cadastrado.");
        if (!dentroDoExpediente(horario, expediente))
            return false;

        List<BloqueioAgenda> bloqueios =
            agendaRepositorio.listarBloqueios(medicoId, horario);
        List<Consulta> consultas = consultaRepositorio.listarPorMedicoEPeriodo(
            medicoId, horario.getInicio(), horario.getFim());
        return estaLivre(horario, bloqueios, consultas);
    }

    private List<HorarioConsulta> gerarSlots(Expediente expediente,
                                             LocalDateTime inicio, LocalDateTime fim) {
        List<HorarioConsulta> slots = new ArrayList<>();
        LocalDate dia = inicio.toLocalDate();
        LocalDate ultimoDia = fim.toLocalDate();
        while (!dia.isAfter(ultimoDia)) {
            if (expediente.atendeNoDia(dia.getDayOfWeek())) {
                LocalDateTime cursor = dia.atTime(expediente.getHoraInicio());
                LocalDateTime fimExpediente = dia.atTime(expediente.getHoraFim());
                while (!cursor.plusMinutes(expediente.getDuracaoConsultaMinutos()).isAfter(fimExpediente)) {
                    LocalDateTime fimSlot = cursor.plusMinutes(expediente.getDuracaoConsultaMinutos());
                    if (!cursor.isBefore(inicio) && !fimSlot.isAfter(fim)) {
                        slots.add(new HorarioConsulta(cursor, fimSlot));
                    }
                    cursor = fimSlot;
                }
            }
            dia = dia.plusDays(1);
        }
        return slots;
    }

    private boolean dentroDoExpediente(HorarioConsulta horario, Expediente expediente) {
        if (!expediente.atendeNoDia(horario.getInicio().getDayOfWeek()))
            return false;
        boolean depoisDoInicio = !horario.getInicio().toLocalTime().isBefore(expediente.getHoraInicio());
        boolean antesDoFim = !horario.getFim().toLocalTime().isAfter(expediente.getHoraFim());
        boolean mesmoDia = horario.getInicio().toLocalDate().equals(horario.getFim().toLocalDate());
        return depoisDoInicio && antesDoFim && mesmoDia;
    }

    private boolean estaLivre(HorarioConsulta candidato, List<BloqueioAgenda> bloqueios,
                              List<Consulta> consultas) {
        for (BloqueioAgenda bloqueio : bloqueios) {
            if (candidato.sobrepoeCom(bloqueio.getPeriodo()))
                return false;
        }
        for (Consulta consulta : consultas) {
            if (consulta.getStatus() == StatusConsulta.CANCELADA)
                continue;
            if (candidato.sobrepoeCom(consulta.getHorario()))
                return false;
        }
        return true;
    }
}
