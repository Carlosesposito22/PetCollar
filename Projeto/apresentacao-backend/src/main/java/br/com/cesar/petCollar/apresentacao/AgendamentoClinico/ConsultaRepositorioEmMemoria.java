package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.FiltroConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.IConsultaRepositorio;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementação provisória em memória de {@link IConsultaRepositorio} (stand-in
 * enquanto o banco está desligado — ver application.yml). Substituível pelo adapter
 * JPA {@code ConsultaRepositorioJpa} sem tocar no domínio.
 */
@Repository
public class ConsultaRepositorioEmMemoria implements IConsultaRepositorio {

    private final ConcurrentMap<String, Consulta> consultas = new ConcurrentHashMap<>();

    @Override
    public void salvar(Consulta consulta) {
        consultas.put(consulta.getId().getValor(), consulta);
    }

    @Override
    public Optional<Consulta> buscarPorId(ConsultaId id) {
        return Optional.ofNullable(consultas.get(id.getValor()));
    }

    @Override
    public List<Consulta> listarPorPaciente(PacienteId pacienteId, FiltroConsulta filtro) {
        FiltroConsulta criterio = filtro == null ? FiltroConsulta.vazio() : filtro;
        return consultas.values().stream()
            .filter(c -> c.getPacienteId().equals(pacienteId))
            .filter(c -> criterio.correspondeStatus(c.getStatus()))
            .filter(c -> criterio.correspondeTipo(c.getTipo()))
            .filter(c -> criterio.correspondePeriodo(c.getHorario().getInicio()))
            .toList();
    }

    @Override
    public List<Consulta> listarPorMedicoEPeriodo(MedicoId medicoId,
                                                  LocalDateTime inicio, LocalDateTime fim) {
        HorarioConsulta periodo = new HorarioConsulta(inicio, fim);
        return consultas.values().stream()
            .filter(c -> c.getMedicoId().equals(medicoId))
            .filter(c -> c.getHorario().sobrepoeCom(periodo))
            .toList();
    }

    @Override
    public List<Consulta> listarElegiveisRetorno(PacienteId pacienteId) {
        return consultas.values().stream()
            .filter(c -> c.getPacienteId().equals(pacienteId))
            .filter(Consulta::isElegivelParaRetorno)
            .toList();
    }

    @Override
    public boolean existeConflitoNoPaciente(PacienteId pacienteId, HorarioConsulta horario) {
        return consultas.values().stream()
            .filter(c -> c.getPacienteId().equals(pacienteId))
            .filter(c -> c.getStatus() != StatusConsulta.CANCELADA)
            .anyMatch(c -> c.getHorario().sobrepoeCom(horario));
    }
}
