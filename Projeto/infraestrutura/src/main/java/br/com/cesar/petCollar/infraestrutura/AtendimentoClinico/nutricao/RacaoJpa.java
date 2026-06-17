package br.com.cesar.petCollar.infraestrutura.AtendimentoClinico.nutricao;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.FaixaEtaria;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Porte;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.Racao;
import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.RacaoId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "racoes")
public class RacaoJpa {

    @Id
    private String id;

    @Column(nullable = false) private String fabricante;
    @Column(nullable = false) private String linha;
    @Column(nullable = false, precision = 8, scale = 2) private BigDecimal densidadeCaloricaKcalPorKg;

    @Column(nullable = false) private String faixasIndicadas;
    @Column(nullable = false) private String portesIndicados;
    @Column(nullable = false) private String comorbidadesIndicadas;

    private Boolean desativada;

    protected RacaoJpa() {}

    public static RacaoJpa fromDomain(Racao r) {
        RacaoJpa j = new RacaoJpa();
        j.id = r.getId().getValor();
        j.fabricante = r.getFabricante();
        j.linha = r.getLinha();
        j.densidadeCaloricaKcalPorKg = r.getDensidadeCaloricaKcalPorKg();
        j.faixasIndicadas = serializar(r.getFaixasIndicadas());
        j.portesIndicados = serializar(r.getPortesIndicados());
        j.comorbidadesIndicadas = serializar(r.getComorbidadesIndicadas());
        j.desativada = r.isDesativada();
        return j;
    }

    public Racao toDomain() {
        Set<FaixaEtaria> faixas = desserializar(faixasIndicadas, FaixaEtaria::valueOf, FaixaEtaria.class);
        Set<Porte> portes = desserializar(portesIndicados, Porte::valueOf, Porte.class);
        Set<Comorbidade> comorbidades = desserializar(comorbidadesIndicadas, Comorbidade::valueOf, Comorbidade.class);
        return new Racao(
                RacaoId.de(id), fabricante, linha,
                densidadeCaloricaKcalPorKg, faixas, portes, comorbidades,
                Boolean.TRUE.equals(desativada));
    }

    public boolean isDesativada() { return Boolean.TRUE.equals(desativada); }

    private static <E extends Enum<E>> String serializar(Set<E> valores) {
        return valores.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private static <E extends Enum<E>> Set<E> desserializar(
            String csv, java.util.function.Function<String, E> parser, Class<E> tipo) {
        if (csv == null || csv.isBlank()) return EnumSet.noneOf(tipo);
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isBlank())
                .map(parser).collect(Collectors.toCollection(() -> EnumSet.noneOf(tipo)));
    }
}
