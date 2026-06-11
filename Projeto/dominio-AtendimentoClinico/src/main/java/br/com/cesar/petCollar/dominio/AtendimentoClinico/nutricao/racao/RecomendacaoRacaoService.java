package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao.EstrategiaRecomendacaoRacao.PerfilNutricional;

/**
 * Service de domínio que aplica as {@link EstrategiaRecomendacaoRacao} sobre
 * o catálogo e devolve o top-N ranqueado. Combinação por soma simples — o
 * peso relativo já vem embutido em cada Strategy (ver constantes internas).
 *
 * <p>Padrão Strategy + Service Layer (CLAUDE.md §8).
 */
public class RecomendacaoRacaoService {

    private final IRacaoRepositorio repositorio;
    private final List<EstrategiaRecomendacaoRacao> estrategias;

    public RecomendacaoRacaoService(IRacaoRepositorio repositorio,
                                    List<EstrategiaRecomendacaoRacao> estrategias) {
        if (repositorio == null)
            throw new IllegalArgumentException("IRacaoRepositorio é obrigatório.");
        if (estrategias == null || estrategias.isEmpty())
            throw new IllegalArgumentException("Pelo menos uma estratégia de recomendação é obrigatória.");
        this.repositorio = repositorio;
        this.estrategias = List.copyOf(estrategias);
    }

    public List<RacaoRecomendada> recomendarTop(PerfilNutricional perfil, int n) {
        if (perfil == null) throw new IllegalArgumentException("Perfil nutricional é obrigatório.");
        if (n <= 0) throw new IllegalArgumentException("N deve ser positivo.");

        return repositorio.listarTodas().stream()
                .map(racao -> pontuar(racao, perfil))
                .filter(r -> r.pontuacao() > 0)
                .sorted(Comparator.comparingInt(RacaoRecomendada::pontuacao).reversed())
                .limit(n)
                .toList();
    }

    private RacaoRecomendada pontuar(Racao racao, PerfilNutricional perfil) {
        Map<String, Integer> detalhes = new LinkedHashMap<>();
        int total = 0;
        for (EstrategiaRecomendacaoRacao e : estrategias) {
            int p = e.pontuar(racao, perfil);
            detalhes.put(e.nome(), p);
            total += p;
        }
        return new RacaoRecomendada(racao, total, Map.copyOf(detalhes));
    }
}
