package br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio;

/**
 * Porta Observer (padrão Observer, CLAUDE.md §8): define o contrato dos
 * observadores que reagem à alteração de configuração de um
 * {@link BeneficioCatalogo} pelo administrador. O Subject correspondente é
 * {@link PublicadorDeAlteracoesBeneficio}.
 */
public interface IObservadorDeAlteracaoBeneficio {
    void aoAlterarConfiguracao(BeneficioCatalogo catalogo);
}
