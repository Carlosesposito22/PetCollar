package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Valor monetário do plano em reais. Encapsula o {@link BigDecimal} com 2 casas
 * decimais e validação de não-negatividade, evitando a propagação de double pelo
 * domínio.
 */
public final class ValorMensalidade {

    private final BigDecimal valor;

    private ValorMensalidade(BigDecimal valor) {
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static ValorMensalidade de(BigDecimal valor) {
        if (valor == null)
            throw new IllegalArgumentException("Valor da mensalidade não pode ser nulo.");
        if (valor.signum() < 0)
            throw new IllegalArgumentException("Valor da mensalidade não pode ser negativo.");
        return new ValorMensalidade(valor);
    }

    public static ValorMensalidade de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("Valor da mensalidade não pode ser vazio.");
        return de(new BigDecimal(valor));
    }

    public BigDecimal getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValorMensalidade other)) return false;
        return valor.compareTo(other.valor) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(valor.stripTrailingZeros()); }

    @Override
    public String toString() { return "R$ " + valor.toPlainString(); }
}
