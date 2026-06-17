package br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;

import java.util.List;

public interface IAgendaRepositorio {

    List<BloqueioAgenda> listarBloqueios(MedicoId medicoId, HorarioConsulta periodo);

    Expediente obterExpediente(MedicoId medicoId);
}
