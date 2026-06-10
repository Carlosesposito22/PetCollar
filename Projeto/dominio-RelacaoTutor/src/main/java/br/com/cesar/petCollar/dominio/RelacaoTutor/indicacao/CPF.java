package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.util.Objects;

public final class CPF {

    private final String valor;

    private CPF(String valor) { this.valor = valor; }

    public static CPF de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("CPF não pode ser vazio.");
        String apenasDigitos = valor.replaceAll("[^0-9]", "");
        if (apenasDigitos.length() != 11)
            throw new IllegalArgumentException(
                "CPF deve conter 11 dígitos numéricos. Valor informado: " + valor);
        return new CPF(apenasDigitos);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CPF other)) return false;
        return Objects.equals(valor, other.valor);
    }

    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
