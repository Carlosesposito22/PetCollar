package br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Subject do padrão Observer: ponto único de publicação de alterações em
 * {@link BeneficioCatalogo}. Desacopla o {@code AlterarConfiguracaoBeneficioUseCase}
 * (Fase 5) dos observadores que reagem à mudança (ex.: sincronização de
 * {@code BeneficioTutor} ativos vinculados). Domínio puro — o wiring dos
 * observadores acontece em {@code BeneficiosPlanoConfig} (CLAUDE.md §6.5).
 */
public class PublicadorDeAlteracoesBeneficio {

    private final List<IObservadorDeAlteracaoBeneficio> observadores = new ArrayList<>();

    public void inscrever(IObservadorDeAlteracaoBeneficio observador) {
        if (observador == null)
            throw new IllegalArgumentException("Observador não pode ser nulo.");
        this.observadores.add(observador);
    }

    public void publicar(BeneficioCatalogo catalogo) {
        if (catalogo == null)
            throw new IllegalArgumentException("BeneficioCatalogo não pode ser nulo.");
        for (IObservadorDeAlteracaoBeneficio observador : observadores) {
            observador.aoAlterarConfiguracao(catalogo);
        }
    }

    public List<IObservadorDeAlteracaoBeneficio> getObservadores() {
        return Collections.unmodifiableList(observadores);
    }
}
