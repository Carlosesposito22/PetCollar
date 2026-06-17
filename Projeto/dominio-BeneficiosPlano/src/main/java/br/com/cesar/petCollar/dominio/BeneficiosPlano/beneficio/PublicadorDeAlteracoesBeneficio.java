package br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
