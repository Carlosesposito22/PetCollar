package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
