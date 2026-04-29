package petcollar.dominio.recepcaotriagem.triagem;

import java.util.Objects;

public final class RespostaSintoma {

    private final Sintoma sintoma;
    private final boolean presente;
    private final int pesoContribuido;

    public RespostaSintoma(Sintoma sintoma, boolean presente) {
        if (sintoma == null)
            throw new IllegalArgumentException("Sintoma não pode ser nulo.");
        this.sintoma = sintoma;
        this.presente = presente;
        this.pesoContribuido = presente ? sintoma.getPeso() : 0;
    }

    public Sintoma getSintoma()       { return sintoma; }
    public boolean isPresente()       { return presente; }
    public int getPesoContribuido()   { return pesoContribuido; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RespostaSintoma)) return false;
        RespostaSintoma other = (RespostaSintoma) o;
        return presente == other.presente
            && pesoContribuido == other.pesoContribuido
            && Objects.equals(sintoma, other.sintoma);
    }

    @Override
    public int hashCode() { return Objects.hash(sintoma, presente, pesoContribuido); }
}
