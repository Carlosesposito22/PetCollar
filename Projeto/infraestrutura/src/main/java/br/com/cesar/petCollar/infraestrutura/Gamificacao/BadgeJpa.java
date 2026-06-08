package br.com.cesar.petCollar.infraestrutura.Gamificacao;

import java.time.LocalDateTime;

import br.com.cesar.petCollar.dominio.Gamificacao.conquista.Badge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.BadgeId;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.CategoriaBadge;
import br.com.cesar.petCollar.dominio.Gamificacao.conquista.RaridadeBadge;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA do agregado {@link Badge}. Enums (categoria, raridade) são
 * persistidos como String ({@code name()}/{@code valueOf}, §6.1 do CLAUDE.md).
 */
@Entity
@Table(name = "badges")
public class BadgeJpa {

    @Id
    private String id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String raridade;

    @Column(nullable = false)
    private String chaveEvento;

    @Column(nullable = false)
    private boolean eventoUnico;

    @Column(nullable = false)
    private int metaQuantitativa;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    protected BadgeJpa() {}

    public static BadgeJpa fromDomain(Badge b) {
        BadgeJpa j = new BadgeJpa();
        j.id = b.getId().getValor();
        j.nome = b.getNome();
        j.descricao = b.getDescricao();
        j.categoria = b.getCategoria().name();
        j.raridade = b.getRaridade().name();
        j.chaveEvento = b.getChaveEvento();
        j.eventoUnico = b.isEventoUnico();
        j.metaQuantitativa = b.getMetaQuantitativa();
        j.criadoEm = b.getCriadoEm();
        return j;
    }

    public Badge toDomain() {
        return new Badge(
                BadgeId.de(id),
                nome,
                descricao,
                CategoriaBadge.valueOf(categoria),
                RaridadeBadge.valueOf(raridade),
                chaveEvento,
                eventoUnico,
                metaQuantitativa,
                criadoEm
        );
    }

    public String getId()          { return id; }
    public String getChaveEvento() { return chaveEvento; }
}
