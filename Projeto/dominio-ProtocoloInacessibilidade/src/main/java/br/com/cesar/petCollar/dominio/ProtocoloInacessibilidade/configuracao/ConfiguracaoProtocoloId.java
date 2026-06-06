package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao;

import java.util.Objects;
import java.util.UUID;

public final class ConfiguracaoProtocoloId {

    private final String valor;

    private ConfiguracaoProtocoloId(String valor) {
        this.valor = valor;
    }

    public static ConfiguracaoProtocoloId gerar() {
        return new ConfiguracaoProtocoloId(UUID.randomUUID().toString());
    }

    public static ConfiguracaoProtocoloId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("ConfiguracaoProtocoloId não pode ser vazio.");
        return new ConfiguracaoProtocoloId(valor);
    }

    public String getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfiguracaoProtocoloId)) return false;
        return Objects.equals(valor, ((ConfiguracaoProtocoloId) o).valor);
    }

    @Override
    public int hashCode() { return Objects.hash(valor); }

    @Override
    public String toString() { return valor; }
}
