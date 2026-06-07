package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundario;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResponsavelSecundarioId;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entidade JPA para responsáveis secundários (porta ACL do ProtocoloInacessibilidade).
 * Os canais de contato são persistidos como strings (CanalContato.name()).
 */
@Entity
@Table(name = "responsaveis_secundarios")
public class ResponsavelSecundarioJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String pacienteId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private int prioridade;

    @ElementCollection
    @CollectionTable(name = "responsavel_canais", joinColumns = @JoinColumn(name = "responsavel_id"))
    @Column(name = "canal")
    private List<String> canais = new ArrayList<>();

    protected ResponsavelSecundarioJpa() {}

    public static ResponsavelSecundarioJpa fromDomain(String pacienteId, ResponsavelSecundario r) {
        ResponsavelSecundarioJpa jpa = new ResponsavelSecundarioJpa();
        jpa.id         = r.getId().getValor();
        jpa.pacienteId = pacienteId;
        jpa.nome       = r.getNome();
        jpa.prioridade = r.getPrioridade();
        jpa.canais     = r.getCanais().stream().map(CanalContato::name)
                          .collect(Collectors.toCollection(ArrayList::new));
        return jpa;
    }

    public ResponsavelSecundario toDomain() {
        List<CanalContato> lista = canais.stream()
                .map(CanalContato::valueOf).toList();
        return new ResponsavelSecundario(
                ResponsavelSecundarioId.de(id), nome, prioridade, lista);
    }

    public String getPacienteId() { return pacienteId; }
}
