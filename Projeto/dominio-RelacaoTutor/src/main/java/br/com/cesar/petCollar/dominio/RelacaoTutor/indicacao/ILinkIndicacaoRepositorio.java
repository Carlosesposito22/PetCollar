package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.util.Optional;

public interface ILinkIndicacaoRepositorio {

    void salvar(LinkIndicacao link);

    Optional<LinkIndicacao> buscarPorId(LinkIndicacaoId id);

    Optional<LinkIndicacao> buscarPorTutorId(TutorId tutorId);

    Optional<LinkIndicacao> buscarPorCodigo(CodigoIndicacao codigo);

    boolean existePorCodigo(CodigoIndicacao codigo);
}
