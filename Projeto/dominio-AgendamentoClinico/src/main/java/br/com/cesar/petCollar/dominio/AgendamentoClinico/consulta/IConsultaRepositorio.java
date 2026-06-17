package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IConsultaRepositorio {

    void salvar(Consulta consulta);

    Optional<Consulta> buscarPorId(ConsultaId id);

    List<Consulta> listarPorPaciente(PacienteId pacienteId, FiltroConsulta filtro);

    List<Consulta> listarPorMedicoEPeriodo(MedicoId medicoId, LocalDateTime inicio, LocalDateTime fim);

    List<Consulta> listarElegiveisRetorno(PacienteId pacienteId);

    boolean existeConflitoNoPaciente(PacienteId pacienteId, HorarioConsulta horario);

    List<Consulta> listarPendentesRetornoPorMedico(MedicoId medicoId);
}
