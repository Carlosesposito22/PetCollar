package br.com.cesar.petCollar.dominio.Farmacovigilancia.template;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Frequencia;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ViaAdministracao;

/**
 * Conjunto pré-configurado de itens de prescrição (Gastroproteção, Antiemético,
 * Antibiótico Amplo Espectro etc) que o médico pode aplicar com 1 clique.
 * O agregado fica em memória — é populado por seed e nunca é editado pela UI.
 */
public final class TemplatePrescricao {

    private final TemplatePrescricaoId id;
    private final String nome;
    private final String descricao;
    private final List<ItemTemplate> itens;

    public TemplatePrescricao(TemplatePrescricaoId id, String nome, String descricao,
                              List<ItemTemplate> itens) {
        if (id == null) throw new IllegalArgumentException("Id é obrigatório.");
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome é obrigatório.");
        if (itens == null || itens.isEmpty())
            throw new IllegalArgumentException("Template deve ter pelo menos 1 item.");
        this.id = id;
        this.nome = nome;
        this.descricao = descricao == null ? "" : descricao;
        this.itens = Collections.unmodifiableList(List.copyOf(itens));
    }

    public TemplatePrescricaoId getId()       { return id; }
    public String getNome()                   { return nome; }
    public String getDescricao()              { return descricao; }
    public List<ItemTemplate> getItens()      { return itens; }

    public record ItemTemplate(
            MedicamentoId medicamentoId,
            BigDecimal doseMgPorKg,
            int duracaoDias,
            Frequencia frequencia,
            ViaAdministracao via
    ) {
        public ItemTemplate {
            if (medicamentoId == null) throw new IllegalArgumentException("MedicamentoId é obrigatório.");
            if (doseMgPorKg == null || doseMgPorKg.signum() <= 0)
                throw new IllegalArgumentException("Dose mg/kg deve ser positiva.");
            if (duracaoDias <= 0)
                throw new IllegalArgumentException("Duração em dias deve ser positiva.");
            if (frequencia == null) throw new IllegalArgumentException("Frequência é obrigatória.");
            if (via == null) throw new IllegalArgumentException("Via é obrigatória.");
        }
    }
}
