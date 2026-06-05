package br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;

import java.util.List;

public interface IAgendaRepositorio {

    /** Bloqueios do médico que intersectam o período consultado (RN 4). */
    List<BloqueioAgenda> listarBloqueios(MedicoId medicoId, HorarioConsulta periodo);

    /** Expediente vigente do médico (faixa de horário, duração e dias de atendimento). */
    Expediente obterExpediente(MedicoId medicoId);
}
