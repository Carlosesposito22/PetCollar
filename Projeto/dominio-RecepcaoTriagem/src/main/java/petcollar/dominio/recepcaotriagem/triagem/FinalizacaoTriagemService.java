package petcollar.dominio.recepcaotriagem.triagem;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

public class FinalizacaoTriagemService {

    private final ITriagemRepositorio repositorio;

    public FinalizacaoTriagemService(ITriagemRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Triagem finalizar(TriagemId id) {
        Triagem triagem = repositorio.findById(id);
        if (triagem == null)
            throw new IllegalArgumentException("Triagem não encontrada com o id informado.");
        triagem.finalizar();
        repositorio.save(triagem);
        return triagem;
    }

    public Triagem criarCorrecao(TriagemId triagemAnteriorId) {
        Triagem anterior = repositorio.findById(triagemAnteriorId);
        if (anterior == null)
            throw new IllegalArgumentException("Triagem anterior não encontrada com o id informado.");
        anterior.bloquear();
        repositorio.save(anterior);

        TriagemId novoId = TriagemId.gerar();
        PacienteId pacienteId = anterior.getPacienteId();
        Triagem nova = new Triagem(novoId, pacienteId, triagemAnteriorId);
        repositorio.save(nova);
        return nova;
    }
}
