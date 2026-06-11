package br.com.cesar.petCollar.infraestrutura.SaudePreventiva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CicloVacinalJpaRepository extends JpaRepository<CicloVacinalJpa, String> {

    List<CicloVacinalJpa> findByPacienteId(String pacienteId);

    Optional<CicloVacinalJpa> findByPacienteIdAndNomeCicloIgnoreCase(String pacienteId, String nomeCiclo);

    void deleteByPacienteId(String pacienteId);

    /** DELETE direto via JPQL para evitar o UPDATE SET cicloId=NULL do orphanRemoval. */
    @Modifying
    @Query("DELETE FROM CicloVacinalJpa c WHERE c.id = :id")
    void deletarPorId(@Param("id") String id);

    /**
     * Remove em massa as doses de todos os ciclos de um paciente, via JPQL — evita
     * o UPDATE SET cicloId=NULL do orphanRemoval (a coluna cicloId é NOT NULL).
     * Deve ser chamado ANTES de {@link #deletarCiclosPorPaciente(String)}.
     */
    @Modifying
    @Query("DELETE FROM DoseVacinalJpa d WHERE d.cicloId IN "
         + "(SELECT c.id FROM CicloVacinalJpa c WHERE c.pacienteId = :pacienteId)")
    void deletarDosesPorPaciente(@Param("pacienteId") String pacienteId);

    /** DELETE direto dos ciclos de um paciente, via JPQL (sem orphanRemoval). */
    @Modifying
    @Query("DELETE FROM CicloVacinalJpa c WHERE c.pacienteId = :pacienteId")
    void deletarCiclosPorPaciente(@Param("pacienteId") String pacienteId);
}
