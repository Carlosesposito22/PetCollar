package br.com.cesar.petCollar.dominio.compartilhado.eventos;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Porta do padrão Observer: contrato dos observadores que reagem a eventos do
 * tutor publicados por casos de uso de qualquer contexto (consulta concluída,
 * pagamento confirmado, indicação aceita, benefício utilizado etc.).
 */
public interface IObservadorDeEventoTutor {

    /**
     * @param tutorId    tutor ao qual o evento se refere
     * @param chaveEvento identificador estável do evento (ex.: "pagamento_confirmado")
     * @param ocorridoEm  instante em que o evento ocorreu
     */
    void aoOcorrerEvento(TutorId tutorId, String chaveEvento, LocalDateTime ocorridoEm);
}
