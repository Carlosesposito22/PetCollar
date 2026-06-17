package br.com.cesar.petCollar.dominio.compartilhado.eventos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class PublicadorDeEventosDoTutor {

    private final List<IObservadorDeEventoTutor> observadores = new ArrayList<>();

    public void inscrever(IObservadorDeEventoTutor observador) {
        if (observador == null)
            throw new IllegalArgumentException("Observador não pode ser nulo.");
        this.observadores.add(observador);
    }

    public void publicar(TutorId tutorId, String chaveEvento) {
        if (tutorId == null)
            throw new IllegalArgumentException("TutorId não pode ser nulo.");
        if (chaveEvento == null || chaveEvento.isBlank())
            throw new IllegalArgumentException("Chave de evento não pode ser vazia.");

        LocalDateTime ocorridoEm = LocalDateTime.now();
        for (IObservadorDeEventoTutor observador : observadores) {
            observador.aoOcorrerEvento(tutorId, chaveEvento, ocorridoEm);
        }
    }

    public List<IObservadorDeEventoTutor> getObservadores() {
        return Collections.unmodifiableList(observadores);
    }
}
