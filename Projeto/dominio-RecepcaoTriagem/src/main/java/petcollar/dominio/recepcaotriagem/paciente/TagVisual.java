package petcollar.dominio.recepcaotriagem.paciente;

import java.util.Objects;

public final class TagVisual {

    private final String codigo;
    private final String descricao;
    private final TipoTag tipo;
    private final boolean altaVisibilidade;

    public TagVisual(String codigo, String descricao, TipoTag tipo, boolean altaVisibilidade) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código da tag não pode ser vazio.");
        if (descricao == null || descricao.isBlank())
            throw new IllegalArgumentException("Descrição da tag não pode ser vazia.");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo da tag não pode ser nulo.");
        this.codigo = codigo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.altaVisibilidade = altaVisibilidade;
    }

    public String getCodigo()          { return codigo; }
    public String getDescricao()       { return descricao; }
    public TipoTag getTipo()           { return tipo; }
    public boolean isAltaVisibilidade(){ return altaVisibilidade; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagVisual)) return false;
        TagVisual other = (TagVisual) o;
        return altaVisibilidade == other.altaVisibilidade
            && Objects.equals(codigo, other.codigo)
            && Objects.equals(descricao, other.descricao)
            && tipo == other.tipo;
    }

    @Override
    public int hashCode() { return Objects.hash(codigo, descricao, tipo, altaVisibilidade); }
}
