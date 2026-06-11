package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.parametros.Comorbidade;

/**
 * Agregado raiz do catálogo de rações. É consultado pela
 * {@code RecomendacaoRacaoService} para sugerir top-N ao médico.
 *
 * <p>Cada ração declara para que {@link FaixaEtaria}s e {@link Porte}s é
 * indicada, e quais {@link Comorbidade}s ela cobre. A compatibilidade é
 * verificada pelos métodos {@code compativelCom*}, que servem de input para
 * as Strategies de recomendação.
 */
public final class Racao {

    private final RacaoId id;
    private final String fabricante;
    private final String linha;
    private final BigDecimal densidadeCaloricaKcalPorKg;
    private final Set<FaixaEtaria> faixasIndicadas;
    private final Set<Porte> portesIndicados;
    private final Set<Comorbidade> comorbidadesIndicadas;

    public Racao(RacaoId id,
                 String fabricante,
                 String linha,
                 BigDecimal densidadeCaloricaKcalPorKg,
                 Set<FaixaEtaria> faixasIndicadas,
                 Set<Porte> portesIndicados,
                 Set<Comorbidade> comorbidadesIndicadas) {
        if (id == null) throw new IllegalArgumentException("Id da ração é obrigatório.");
        if (fabricante == null || fabricante.isBlank())
            throw new IllegalArgumentException("Fabricante da ração é obrigatório.");
        if (linha == null || linha.isBlank())
            throw new IllegalArgumentException("Linha/nome da ração é obrigatório.");
        if (densidadeCaloricaKcalPorKg == null || densidadeCaloricaKcalPorKg.signum() <= 0)
            throw new IllegalArgumentException("Densidade calórica deve ser positiva.");
        if (faixasIndicadas == null || faixasIndicadas.isEmpty())
            throw new IllegalArgumentException("Pelo menos uma faixa etária deve ser indicada.");
        if (portesIndicados == null || portesIndicados.isEmpty())
            throw new IllegalArgumentException("Pelo menos um porte deve ser indicado.");

        this.id = id;
        this.fabricante = fabricante;
        this.linha = linha;
        this.densidadeCaloricaKcalPorKg = densidadeCaloricaKcalPorKg;
        this.faixasIndicadas = Collections.unmodifiableSet(EnumSet.copyOf(faixasIndicadas));
        this.portesIndicados = Collections.unmodifiableSet(EnumSet.copyOf(portesIndicados));
        this.comorbidadesIndicadas = comorbidadesIndicadas == null || comorbidadesIndicadas.isEmpty()
                ? Set.of(Comorbidade.NENHUMA)
                : Collections.unmodifiableSet(new LinkedHashSet<>(comorbidadesIndicadas));
    }

    public boolean compativelComFaixa(FaixaEtaria faixa)   { return faixasIndicadas.contains(faixa); }
    public boolean compativelComPorte(Porte porte)         { return portesIndicados.contains(porte); }
    public boolean cobreComorbidade(Comorbidade comorbidade) {
        return comorbidadesIndicadas.contains(comorbidade);
    }

    public String descricaoCurta() { return fabricante + " " + linha; }

    // ── Getters ───────────────────────────────────────────────────────────────
    public RacaoId getId()                                  { return id; }
    public String getFabricante()                           { return fabricante; }
    public String getLinha()                                { return linha; }
    public BigDecimal getDensidadeCaloricaKcalPorKg()       { return densidadeCaloricaKcalPorKg; }
    public Set<FaixaEtaria> getFaixasIndicadas()            { return faixasIndicadas; }
    public Set<Porte> getPortesIndicados()                  { return portesIndicados; }
    public Set<Comorbidade> getComorbidadesIndicadas()      { return comorbidadesIndicadas; }
}
