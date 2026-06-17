package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;
import java.util.Optional;

public interface ICicloVacinalRepositorio {

    void salvar(CicloVacinal ciclo);

    Optional<CicloVacinal> buscarPorId(VacinaId id);

    List<CicloVacinal> listarPorPaciente(PacienteId pacienteId);

    Optional<CicloVacinal> buscarPorPacienteENomeCiclo(PacienteId pacienteId, String nomeCiclo);

    void remover(VacinaId id);

    void removerPorPaciente(PacienteId pacienteId);
}
