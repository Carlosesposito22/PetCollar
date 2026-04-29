package petCollar.dominio.AtendimentoClinico.nutricao;

import java.util.ArrayList;
import java.util.List;

public class GeracaoCronogramaTransicaoService {

    public CronogramaTransicao gerarCronograma7Dias() {
        List<DiaTransicao> dias = new ArrayList<>();

        dias.add(new DiaTransicao(1, 25.0, 75.0));
        dias.add(new DiaTransicao(2, 37.5, 62.5));
        dias.add(new DiaTransicao(3, 50.0, 50.0));
        dias.add(new DiaTransicao(4, 62.5, 37.5));
        dias.add(new DiaTransicao(5, 75.0, 25.0));
        dias.add(new DiaTransicao(6, 87.5, 12.5));
        dias.add(new DiaTransicao(7, 100.0, 0.0));

        return new CronogramaTransicao(7, dias);
    }
}
