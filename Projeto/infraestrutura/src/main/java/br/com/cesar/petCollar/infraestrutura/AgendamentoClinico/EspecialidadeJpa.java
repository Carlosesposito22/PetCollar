package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.Especialidade;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA do agregado {@link Especialidade}. Os médicos habilitados (RN 2)
 * são guardados como uma coleção de Strings de {@code MedicoId} (referência
 * cross-agregado), dentro do próprio agregado.
 */
@Entity
@Table(name = "especialidades")
public class EspecialidadeJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @ElementCollection
    @CollectionTable(name = "especialidade_medicos", joinColumns = @JoinColumn(name = "especialidade_id"))
    @Column(name = "medico_id")
    private List<String> medicoIds = new ArrayList<>();

    protected EspecialidadeJpa() {}

    public static EspecialidadeJpa fromDomain(Especialidade e, List<MedicoId> medicos) {
        EspecialidadeJpa jpa = new EspecialidadeJpa();
        jpa.id = e.getId().getValor();
        jpa.nome = e.getNome();
        jpa.descricao = e.getDescricao();
        jpa.medicoIds = medicos.stream().map(MedicoId::getValor)
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return jpa;
    }

    public Especialidade toDomain() {
        return new Especialidade(EspecialidadeId.de(id), nome, descricao);
    }

    public List<MedicoId> medicos() {
        return medicoIds.stream().map(MedicoId::de).toList();
    }
}
