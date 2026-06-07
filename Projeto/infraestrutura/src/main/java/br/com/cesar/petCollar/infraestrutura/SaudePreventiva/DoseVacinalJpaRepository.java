package br.com.cesar.petCollar.infraestrutura.SaudePreventiva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoseVacinalJpaRepository extends JpaRepository<DoseVacinalJpa, String> {

    /** DELETE direto via JPQL para garantir execução imediata antes do delete do ciclo pai. */
    @Modifying
    @Query("DELETE FROM DoseVacinalJpa d WHERE d.cicloId = :cicloId")
    void deleteByCicloId(@Param("cicloId") String cicloId);
}
