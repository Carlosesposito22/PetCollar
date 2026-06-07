package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.BloqueioAgenda;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.Expediente;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.IAgendaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;

import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

/**
 * Adapter JPA de {@link IAgendaRepositorio}. O expediente padrão está
 * hardcoded (segunda a sexta, 08–18h, 30 min) — igual à implementação em
 * memória. Bloqueios pontuais não são persistidos nesta versão:
 * {@link IAgendaRepositorio} não possui método de escrita para eles.
 */
@Repository
public class AgendaRepositorioJpa implements IAgendaRepositorio {

    private static final Expediente EXPEDIENTE_PADRAO = new Expediente(
            LocalTime.of(8, 0), LocalTime.of(18, 0), 30,
            EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                       DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

    @Override
    public List<BloqueioAgenda> listarBloqueios(MedicoId medicoId, HorarioConsulta periodo) {
        return List.of();
    }

    @Override
    public Expediente obterExpediente(MedicoId medicoId) {
        return EXPEDIENTE_PADRAO;
    }
}
