package petcollar.dominio.gamificacao.conquista;

import java.time.LocalDateTime;

public class Badge {

    private final BadgeId id;
    private final String nome;
    private final String descricao;
    private final CategoriaBadge categoria;
    private final RaridadeBadge raridade;
    private final String chaveEvento;
    private final boolean eventoUnico;
    private final int metaQuantitativa;
    private final LocalDateTime criadoEm;

    // Construtor de CRIAÇÃO
    public Badge(BadgeId id, String nome, String descricao,
                 CategoriaBadge categoria, RaridadeBadge raridade,
                 String chaveEvento, boolean eventoUnico, int metaQuantitativa) {
        if (id == null)
            throw new IllegalArgumentException("BadgeId não pode ser nulo.");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome da badge não pode ser vazio.");
        if (descricao == null || descricao.isBlank())
            throw new IllegalArgumentException("Descrição da badge não pode ser vazia.");
        if (categoria == null)
            throw new IllegalArgumentException("Categoria da badge não pode ser nula.");
        if (raridade == null)
            throw new IllegalArgumentException("Raridade da badge não pode ser nula.");
        if (chaveEvento == null || chaveEvento.isBlank())
            throw new IllegalArgumentException("Chave de evento da badge não pode ser vazia.");
        if (metaQuantitativa < 1)
            throw new IllegalArgumentException("Meta quantitativa deve ser maior ou igual a 1.");
        if (eventoUnico && metaQuantitativa > 1)
            throw new IllegalArgumentException("Badge de evento único deve ter meta quantitativa igual a 1.");
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.raridade = raridade;
        this.chaveEvento = chaveEvento;
        this.eventoUnico = eventoUnico;
        this.metaQuantitativa = metaQuantitativa;
        this.criadoEm = LocalDateTime.now();
    }

    // Construtor de RECONSTRUÇÃO
    public Badge(BadgeId id, String nome, String descricao,
                 CategoriaBadge categoria, RaridadeBadge raridade,
                 String chaveEvento, boolean eventoUnico, int metaQuantitativa,
                 LocalDateTime criadoEm) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.raridade = raridade;
        this.chaveEvento = chaveEvento;
        this.eventoUnico = eventoUnico;
        this.metaQuantitativa = metaQuantitativa;
        this.criadoEm = criadoEm;
    }

    public BadgeId getId()                { return id; }
    public String getNome()               { return nome; }
    public String getDescricao()          { return descricao; }
    public CategoriaBadge getCategoria()  { return categoria; }
    public RaridadeBadge getRaridade()    { return raridade; }
    public String getChaveEvento()        { return chaveEvento; }
    public boolean isEventoUnico()        { return eventoUnico; }
    public int getMetaQuantitativa()      { return metaQuantitativa; }
    public LocalDateTime getCriadoEm()    { return criadoEm; }
}
