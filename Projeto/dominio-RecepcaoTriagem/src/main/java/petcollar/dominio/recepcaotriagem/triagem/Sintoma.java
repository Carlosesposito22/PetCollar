package petcollar.dominio.recepcaotriagem.triagem;

import java.util.Objects;

public final class Sintoma {

    private final String codigo;
    private final String descricao;
    private final int peso;

    public Sintoma(String codigo, String descricao, int peso) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código do sintoma não pode ser vazio.");
        if (descricao == null || descricao.isBlank())
            throw new IllegalArgumentException("Descrição do sintoma não pode ser vazia.");
        if (peso < 0)
            throw new IllegalArgumentException("Peso do sintoma não pode ser negativo.");
        this.codigo = codigo;
        this.descricao = descricao;
        this.peso = peso;
    }

    public String getCodigo()  { return codigo; }
    public String getDescricao(){ return descricao; }
    public int getPeso()       { return peso; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sintoma)) return false;
        Sintoma other = (Sintoma) o;
        return peso == other.peso
            && Objects.equals(codigo, other.codigo)
            && Objects.equals(descricao, other.descricao);
    }

    @Override
    public int hashCode() { return Objects.hash(codigo, descricao, peso); }
}
