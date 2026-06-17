package petcollar.dominio.recepcaotriagem.triagem;

import java.util.ArrayList;
import java.util.List;


public class GestaoFilaAtendimentoService {

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

    /**
     * Retorna todas as posições da fila ordenadas por prioridade,
     * percorrendo via FilaPorPrioridadeIterator.
     */
    public List<PosicaoFila> listarFila() {
        List<PosicaoFila> todas = repositorio.listarOrdenada();

        FilaPorPrioridadeIterator iterator = new FilaPorPrioridadeIterator(todas);

        List<PosicaoFila> ordenadas = new ArrayList<>();
        while (iterator.hasNext()) {
            ordenadas.add(iterator.next());
        }

        return ordenadas;
    }

 
    public void removerDaFila(TriagemId triagemId) {
        if (triagemId == null)
            throw new IllegalArgumentException("TriagemId não pode ser nulo.");
        repositorio.remover(triagemId);
    }

    public List<PosicaoFila> visualizarFilaOrdenada() {
        FilaPorPrioridadeIterator iterator =
            new FilaPorPrioridadeIterator(repositorio.listarOrdenada());
        return iterator.listarRestantes();
    }
}