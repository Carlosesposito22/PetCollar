package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Frequencia;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ViaAdministracao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.TemplatePrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.TemplatePrescricaoId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "templates_prescricao")
public class TemplatePrescricaoJpa {

    @Id
    private String id;

    @Column(nullable = false) private String nome;
    @Column(columnDefinition = "TEXT") private String descricao;

    @Column(nullable = false, columnDefinition = "TEXT") private String itensTexto;

    protected TemplatePrescricaoJpa() {}

    public static TemplatePrescricaoJpa fromDomain(TemplatePrescricao t) {
        TemplatePrescricaoJpa j = new TemplatePrescricaoJpa();
        j.id = t.getId().getValor();
        j.nome = t.getNome();
        j.descricao = t.getDescricao();
        j.itensTexto = t.getItens().stream()
                .map(it -> it.medicamentoId().getValor() + ":" + it.doseMgPorKg()
                        + ":" + it.duracaoDias()
                        + ":" + it.frequencia().name()
                        + ":" + it.via().name())
                .collect(Collectors.joining("|"));
        return j;
    }

    public TemplatePrescricao toDomain() {
        List<TemplatePrescricao.ItemTemplate> itens = new ArrayList<>();
        if (itensTexto != null && !itensTexto.isBlank()) {
            for (String parte : itensTexto.split("\\|")) {
                String[] c = parte.split(":");
                itens.add(new TemplatePrescricao.ItemTemplate(
                        MedicamentoId.de(c[0]),
                        new BigDecimal(c[1]),
                        Integer.parseInt(c[2]),
                        Frequencia.valueOf(c[3]),
                        ViaAdministracao.valueOf(c[4])));
            }
        }
        return new TemplatePrescricao(TemplatePrescricaoId.de(id), nome, descricao, itens);
    }
}
