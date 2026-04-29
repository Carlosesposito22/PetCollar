package petcollar.dominio.assinaturafaturamento.indicacao;

import petcollar.dominio.assinaturafaturamento.CobrancaId;

import java.util.Objects;

public final class DescontoIndicado {

    private static final double PERCENTUAL = 0.30;

    private final CobrancaId cobrancaAplicadaId;
    private final boolean aplicado;

    public DescontoIndicado(CobrancaId cobrancaAplicadaId) {
        if (cobrancaAplicadaId == null)
            throw new IllegalArgumentException("cobrancaAplicadaId não pode ser nulo.");
        this.cobrancaAplicadaId = cobrancaAplicadaId;
        this.aplicado = false;
    }

    private DescontoIndicado(CobrancaId cobrancaAplicadaId, boolean aplicado) {
        this.cobrancaAplicadaId = cobrancaAplicadaId;
        this.aplicado = aplicado;
    }

    public DescontoIndicado aplicar() {
        return new DescontoIndicado(this.cobrancaAplicadaId, true);
    }

    public double getPercentualDesconto()       { return PERCENTUAL; }
    public CobrancaId getCobrancaAplicadaId()   { return cobrancaAplicadaId; }
    public boolean isAplicado()                 { return aplicado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DescontoIndicado)) return false;
        DescontoIndicado other = (DescontoIndicado) o;
        return aplicado == other.aplicado
                && Objects.equals(cobrancaAplicadaId, other.cobrancaAplicadaId);
    }

    @Override
    public int hashCode() { return Objects.hash(cobrancaAplicadaId, aplicado); }
}
