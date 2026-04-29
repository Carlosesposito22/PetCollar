package petCollar.dominio.AtendimentoClinico.nutricao;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CronogramaTransicao {

    private final int duracaoEmDias;
    private final List<DiaTransicao> diasTransicao;

    public CronogramaTransicao(int duracaoEmDias, List<DiaTransicao> diasTransicao) {
        if (duracaoEmDias <= 0)
            throw new IllegalArgumentException("Duração deve ser maior que zero.");
        if (diasTransicao == null || diasTransicao.isEmpty())
            throw new IllegalArgumentException("Lista de dias de transição não pode ser vazia.");
        if (diasTransicao.size() != duracaoEmDias)
            throw new IllegalArgumentException(
                "Número de dias na lista deve corresponder à duração informada.");

        this.duracaoEmDias = duracaoEmDias;
        this.diasTransicao = List.copyOf(diasTransicao);
    }

    public int getDuracaoEmDias() {
        return duracaoEmDias;
    }

    public List<DiaTransicao> getDiasTransicao() {
        return Collections.unmodifiableList(diasTransicao);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CronogramaTransicao)) return false;
        CronogramaTransicao that = (CronogramaTransicao) o;
        return duracaoEmDias == that.duracaoEmDias
                && Objects.equals(diasTransicao, that.diasTransicao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duracaoEmDias, diasTransicao);
    }

    @Override
    public String toString() {
        return "CronogramaTransicao{" +
                "duracaoEmDias=" + duracaoEmDias +
                ", diasTransicao=" + diasTransicao +
                '}';
    }
}
