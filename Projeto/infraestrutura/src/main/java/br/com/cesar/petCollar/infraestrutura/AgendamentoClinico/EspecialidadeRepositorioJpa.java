package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.Especialidade;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.IEspecialidadeRepositorio;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EspecialidadeRepositorioJpa implements IEspecialidadeRepositorio {

    private final EspecialidadeJpaRepository jpa;

    public EspecialidadeRepositorioJpa(EspecialidadeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Especialidade> listarTodas() {
        return jpa.findAll().stream().map(EspecialidadeJpa::toDomain).toList();
    }

    @Override
    public Optional<Especialidade> buscarPorId(EspecialidadeId id) {
        return jpa.findById(id.getValor()).map(EspecialidadeJpa::toDomain);
    }

    @Override
    public List<MedicoId> listarMedicosDaEspecialidade(EspecialidadeId id) {
        return jpa.findById(id.getValor())
            .map(EspecialidadeJpa::medicos)
            .orElseGet(List::of);
    }
}
