package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.BloqueioAgenda;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.Expediente;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.agenda.IAgendaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Repository removido — substituído por AgendaRepositorioJpa (JPA).
 */
public class AgendaRepositorioEmMemoria implements IAgendaRepositorio {

    private static final Expediente EXPEDIENTE_PADRAO = new Expediente(
        LocalTime.of(8, 0), LocalTime.of(18, 0), 30,
        EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                   DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

    private final ConcurrentMap<String, List<BloqueioAgenda>> bloqueios = new ConcurrentHashMap<>();

    public void adicionarBloqueio(MedicoId medicoId, BloqueioAgenda bloqueio) {
        bloqueios.computeIfAbsent(medicoId.getValor(), k -> new ArrayList<>()).add(bloqueio);
    }

    @Override
    public List<BloqueioAgenda> listarBloqueios(MedicoId medicoId, HorarioConsulta periodo) {
        return bloqueios.getOrDefault(medicoId.getValor(), List.of()).stream()
            .filter(b -> b.getPeriodo().sobrepoeCom(periodo))
            .toList();
    }

    @Override
    public Expediente obterExpediente(MedicoId medicoId) {
        return EXPEDIENTE_PADRAO;
    }
}
