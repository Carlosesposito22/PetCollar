package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutorId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.ITicketBeneficioRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.StatusTicket;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.TicketBeneficio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.TicketBeneficioId;

@Repository
public class TicketBeneficioRepositorioJpa implements ITicketBeneficioRepositorio {

    private final TicketBeneficioJpaRepository jpa;

    public TicketBeneficioRepositorioJpa(TicketBeneficioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(TicketBeneficio ticketBeneficio) {
        jpa.save(TicketBeneficioJpa.fromDomain(ticketBeneficio));
    }

    @Override
    public TicketBeneficio findById(TicketBeneficioId id) {
        return jpa.findById(id.getValor()).map(TicketBeneficioJpa::toDomain).orElse(null);
    }

    @Override
    public List<TicketBeneficio> findByBeneficioTutorId(BeneficioTutorId beneficioTutorId) {
        return jpa.findByBeneficioTutorId(beneficioTutorId.getValor()).stream().map(TicketBeneficioJpa::toDomain).toList();
    }

    @Override
    public List<TicketBeneficio> findByStatus(StatusTicket status) {
        return jpa.findByStatus(status.name()).stream().map(TicketBeneficioJpa::toDomain).toList();
    }

    @Override
    public boolean existsByCodigoGUID(String codigoGUID) {
        return jpa.existsByCodigoGUID(codigoGUID);
    }

    @Override
    public boolean existsByBeneficioTutorIdAndStatus(BeneficioTutorId beneficioTutorId, StatusTicket status) {
        return jpa.existsByBeneficioTutorIdAndStatus(beneficioTutorId.getValor(), status.name());
    }
}
