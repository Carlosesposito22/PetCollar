package br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;

import java.util.List;
import java.util.Optional;

public interface IEspecialidadeRepositorio {

    List<Especialidade> listarTodas();

    Optional<Especialidade> buscarPorId(EspecialidadeId id);

    List<MedicoId> listarMedicosDaEspecialidade(EspecialidadeId id);
}
