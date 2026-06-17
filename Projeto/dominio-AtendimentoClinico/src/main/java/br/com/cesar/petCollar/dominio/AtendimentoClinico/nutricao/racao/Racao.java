package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;

public class Racao {

    private final RacaoId id;
    private String fabricante;
    private String linha;
    private BigDecimal densidadeCaloricaKcalPorKg;
    private Set<FaixaEtaria> faixasIndicadas;
    private Set<Porte> portesIndicados;
    private Set<Comorbidade> comorbidadesIndicadas;
    private boolean desativada;

    public Racao(RacaoId id,
                 String fabricante,
                 String linha,
                 BigDecimal densidadeCaloricaKcalPorKg,
                 Set<FaixaEtaria> faixasIndicadas,
                 Set<Porte> portesIndicados,
                 Set<Comorbidade> comorbidadesIndicadas) {
        if (id == null) throw new IllegalArgumentException("Id da ração é obrigatório.");
        validarCampos(fabricante, linha, densidadeCaloricaKcalPorKg, faixasIndicadas, portesIndicados);

        this.id = id;
        this.fabricante = fabricante;
        this.linha = linha;
        this.densidadeCaloricaKcalPorKg = densidadeCaloricaKcalPorKg;
        this.faixasIndicadas = Collections.unmodifiableSet(EnumSet.copyOf(faixasIndicadas));
        this.portesIndicados = Collections.unmodifiableSet(EnumSet.copyOf(portesIndicados));
        this.comorbidadesIndicadas = normalizarComorbidades(comorbidadesIndicadas);
        this.desativada = false;
    }

    public Racao(RacaoId id,
                 String fabricante,
                 String linha,
                 BigDecimal densidadeCaloricaKcalPorKg,
                 Set<FaixaEtaria> faixasIndicadas,
                 Set<Porte> portesIndicados,
                 Set<Comorbidade> comorbidadesIndicadas,
                 boolean desativada) {
        this(id, fabricante, linha, densidadeCaloricaKcalPorKg,
                faixasIndicadas, portesIndicados, comorbidadesIndicadas);
        this.desativada = desativada;
    }

    public void editar(String fabricante, String linha,
                       BigDecimal densidadeCaloricaKcalPorKg,
                       Set<FaixaEtaria> faixasIndicadas,
                       Set<Porte> portesIndicados,
                       Set<Comorbidade> comorbidadesIndicadas) {
        validarCampos(fabricante, linha, densidadeCaloricaKcalPorKg, faixasIndicadas, portesIndicados);
        this.fabricante = fabricante;
        this.linha = linha;
        this.densidadeCaloricaKcalPorKg = densidadeCaloricaKcalPorKg;
        this.faixasIndicadas = Collections.unmodifiableSet(EnumSet.copyOf(faixasIndicadas));
        this.portesIndicados = Collections.unmodifiableSet(EnumSet.copyOf(portesIndicados));
        this.comorbidadesIndicadas = normalizarComorbidades(comorbidadesIndicadas);
    }

    public void desativar() {
        if (this.desativada)
            throw new IllegalStateException("Ração já está desativada.");
        this.desativada = true;
    }

    public void reativar() {
        if (!this.desativada)
            throw new IllegalStateException("Ração já está ativa.");
        this.desativada = false;
    }

    public boolean isAtiva() { return !desativada; }

    public boolean compativelComFaixa(FaixaEtaria faixa)   { return faixasIndicadas.contains(faixa); }
    public boolean compativelComPorte(Porte porte)         { return portesIndicados.contains(porte); }
    public boolean cobreComorbidade(Comorbidade comorbidade) {
        return comorbidadesIndicadas.contains(comorbidade);
    }

    public String descricaoCurta() { return fabricante + " " + linha; }

    private static void validarCampos(String fabricante, String linha,
                                       BigDecimal densidade,
                                       Set<FaixaEtaria> faixas, Set<Porte> portes) {
        if (fabricante == null || fabricante.isBlank())
            throw new IllegalArgumentException("Fabricante da ração é obrigatório.");
        if (linha == null || linha.isBlank())
            throw new IllegalArgumentException("Linha/nome da ração é obrigatório.");
        if (densidade == null || densidade.signum() <= 0)
            throw new IllegalArgumentException("Densidade calórica deve ser positiva.");
        if (faixas == null || faixas.isEmpty())
            throw new IllegalArgumentException("Pelo menos uma faixa etária deve ser indicada.");
        if (portes == null || portes.isEmpty())
            throw new IllegalArgumentException("Pelo menos um porte deve ser indicado.");
    }

    private static Set<Comorbidade> normalizarComorbidades(Set<Comorbidade> entrada) {
        return entrada == null || entrada.isEmpty()
                ? Set.of(Comorbidade.NENHUMA)
                : Collections.unmodifiableSet(new LinkedHashSet<>(entrada));
    }

    public RacaoId getId()                                  { return id; }
    public String getFabricante()                           { return fabricante; }
    public String getLinha()                                { return linha; }
    public BigDecimal getDensidadeCaloricaKcalPorKg()       { return densidadeCaloricaKcalPorKg; }
    public Set<FaixaEtaria> getFaixasIndicadas()            { return faixasIndicadas; }
    public Set<Porte> getPortesIndicados()                  { return portesIndicados; }
    public Set<Comorbidade> getComorbidadesIndicadas()      { return comorbidadesIndicadas; }
    public boolean isDesativada()                           { return desativada; }
}
