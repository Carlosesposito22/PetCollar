package br.com.cesar.petCollar.infraestrutura.SaudePreventiva;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CicloVacinalJpaRepository extends JpaRepository<CicloVacinalJpa, String> {

    List<CicloVacinalJpa> findByPacienteId(String pacienteId);

    Optional<CicloVacinalJpa> findByPacienteIdAndNomeCicloIgnoreCase(String pacienteId, String nomeCiclo);

    void deleteByPacienteId(String pacienteId);
}
