package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano;

/**
 * Porta Observer (padrão Observer, CLAUDE.md §8): define o contrato dos
 * observadores que reagem à alteração de um {@link Plano} pelo administrador.
 * O Subject correspondente é {@link PublicadorDeAlteracoesPlano}.
 */
public interface IObservadorDeAlteracaoPlano {
    void aoAlterarPlano(Plano plano);
}
