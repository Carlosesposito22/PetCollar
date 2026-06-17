package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import java.security.SecureRandom;
import java.util.Objects;

public final class CodigoIndicacao {

    private static final int TAMANHO = 8;
    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String valor;

    private CodigoIndicacao(String valor) { this.valor = valor; }

    public static CodigoIndicacao gerar() {
        StringBuilder sb = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }
        return new CodigoIndicacao(sb.toString());
    }

    public static CodigoIndicacao de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("Código de indicação não pode ser vazio.");
        if (!valor.matches("[A-Z0-9]{8}"))
            throw new IllegalArgumentException(
                "Código de indicação deve ter 8 caracteres alfanuméricos maiúsculos. Valor: " + valor);
        return new CodigoIndicacao(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodigoIndicacao other)) return false;
        return Objects.equals(valor, other.valor);
    }

    @Override public int hashCode() { return Objects.hash(valor); }
    @Override public String toString() { return valor; }
}
