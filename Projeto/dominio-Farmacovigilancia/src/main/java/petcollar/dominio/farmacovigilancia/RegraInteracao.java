package petcollar.dominio.farmacovigilancia;

import java.util.Objects;

public final class RegraInteracao {

    private final MedicamentoId medicamentoA;
    private final MedicamentoId medicamentoB;
    private final GravidadeInteracao gravidade;
    private final String descricaoClinica;
    private final boolean bloqueante;

    public RegraInteracao(MedicamentoId medicamentoA,
                          MedicamentoId medicamentoB,
                          GravidadeInteracao gravidade,
                          String descricaoClinica,
                          boolean bloqueante) {
        if (medicamentoA == null)
            throw new IllegalArgumentException("medicamentoA não pode ser nulo.");
        if (medicamentoB == null)
            throw new IllegalArgumentException("medicamentoB não pode ser nulo.");
        if (gravidade == null)
            throw new IllegalArgumentException("gravidade não pode ser nula.");
        if (descricaoClinica == null || descricaoClinica.isBlank())
            throw new IllegalArgumentException("descricaoClinica não pode ser vazia.");
        this.medicamentoA = medicamentoA;
        this.medicamentoB = medicamentoB;
        this.gravidade = gravidade;
        this.descricaoClinica = descricaoClinica;
        this.bloqueante = bloqueante;
    }

    public MedicamentoId getMedicamentoA()     { return medicamentoA; }
    public MedicamentoId getMedicamentoB()     { return medicamentoB; }
    public GravidadeInteracao getGravidade()   { return gravidade; }
    public String getDescricaoClinica()        { return descricaoClinica; }
    public boolean isBloqueante()              { return bloqueante; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegraInteracao)) return false;
        RegraInteracao other = (RegraInteracao) o;
        return bloqueante == other.bloqueante
            && Objects.equals(medicamentoA, other.medicamentoA)
            && Objects.equals(medicamentoB, other.medicamentoB)
            && gravidade == other.gravidade
            && Objects.equals(descricaoClinica, other.descricaoClinica);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicamentoA, medicamentoB, gravidade, descricaoClinica, bloqueante);
    }
}
