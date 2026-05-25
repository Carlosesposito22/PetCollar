package petcollar.dominio.gamificacao.conquista;

import java.time.LocalDateTime;

public class ProgressoBadge {

    private final ProgressoBadgeId id;
    private final String tutorId;
    private final BadgeId badgeId;
    private int valorAtual;
    private final int metaTotal;
    private final LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Construtor de CRIAÇÃO
    public ProgressoBadge(ProgressoBadgeId id, String tutorId, BadgeId badgeId, int metaTotal) {
        if (id == null)
            throw new IllegalArgumentException("ProgressoBadgeId não pode ser nulo.");
        if (tutorId == null || tutorId.isBlank())
            throw new IllegalArgumentException("TutorId não pode ser vazio.");
        if (badgeId == null)
            throw new IllegalArgumentException("BadgeId não pode ser nulo.");
        if (metaTotal < 1)
            throw new IllegalArgumentException("Meta total deve ser maior ou igual a 1.");
        this.id = id;
        this.tutorId = tutorId;
        this.badgeId = badgeId;
        this.valorAtual = 0;
        this.metaTotal = metaTotal;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public ProgressoBadge(ProgressoBadgeId id, String tutorId, BadgeId badgeId,
                          int valorAtual, int metaTotal,
                          LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.tutorId = tutorId;
        this.badgeId = badgeId;
        this.valorAtual = valorAtual;
        this.metaTotal = metaTotal;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public void incrementar(int quantidade) {
        if (quantidade <= 0)
            throw new IllegalArgumentException("Quantidade de incremento deve ser maior que zero.");
        this.valorAtual += quantidade;
        this.atualizadoEm = LocalDateTime.now();
    }

    public double calcularPercentualConclusao() {
        return ((double) valorAtual / metaTotal) * 100.0;
    }

    public boolean metaAtingida() {
        return valorAtual >= metaTotal;
    }

    public ProgressoBadgeId getId()        { return id; }
    public String getTutorId()             { return tutorId; }
    public BadgeId getBadgeId()            { return badgeId; }
    public int getValorAtual()             { return valorAtual; }
    public int getMetaTotal()              { return metaTotal; }
    public LocalDateTime getCriadoEm()     { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
