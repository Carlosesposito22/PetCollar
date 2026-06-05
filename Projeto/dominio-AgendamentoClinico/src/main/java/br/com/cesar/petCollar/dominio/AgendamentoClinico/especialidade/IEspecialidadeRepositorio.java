package br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;

import java.util.List;
import java.util.Optional;

public interface IEspecialidadeRepositorio {

    List<Especialidade> listarTodas();

    Optional<Especialidade> buscarPorId(EspecialidadeId id);

    /** Médicos habilitados na especialidade, para o filtro de agendamento (RN 2). */
    List<MedicoId> listarMedicosDaEspecialidade(EspecialidadeId id);
}
