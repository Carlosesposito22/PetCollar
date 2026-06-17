package br.com.cesar.petCollar.dominio.Gamificacao.conquista;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.compartilhado.eventos.IObservadorDeEventoTutor;

public class ConcessaoBadgeObservador implements IObservadorDeEventoTutor {

    private final ConcessaoBadgeService concessaoBadgeService;

    public ConcessaoBadgeObservador(ConcessaoBadgeService concessaoBadgeService) {
        if (concessaoBadgeService == null)
            throw new IllegalArgumentException("ConcessaoBadgeService não pode ser nulo.");
        this.concessaoBadgeService = concessaoBadgeService;
    }

    @Override
    public void aoOcorrerEvento(TutorId tutorId, String chaveEvento, LocalDateTime ocorridoEm) {
        concessaoBadgeService.avaliarBadgesParaEvento(tutorId.getValor(), chaveEvento);
    }
}
