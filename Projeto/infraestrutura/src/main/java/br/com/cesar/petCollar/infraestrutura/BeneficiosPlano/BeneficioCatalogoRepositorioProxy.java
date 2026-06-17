package br.com.cesar.petCollar.infraestrutura.BeneficiosPlano;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogoId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

public class BeneficioCatalogoRepositorioProxy implements IBeneficioCatalogoRepositorio {

    private final IBeneficioCatalogoRepositorio repositorioReal;

    private final Map<String, BeneficioCatalogo> porId = new ConcurrentHashMap<>();
    private final Map<String, List<BeneficioCatalogo>> porPlanoId = new ConcurrentHashMap<>();
    private final Map<Boolean, List<BeneficioCatalogo>> porAtivo = new ConcurrentHashMap<>();

    public BeneficioCatalogoRepositorioProxy(IBeneficioCatalogoRepositorio repositorioReal) {
        if (repositorioReal == null)
            throw new IllegalArgumentException("IBeneficioCatalogoRepositorio real é obrigatório.");
        this.repositorioReal = repositorioReal;
    }

    @Override
    public void save(BeneficioCatalogo beneficioCatalogo) {
        repositorioReal.save(beneficioCatalogo);
        invalidar(beneficioCatalogo);
    }

    @Override
    public BeneficioCatalogo findById(BeneficioCatalogoId id) {
        return porId.computeIfAbsent(id.getValor(), chave -> repositorioReal.findById(id));
    }

    @Override
    public List<BeneficioCatalogo> findByPlanoId(PlanoId planoId) {
        return porPlanoId.computeIfAbsent(planoId.getValor(), chave -> repositorioReal.findByPlanoId(planoId));
    }

    @Override
    public List<BeneficioCatalogo> findByAtivo(boolean ativo) {
        return porAtivo.computeIfAbsent(ativo, chave -> repositorioReal.findByAtivo(ativo));
    }

    private void invalidar(BeneficioCatalogo alterado) {
        porId.remove(alterado.getId().getValor());
        porPlanoId.remove(alterado.getPlanoId().getValor());
        porAtivo.clear();
    }
}
