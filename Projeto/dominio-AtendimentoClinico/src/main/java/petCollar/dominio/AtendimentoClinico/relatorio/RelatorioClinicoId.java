package petCollar.dominio.AtendimentoClinico.relatorio;

import java.util.Objects;
import java.util.UUID;

public class RelatorioClinicoId {

    private final String valor;

    private RelatorioClinicoId(String valor) {
        this.valor = valor;
    }

    public static RelatorioClinicoId gerar() {
        return new RelatorioClinicoId(UUID.randomUUID().toString());
    }

    public static RelatorioClinicoId de(String valor) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("RelatorioClinicoId não pode ser vazio.");
        return new RelatorioClinicoId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelatorioClinicoId)) return false;
        return Objects.equals(valor, ((RelatorioClinicoId) o).valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
