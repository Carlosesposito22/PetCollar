package br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

@Repository
public class CobrancaRepositorioJpa implements ICobrancaRepositorio {

    private final CobrancaJpaRepository jpa;

    public CobrancaRepositorioJpa(CobrancaJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void salvar(Cobranca cobranca) {
        jpa.save(CobrancaJpa.fromDomain(cobranca));
    }

    @Override
    public Optional<Cobranca> buscarPorId(CobrancaId id) {
        return jpa.findById(id.getValor()).map(CobrancaJpa::toDomain);
    }

    @Override
    public List<Cobranca> listarPorTutor(TutorId tutorId) {
        return jpa.findByTutorIdOrderByVencimentoDesc(tutorId.getValor()).stream()
                .map(CobrancaJpa::toDomain)
                .toList();
    }

    @Override
    public long contarEmAtrasoPorTutor(TutorId tutorId) {
        return jpa.contarEmAtraso(tutorId.getValor(), LocalDate.now());
    }
}
