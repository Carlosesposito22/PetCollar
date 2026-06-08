package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficioTutorJpaRepository extends JpaRepository<BeneficioTutorJpa, String> {
    List<BeneficioTutorJpa> findByTutorId(String tutorId);
    List<BeneficioTutorJpa> findByPlanoId(String planoId);
    List<BeneficioTutorJpa> findByStatus(String status);
    List<BeneficioTutorJpa> findByBeneficioCatalogoId(String beneficioCatalogoId);
}
