package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca;

import java.util.List;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public interface ICobrancaRepositorio {

    void salvar(Cobranca cobranca);

    Optional<Cobranca> buscarPorId(CobrancaId id);

    List<Cobranca> listarPorTutor(TutorId tutorId);

    long contarEmAtrasoPorTutor(TutorId tutorId);

    List<TutorId> listarTutoresComCobrancaPendente(PlanoId planoId);
}
