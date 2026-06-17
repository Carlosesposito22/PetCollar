package petcollar.dominio.recepcaotriagem.triagem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public class FilaPorPrioridadeIterator implements Iterator<PosicaoFila> {

    private static final Comparator<PosicaoFila> COMPARADOR_FILA =
        Comparator
            .comparingInt(FilaPorPrioridadeIterator::prioridade)
            .thenComparing(PosicaoFila::getFinalizadaEm);

    private final List<PosicaoFila> posicoes;
    private int cursor;


    public FilaPorPrioridadeIterator(List<PosicaoFila> posicoes) {
        if (posicoes == null)
            throw new IllegalArgumentException("Lista de posições não pode ser nula.");

        List<PosicaoFila> copia = new ArrayList<>(posicoes);
        copia.sort(COMPARADOR_FILA);

        this.posicoes = copia;
        this.cursor   = 0;
    }

    @Override
    public boolean hasNext() {
        return cursor < posicoes.size();
    }

    @Override
    public PosicaoFila next() {
        if (!hasNext())
            throw new NoSuchElementException(
                "Não há mais posições na fila de atendimento.");
        return posicoes.get(cursor++);
    }

    public int posicaoAtual() {
        return cursor;
    }

 
    public List<PosicaoFila> listarRestantes() {
        return List.copyOf(posicoes.subList(cursor, posicoes.size()));
    }



    private static int prioridade(PosicaoFila p) {
        return switch (p.getCorDeRisco()) {
            case VERMELHO -> 0;
            case AMARELO  -> 1;
            case VERDE    -> 2;
        };
    }
}