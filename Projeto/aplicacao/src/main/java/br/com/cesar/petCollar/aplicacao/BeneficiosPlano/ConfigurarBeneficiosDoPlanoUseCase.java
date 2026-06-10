package br.com.cesar.petCollar.aplicacao.BeneficiosPlano;

import java.util.List;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogoId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.PeriodoRenovacao;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.PublicadorDeAlteracoesBeneficio;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;

/**
 * Configura o catálogo de benefícios de um plano (F-08). Para cada benefício
 * informado pelo admin (ex.: Consulta, Vacinação), cria a entrada de
 * {@link BeneficioCatalogo} se ainda não existir naquele plano, ou atualiza a
 * existente via {@code alterarConfiguracao}. Em toda alteração de configuração,
 * dispara o {@link PublicadorDeAlteracoesBeneficio} para que os observadores
 * registrados (ex.: sincronização dos {@code BeneficioTutor} já provisionados)
 * reajam sem acoplamento direto (padrão Observer, CLAUDE.md §8).
 */
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
                // Observer: sincroniza os BeneficioTutor já provisionados (ex.: cap de usos).
                publicadorDeAlteracoesBeneficio.publicar(existente);
            }
        }
    }

    public List<BeneficioCatalogo> listar(PlanoId planoId) {
        if (planoId == null) throw new IllegalArgumentException("PlanoId é obrigatório.");
        return catalogoRepositorio.findByPlanoId(planoId);
    }

    /** Configuração de um benefício do plano informada pelo admin. */
    public record ConfigBeneficio(
            String nome,
            PeriodoRenovacao periodoRenovacao,
            int limiteUsosPorPeriodo,
            int carenciaDias) {}
}
