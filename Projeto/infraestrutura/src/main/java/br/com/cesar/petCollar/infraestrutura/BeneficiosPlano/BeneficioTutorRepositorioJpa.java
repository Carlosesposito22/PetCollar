package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogoId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutor;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutorId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioTutorRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.StatusBeneficio;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

@Repository
public class BeneficioTutorRepositorioJpa implements IBeneficioTutorRepositorio {

    private final BeneficioTutorJpaRepository jpa;

    public BeneficioTutorRepositorioJpa(BeneficioTutorJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(BeneficioTutor beneficioTutor) {
        jpa.save(BeneficioTutorJpa.fromDomain(beneficioTutor));
    }

    @Override
    public BeneficioTutor findById(BeneficioTutorId id) {
        return jpa.findById(id.getValor()).map(BeneficioTutorJpa::toDomain).orElse(null);
    }

    @Override
    public List<BeneficioTutor> findByTutorId(TutorId tutorId) {
        return jpa.findByTutorId(tutorId.getValor()).stream().map(BeneficioTutorJpa::toDomain).toList();
    }

    @Override
    public List<BeneficioTutor> findByPlanoId(PlanoId planoId) {
        return jpa.findByPlanoId(planoId.getValor()).stream().map(BeneficioTutorJpa::toDomain).toList();
    }

    @Override
    public List<BeneficioTutor> findByStatus(StatusBeneficio status) {
        return jpa.findByStatus(status.name()).stream().map(BeneficioTutorJpa::toDomain).toList();
    }

    @Override
    public List<BeneficioTutor> findByBeneficioCatalogoId(BeneficioCatalogoId beneficioCatalogoId) {
        return jpa.findByBeneficioCatalogoId(beneficioCatalogoId.getValor()).stream().map(BeneficioTutorJpa::toDomain).toList();
    }
}
