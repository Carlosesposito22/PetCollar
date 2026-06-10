package br.com.cesar.petCollar.dominio.AtendimentoClinico.nutricao.plano;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cronograma de transição alimentar (F-11 RN 5). Imutável para evitar
 * mutação acidental do plano. Fábricas para os modelos padrão de 7, 10 e
 * 14 dias estão aqui mesmo para o frontend não precisar duplicar os números.
 */
public record CronogramaTransicao(TipoCronograma tipo, List<DiaTransicao> dias) {

    public CronogramaTransicao {
        if (tipo == null)
            throw new IllegalArgumentException("Tipo de cronograma é obrigatório.");
        if (dias == null || dias.isEmpty())
            throw new IllegalArgumentException("O cronograma precisa de pelo menos uma faixa de dias.");
        dias = Collections.unmodifiableList(new ArrayList<>(dias));
    }

    public static CronogramaTransicao padrao7Dias() {
        return new CronogramaTransicao(TipoCronograma.PADRAO_7_DIAS, List.of(
                new DiaTransicao("1-2", 75, 25),
                new DiaTransicao("3-4", 50, 50),
                new DiaTransicao("5-6", 25, 75),
                new DiaTransicao("7+",  0, 100)
        ));
    }

    public static CronogramaTransicao padrao10Dias() {
        return new CronogramaTransicao(TipoCronograma.PADRAO_10_DIAS, List.of(
                new DiaTransicao("1-3",  75, 25),
                new DiaTransicao("4-6",  50, 50),
                new DiaTransicao("7-9",  25, 75),
                new DiaTransicao("10+",  0, 100)
        ));
    }

    public static CronogramaTransicao padrao14Dias() {
        return new CronogramaTransicao(TipoCronograma.PADRAO_14_DIAS, List.of(
                new DiaTransicao("1-4",   75, 25),
                new DiaTransicao("5-8",   50, 50),
                new DiaTransicao("9-12",  25, 75),
                new DiaTransicao("13+",   0, 100)
        ));
    }
}
