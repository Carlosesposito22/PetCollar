package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public interface ICobrancaRepositorio {

    void salvar(Cobranca cobranca);

    Optional<Cobranca> buscarPorId(CobrancaId id);

    /** Histórico completo do tutor — mais recente primeiro. */
    List<Cobranca> listarPorTutor(TutorId tutorId);

    /** Quantidade de cobranças com status {@code EM_ATRASO} (RN 6/7). */
    long contarEmAtrasoPorTutor(TutorId tutorId);

    /** Tutores distintos que possuem cobrança pendente (não paga) no plano dado. Usado pelo observer de alteração de plano. */
    List<TutorId> listarTutoresComCobrancaPendente(PlanoId planoId);
}
