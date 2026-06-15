package br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IConsultaRepositorio {

    void salvar(Consulta consulta);

    Optional<Consulta> buscarPorId(ConsultaId id);

    /** Consultas do paciente, aplicando os filtros opcionais da agenda do tutor (RN 17). */
    List<Consulta> listarPorPaciente(PacienteId pacienteId, FiltroConsulta filtro);

    /** Consultas de um médico cujo horário intersecta o período (base da disponibilidade — RN 4). */
    List<Consulta> listarPorMedicoEPeriodo(MedicoId medicoId, LocalDateTime inicio, LocalDateTime fim);

    /** Consultas elegíveis a retorno: status AGUARDANDO_RETORNO ou EXAMES_SOLICITADOS (RN 7). */
    List<Consulta> listarElegiveisRetorno(PacienteId pacienteId);

    /** Indica se o paciente já possui consulta ativa sobreposta ao horário (RN 5). */
    boolean existeConflitoNoPaciente(PacienteId pacienteId, HorarioConsulta horario);

    /**
     * Consultas do médico aguardando ou com retorno agendado, sem restrição de período
     * (AGUARDANDO_RETORNO, EXAMES_SOLICITADOS, RETORNO_AGENDADO). Usado para popular
     * a aba "Finalizadas" do painel do médico, que deve exibir pendências de qualquer data.
     */
    List<Consulta> listarPendentesRetornoPorMedico(MedicoId medicoId);
}
