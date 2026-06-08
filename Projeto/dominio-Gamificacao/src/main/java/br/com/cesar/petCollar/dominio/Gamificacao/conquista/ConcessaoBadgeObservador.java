package br.com.cesar.petCollar.dominio.Gamificacao.conquista;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.compartilhado.eventos.IObservadorDeEventoTutor;

/**
 * Observador concreto: traduz eventos do tutor publicados por outros contextos
 * em avaliação de concessão de badges, sem que o publicador precise conhecer
 * {@link ConcessaoBadgeService}. Adapta o service de domínio já existente ao
 * papel de "observador" do padrão Observer (CLAUDE.md §8).
 */
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
