package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.util.List;
import java.util.Optional;

public interface IIndicacaoRepositorio {

    void salvar(Indicacao indicacao);

    Optional<Indicacao> buscarPorId(IndicacaoId id);

    /**
     * Verifica se o CPF já possui ao menos uma indicação com status CONVERTIDA (RN-10).
     * Independe de cancelamentos, reativações ou novas contratações futuras.
     */
    boolean existeConversaoPorCpf(CPF cpfIndicado);

    /** Retorna o histórico de indicações realizadas por um Tutor (painel do indicador). */
    List<Indicacao> listarPorTutorIndicador(TutorId tutorId);
}
