package br.com.cesar.petCollar.dominio.compartilhado.eventos;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public interface IObservadorDeEventoTutor {

    void aoOcorrerEvento(TutorId tutorId, String chaveEvento, LocalDateTime ocorridoEm);
}
