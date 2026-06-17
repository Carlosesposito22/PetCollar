package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.time.LocalDateTime;

public class RegistroClique {

    private final RegistroCliqueId id;
    private final CPF cpfIndicado;
    private final LinkIndicacaoId linkId;
    private final TutorId tutorIndicadorId;
    private final LocalDateTime timestamp;

    public RegistroClique(RegistroCliqueId id, CPF cpfIndicado,
                          LinkIndicacaoId linkId, TutorId tutorIndicadorId,
                          LocalDateTime timestamp) {
        if (id == null)               throw new IllegalArgumentException("Id do registro de clique não pode ser nulo.");
        if (cpfIndicado == null)      throw new IllegalArgumentException("CPF do indicado não pode ser nulo.");
        if (linkId == null)           throw new IllegalArgumentException("Id do link não pode ser nulo.");
        if (tutorIndicadorId == null) throw new IllegalArgumentException("TutorId do indicador não pode ser nulo.");
        if (timestamp == null)        throw new IllegalArgumentException("Timestamp do clique não pode ser nulo.");
        this.id = id;
        this.cpfIndicado = cpfIndicado;
        this.linkId = linkId;
        this.tutorIndicadorId = tutorIndicadorId;
        this.timestamp = timestamp;
    }

    public RegistroCliqueId getId()           { return id; }
    public CPF getCpfIndicado()               { return cpfIndicado; }
    public LinkIndicacaoId getLinkId()        { return linkId; }
    public TutorId getTutorIndicadorId()      { return tutorIndicadorId; }
    public LocalDateTime getTimestamp()       { return timestamp; }
}
