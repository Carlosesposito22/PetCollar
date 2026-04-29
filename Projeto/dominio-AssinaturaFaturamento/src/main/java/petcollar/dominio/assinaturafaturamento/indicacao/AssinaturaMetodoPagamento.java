package petcollar.dominio.assinaturafaturamento.indicacao;

import java.util.Objects;

public final class AssinaturaMetodoPagamento {

    private final String hashMetodo;
    private final String origemGateway;

    public AssinaturaMetodoPagamento(String hashMetodo, String origemGateway) {
        if (hashMetodo == null || hashMetodo.isBlank())
            throw new IllegalArgumentException("hashMetodo não pode ser vazio.");
        if (origemGateway == null || origemGateway.isBlank())
            throw new IllegalArgumentException("origemGateway não pode ser vazio.");
        this.hashMetodo = hashMetodo;
        this.origemGateway = origemGateway;
    }

    public String getHashMetodo()      { return hashMetodo; }
    public String getOrigemGateway()   { return origemGateway; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssinaturaMetodoPagamento)) return false;
        return Objects.equals(hashMetodo, ((AssinaturaMetodoPagamento) o).hashMetodo);
    }

    @Override
    public int hashCode() { return Objects.hash(hashMetodo); }

    @Override
    public String toString() { return "MetodoPagamento{hash=" + hashMetodo + ", gateway=" + origemGateway + "}"; }
}
