package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.IMedicamentoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.InteracaoMedicamentosa;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;

@Repository
public class MedicamentoRepositorioJpa implements IMedicamentoRepositorio {

    private final MedicamentoJpaRepository jpa;
    private final InteracaoMedicamentosaJpaRepository interacoesJpa;

    public MedicamentoRepositorioJpa(MedicamentoJpaRepository jpa,
                                     InteracaoMedicamentosaJpaRepository interacoesJpa) {
        this.jpa = jpa;
        this.interacoesJpa = interacoesJpa;
    }

    @Override public void salvar(Medicamento m) { jpa.save(MedicamentoJpa.fromDomain(m)); }

    @Override public Optional<Medicamento> buscarPorId(MedicamentoId id) {
        return jpa.findById(id.getValor()).map(MedicamentoJpa::toDomain);
    }

    @Override public List<Medicamento> listarTodos() {
        return jpa.findAll().stream().map(MedicamentoJpa::toDomain).toList();
    }

    @Override public long contar() { return jpa.count(); }

    @Override public void registrarInteracao(InteracaoMedicamentosa interacao) {
        interacoesJpa.save(InteracaoMedicamentosaJpa.fromDomain(interacao));
    }

    @Override
    public List<InteracaoMedicamentosa> buscarInteracoesEntre(List<MedicamentoId> medicamentos) {
        if (medicamentos == null || medicamentos.size() < 2) return List.of();
        List<String> ids = medicamentos.stream().map(MedicamentoId::getValor).toList();
        // A matriz armazena pares ordenados (menor, maior). Buscamos com ambos
        // os lados na mesma lista — qualquer linha cuja A e B estejam na lista
        // representa um par presente na prescrição.
        return interacoesJpa.findByMedicamentoAIdInAndMedicamentoBIdIn(ids, ids).stream()
                .map(InteracaoMedicamentosaJpa::toDomain).toList();
    }
}
