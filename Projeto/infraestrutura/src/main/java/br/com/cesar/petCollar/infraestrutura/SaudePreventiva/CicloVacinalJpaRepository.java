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
}
