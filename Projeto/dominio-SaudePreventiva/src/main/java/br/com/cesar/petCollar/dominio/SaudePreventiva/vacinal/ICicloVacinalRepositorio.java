package br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistência do agregado {@link CicloVacinal}.
 * Definido no domínio; implementado pela infraestrutura (Adapter JPA).
 */
public interface ICicloVacinalRepositorio {

    void salvar(CicloVacinal ciclo);

    Optional<CicloVacinal> buscarPorId(VacinaId id);

    List<CicloVacinal> listarPorPaciente(PacienteId pacienteId);

    Optional<CicloVacinal> buscarPorPacienteENomeCiclo(PacienteId pacienteId, String nomeCiclo);

    void removerPorPaciente(PacienteId pacienteId);
}
