package petcollar.dominio.recepcaotriagem.triagem;

import java.util.Comparator;
import java.util.List;

public class GestaoFilaAtendimentoService {

    private static final Comparator<PosicaoFila> ORDENACAO_FILA =
        Comparator

            .comparingInt(GestaoFilaAtendimentoService::prioridadeNumerica)

            .thenComparing(PosicaoFila::getFinalizadaEm);

    private final IFilaAtendimentoRepositorio repositorio;

    public GestaoFilaAtendimentoService(IFilaAtendimentoRepositorio repositorio) {
        if (repositorio == null)
            throw new IllegalArgumentException("Repositório de fila não pode ser nulo.");
        this.repositorio = repositorio;
    }

    public List<PosicaoFila> inserirNaFila(Triagem triagem) {
        if (triagem == null)
            throw new IllegalArgumentException("Triagem não pode ser nula.");
        if (triagem.getStatus() != StatusTriagem.FINALIZADA)
            throw new IllegalStateException(
                "Só é possível inserir na fila triagens com status FINALIZADA.");
        if (triagem.getCorDeRisco() == null)
            throw new IllegalStateException(
                "Triagem finalizada sem cor de risco definida.");

        PosicaoFila posicao = new PosicaoFila(
            triagem.getPacienteId(),
            triagem.getId(),
            triagem.getCorDeRisco(),
            triagem.getFinalizadaEm()
        );

        repositorio.salvar(posicao);
        return listarFila();
    }

    public List<PosicaoFila> listarFila() {
        return repositorio.listarOrdenada()
            .stream()
            .sorted(ORDENACAO_FILA)
            .toList();
    }

    public void removerDaFila(TriagemId triagemId) {
        if (triagemId == null)
            throw new IllegalArgumentException("TriagemId não pode ser nulo.");
        repositorio.remover(triagemId);
    }

    private static int prioridadeNumerica(PosicaoFila p) {
        return switch (p.getCorDeRisco()) {
            case VERMELHO -> 0;
            case AMARELO  -> 1;
            case VERDE    -> 2;
        };
    }
}
