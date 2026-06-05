package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Período de referência da Cobrança (mês/ano). VO imutável que encapsula
 * {@link YearMonth} para protegê-lo de manipulação direta no domínio.
 */
public final class Competencia {

    private final YearMonth valor;

    private Competencia(YearMonth valor) { this.valor = valor; }

    public static Competencia de(YearMonth valor) {
        if (valor == null)
            throw new IllegalArgumentException("Competência não pode ser nula.");
        return new Competencia(valor);
    }

    public static Competencia deTexto(String yyyyMM) {
        if (yyyyMM == null || yyyyMM.isBlank())
            throw new IllegalArgumentException("Competência não pode ser vazia.");
        return new Competencia(YearMonth.parse(yyyyMM));
    }

    public static Competencia de(int ano, int mes) {
        return new Competencia(YearMonth.of(ano, mes));
    }

    public YearMonth getValor() { return valor; }

    public Competencia proximaCompetencia() {
        return new Competencia(valor.plusMonths(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Competencia other)) return false;
        return Objects.equals(valor, other.valor);
    }

    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor.toString(); }
}
