package br.com.cesar.petCollar.aplicacao.BeneficiosPlano;

import java.util.List;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogoId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.PeriodoRenovacao;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.PublicadorDeAlteracoesBeneficio;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

public class ConfigurarBeneficiosDoPlanoUseCase {

    private final IBeneficioCatalogoRepositorio catalogoRepositorio;
    private final PublicadorDeAlteracoesBeneficio publicadorDeAlteracoesBeneficio;

    public ConfigurarBeneficiosDoPlanoUseCase(IBeneficioCatalogoRepositorio catalogoRepositorio,
                                              PublicadorDeAlteracoesBeneficio publicadorDeAlteracoesBeneficio) {
        if (catalogoRepositorio == null)
            throw new IllegalArgumentException("IBeneficioCatalogoRepositorio é obrigatório.");
        if (publicadorDeAlteracoesBeneficio == null)
            throw new IllegalArgumentException("PublicadorDeAlteracoesBeneficio é obrigatório.");
        this.catalogoRepositorio = catalogoRepositorio;
        this.publicadorDeAlteracoesBeneficio = publicadorDeAlteracoesBeneficio;
    }

    public void configurar(PlanoId planoId, List<ConfigBeneficio> beneficios) {
        if (planoId == null) throw new IllegalArgumentException("PlanoId é obrigatório.");
        if (beneficios == null) throw new IllegalArgumentException("Lista de benefícios é obrigatória.");

        List<BeneficioCatalogo> existentes = catalogoRepositorio.findByPlanoId(planoId);

        for (ConfigBeneficio cfg : beneficios) {
            BeneficioCatalogo existente = existentes.stream()
                    .filter(c -> c.getNome().equalsIgnoreCase(cfg.nome()))
                    .findFirst()
                    .orElse(null);

            if (existente == null) {
                BeneficioCatalogo novo = new BeneficioCatalogo(
                        BeneficioCatalogoId.gerar(),
                        planoId,
                        cfg.nome(),
                        cfg.periodoRenovacao(),
                        cfg.limiteUsosPorPeriodo(),
                        cfg.carenciaDias());
                catalogoRepositorio.save(novo);
            } else {
                existente.alterarConfiguracao(
                        cfg.nome(),
                        cfg.periodoRenovacao(),
                        cfg.limiteUsosPorPeriodo(),
                        cfg.carenciaDias());
                catalogoRepositorio.save(existente);

                publicadorDeAlteracoesBeneficio.publicar(existente);
            }
        }
    }

    public List<BeneficioCatalogo> listar(PlanoId planoId) {
        if (planoId == null) throw new IllegalArgumentException("PlanoId é obrigatório.");
        return catalogoRepositorio.findByPlanoId(planoId);
    }

    public record ConfigBeneficio(
            String nome,
            PeriodoRenovacao periodoRenovacao,
            int limiteUsosPorPeriodo,
            int carenciaDias) {}
}
