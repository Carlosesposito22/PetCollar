package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.racao;

import java.util.List;
import java.util.Map;

/**
 * Resultado de uma recomendação: a ração + a pontuação total + o breakdown
 * por Strategy, útil para a UI explicar "por que esta ração foi sugerida".
 */
public record RacaoRecomendada(Racao racao, int pontuacao, Map<String, Integer> detalhes) {

    public RacaoRecomendada {
        if (racao == null) throw new IllegalArgumentException("Ração é obrigatória.");
        if (pontuacao < 0) throw new IllegalArgumentException("Pontuação não pode ser negativa.");
        if (detalhes == null) detalhes = Map.of();
    }

    public List<String> motivosFortes() {
        return detalhes.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }
}
