package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketBeneficioJpaRepository extends JpaRepository<TicketBeneficioJpa, String> {
    List<TicketBeneficioJpa> findByBeneficioTutorId(String beneficioTutorId);
    List<TicketBeneficioJpa> findByStatus(String status);
    boolean existsByCodigoGUID(String codigoGUID);
    boolean existsByBeneficioTutorIdAndStatus(String beneficioTutorId, String status);
}
