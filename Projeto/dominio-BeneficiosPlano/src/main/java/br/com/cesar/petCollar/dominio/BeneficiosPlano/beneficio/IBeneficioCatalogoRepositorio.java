package br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio;

import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

import java.util.List;

public interface IBeneficioCatalogoRepositorio {
    void save(BeneficioCatalogo beneficioCatalogo);
    BeneficioCatalogo findById(BeneficioCatalogoId id);
    List<BeneficioCatalogo> findByPlanoId(PlanoId planoId);
    List<BeneficioCatalogo> findByAtivo(boolean ativo);
}

