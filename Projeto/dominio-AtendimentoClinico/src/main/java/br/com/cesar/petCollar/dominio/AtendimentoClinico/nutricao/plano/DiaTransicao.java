package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

public record DiaTransicao(
        String faixaDias,
        int percentualRacaoAtual,
        int percentualRacaoNova
) {
    public DiaTransicao {
        if (faixaDias == null || faixaDias.isBlank())
            throw new IllegalArgumentException("Faixa de dias é obrigatória.");
        if (percentualRacaoAtual < 0 || percentualRacaoAtual > 100)
            throw new IllegalArgumentException("Percentual de ração atual fora do intervalo 0–100.");
        if (percentualRacaoNova < 0 || percentualRacaoNova > 100)
            throw new IllegalArgumentException("Percentual de ração nova fora do intervalo 0–100.");
        if (percentualRacaoAtual + percentualRacaoNova != 100)
            throw new IllegalArgumentException(
                    "Soma dos percentuais deve ser 100% (atual + nova). Recebido: "
                            + (percentualRacaoAtual + percentualRacaoNova));
    }
}
