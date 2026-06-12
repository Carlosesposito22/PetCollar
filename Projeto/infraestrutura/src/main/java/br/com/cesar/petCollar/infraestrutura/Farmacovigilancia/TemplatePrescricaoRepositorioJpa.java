package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.ITemplatePrescricaoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.TemplatePrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.TemplatePrescricaoId;

@Repository
public class TemplatePrescricaoRepositorioJpa implements ITemplatePrescricaoRepositorio {

    private final TemplatePrescricaoJpaRepository jpa;

    public TemplatePrescricaoRepositorioJpa(TemplatePrescricaoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override public void salvar(TemplatePrescricao t) { jpa.save(TemplatePrescricaoJpa.fromDomain(t)); }

    @Override public Optional<TemplatePrescricao> buscarPorId(TemplatePrescricaoId id) {
        return jpa.findById(id.getValor()).map(TemplatePrescricaoJpa::toDomain);
    }

    @Override public List<TemplatePrescricao> listarTodos() {
        return jpa.findAll().stream().map(TemplatePrescricaoJpa::toDomain).toList();
    }

    @Override public long contar() { return jpa.count(); }
}
