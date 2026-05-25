package petcollar.dominio.assinaturafaturamento.indicacao;

import petcollar.dominio.assinaturafaturamento.CobrancaId;

import java.util.Objects;

public final class DescontoIndicador {

    private static final double PERCENTUAL = 0.15;

    private final CobrancaId cobrancaAlvoId;
    private final boolean aplicado;

    public DescontoIndicador(CobrancaId cobrancaAlvoId) {
        if (cobrancaAlvoId == null)
            throw new IllegalArgumentException("cobrancaAlvoId não pode ser nulo.");
        this.cobrancaAlvoId = cobrancaAlvoId;
        this.aplicado = false;
    }

    private DescontoIndicador(CobrancaId cobrancaAlvoId, boolean aplicado) {
        this.cobrancaAlvoId = cobrancaAlvoId;
        this.aplicado = aplicado;
    }

    public DescontoIndicador aplicar() {
        return new DescontoIndicador(this.cobrancaAlvoId, true);
    }

    public double getPercentualDesconto()   { return PERCENTUAL; }
    public CobrancaId getCobrancaAlvoId()   { return cobrancaAlvoId; }
    public boolean isAplicado()             { return aplicado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DescontoIndicador)) return false;
        DescontoIndicador other = (DescontoIndicador) o;
        return aplicado == other.aplicado
                && Objects.equals(cobrancaAlvoId, other.cobrancaAlvoId);
    }

    @Override
    public int hashCode() { return Objects.hash(cobrancaAlvoId, aplicado); }
}
