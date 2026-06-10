package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CodigoIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ILinkIndicacaoRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacaoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class LinkIndicacaoRepositorioJpa implements ILinkIndicacaoRepositorio {

    private final LinkIndicacaoJpaRepository jpa;

    public LinkIndicacaoRepositorioJpa(LinkIndicacaoJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void salvar(LinkIndicacao link) {
        jpa.save(LinkIndicacaoJpa.fromDomain(link));
    }

    @Override
    public Optional<LinkIndicacao> buscarPorId(LinkIndicacaoId id) {
        return jpa.findById(id.getValor()).map(LinkIndicacaoJpa::toDomain);
    }

    @Override
    public Optional<LinkIndicacao> buscarPorTutorId(TutorId tutorId) {
        return jpa.findByTutorId(tutorId.getValor()).map(LinkIndicacaoJpa::toDomain);
    }

    @Override
    public Optional<LinkIndicacao> buscarPorCodigo(CodigoIndicacao codigo) {
        return jpa.findByCodigo(codigo.getValor()).map(LinkIndicacaoJpa::toDomain);
    }

    @Override
    public boolean existePorCodigo(CodigoIndicacao codigo) {
        return jpa.existsByCodigo(codigo.getValor());
    }
}
