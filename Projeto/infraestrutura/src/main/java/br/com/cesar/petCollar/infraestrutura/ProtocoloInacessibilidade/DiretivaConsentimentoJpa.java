package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entidade JPA que persiste as condutas autorizadas pelo tutor para um
 * paciente (porta IDiretivaConsentimentoRepositorio).
 */
@Entity
@Table(name = "diretivas_consentimento")
public class DiretivaConsentimentoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String pacienteId;

    @ElementCollection
    @CollectionTable(name = "diretiva_condutas", joinColumns = @JoinColumn(name = "diretiva_id"))
    @Column(name = "conduta")
    private List<String> condutas = new ArrayList<>();

    protected DiretivaConsentimentoJpa() {}

    public static DiretivaConsentimentoJpa criar(String pacienteId, List<TipoConduta> condutas) {
        DiretivaConsentimentoJpa jpa = new DiretivaConsentimentoJpa();
        jpa.pacienteId = pacienteId;
        jpa.condutas   = condutas.stream().map(TipoConduta::name)
                          .collect(Collectors.toCollection(ArrayList::new));
        return jpa;
    }

    public String getPacienteId() { return pacienteId; }

    public List<TipoConduta> getCondutas() {
        return condutas.stream().map(TipoConduta::valueOf).toList();
    }

    public void adicionarConduta(TipoConduta conduta) {
        String nome = conduta.name();
        if (!condutas.contains(nome)) condutas.add(nome);
    }
}
