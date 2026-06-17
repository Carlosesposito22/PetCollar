package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.util.List;
import java.util.Optional;

public interface IIndicacaoRepositorio {

    void salvar(Indicacao indicacao);

    Optional<Indicacao> buscarPorId(IndicacaoId id);

    boolean existeConversaoPorCpf(CPF cpfIndicado);

    List<Indicacao> listarPorTutorIndicador(TutorId tutorId);

    Optional<Indicacao> buscarPendenteParaCpfIndicado(CPF cpfIndicado);
}
