package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogoId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

@Repository
public class BeneficioCatalogoRepositorioJpa implements IBeneficioCatalogoRepositorio {

    private final BeneficioCatalogoJpaRepository jpa;

    public BeneficioCatalogoRepositorioJpa(BeneficioCatalogoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(BeneficioCatalogo beneficioCatalogo) {
        jpa.save(BeneficioCatalogoJpa.fromDomain(beneficioCatalogo));
    }

    @Override
    public BeneficioCatalogo findById(BeneficioCatalogoId id) {
        return jpa.findById(id.getValor()).map(BeneficioCatalogoJpa::toDomain).orElse(null);
    }

    @Override
    public List<BeneficioCatalogo> findByPlanoId(PlanoId planoId) {
        return jpa.findByPlanoId(planoId.getValor()).stream().map(BeneficioCatalogoJpa::toDomain).toList();
    }

    @Override
    public List<BeneficioCatalogo> findByAtivo(boolean ativo) {
        return jpa.findByAtivo(ativo).stream().map(BeneficioCatalogoJpa::toDomain).toList();
    }
}
