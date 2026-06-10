package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.time.LocalDateTime;

/**
 * Agregado LinkIndicacao (RN-2).
 * Representa o link permanente e único gerado para um Tutor assinante ativo.
 * Não possui prazo de validade e não pode ser regenerado.
 */
public class LinkIndicacao {

    private final LinkIndicacaoId id;
    private final TutorId tutorId;
    private final CodigoIndicacao codigo;
    private final LocalDateTime criadoEm;

    // Construtor de CRIAÇÃO
    public LinkIndicacao(LinkIndicacaoId id, TutorId tutorId, CodigoIndicacao codigo) {
        if (id == null)     throw new IllegalArgumentException("Id do link de indicação não pode ser nulo.");
        if (tutorId == null) throw new IllegalArgumentException("TutorId não pode ser nulo.");
        if (codigo == null)  throw new IllegalArgumentException("Código de indicação não pode ser nulo.");
        this.id = id;
        this.tutorId = tutorId;
        this.codigo = codigo;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO — usado pelos adapters de persistência
    public LinkIndicacao(LinkIndicacaoId id, TutorId tutorId, CodigoIndicacao codigo,
                         LocalDateTime criadoEm) {
        this.id = id;
        this.tutorId = tutorId;
        this.codigo = codigo;
        this.criadoEm = criadoEm;
    }

    /** Monta a URL completa do link de indicação (RN-2). */
    public String getUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank())
            throw new IllegalArgumentException("URL base não pode ser vazia.");
        return baseUrl.stripTrailing() + "/indicacao/" + codigo.getValor();
    }

    public LinkIndicacaoId getId()       { return id; }
    public TutorId getTutorId()          { return tutorId; }
    public CodigoIndicacao getCodigo()   { return codigo; }
    public LocalDateTime getCriadoEm()  { return criadoEm; }
}
