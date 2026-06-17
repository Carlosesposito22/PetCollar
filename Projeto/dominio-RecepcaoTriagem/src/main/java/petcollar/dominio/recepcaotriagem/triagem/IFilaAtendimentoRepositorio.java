package petcollar.dominio.recepcaotriagem.triagem;

import java.util.List;

public interface IFilaAtendimentoRepositorio {
    void salvar(PosicaoFila posicao);
    List<PosicaoFila> listarOrdenada();
    void remover(TriagemId triagemId);
}
