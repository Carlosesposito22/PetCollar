package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Subject do padrão Observer: ponto único de publicação de alterações em
 * {@link Plano}. Desacopla o {@code GerenciarPlanoUseCase} dos observadores
 * que reagem à mudança (ex.: notificação de tutores afetados). Domínio puro —
 * o wiring dos observadores acontece em {@code AssinaturaFaturamentoConfig}
 * (CLAUDE.md §6.5).
 */
public class PublicadorDeAlteracoesPlano {

    private final List<IObservadorDeAlteracaoPlano> observadores = new ArrayList<>();

    public void inscrever(IObservadorDeAlteracaoPlano observador) {
        if (observador == null)
            throw new IllegalArgumentException("Observador não pode ser nulo.");
        this.observadores.add(observador);
    }

    public void publicar(Plano plano) {
        if (plano == null)
            throw new IllegalArgumentException("Plano não pode ser nulo.");
        for (IObservadorDeAlteracaoPlano observador : observadores) {
            observador.aoAlterarPlano(plano);
        }
    }

    public List<IObservadorDeAlteracaoPlano> getObservadores() {
        return Collections.unmodifiableList(observadores);
    }
}
