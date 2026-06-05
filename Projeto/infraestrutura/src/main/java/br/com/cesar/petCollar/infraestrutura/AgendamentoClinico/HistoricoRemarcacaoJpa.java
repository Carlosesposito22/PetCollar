package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HistoricoRemarcacao;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Linha-filha do agregado {@link br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta}
 * (RN 18). Por ser um value object sem identidade de domínio, usa um id técnico
 * gerado pelo banco.
 */
@Entity
@Table(name = "historico_remarcacoes")
public class HistoricoRemarcacaoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime anteriorInicio;
    private LocalDateTime anteriorFim;
    private LocalDateTime novoInicio;
    private LocalDateTime novoFim;
    private LocalDateTime remarcadoEm;

    protected HistoricoRemarcacaoJpa() {}

    public static HistoricoRemarcacaoJpa fromDomain(HistoricoRemarcacao h) {
        HistoricoRemarcacaoJpa jpa = new HistoricoRemarcacaoJpa();
        jpa.anteriorInicio = h.getHorarioAnterior().getInicio();
        jpa.anteriorFim = h.getHorarioAnterior().getFim();
        jpa.novoInicio = h.getHorarioNovo().getInicio();
        jpa.novoFim = h.getHorarioNovo().getFim();
        jpa.remarcadoEm = h.getRemarcadoEm();
        return jpa;
    }

    public HistoricoRemarcacao toDomain() {
        return new HistoricoRemarcacao(
            new HorarioConsulta(anteriorInicio, anteriorFim),
            new HorarioConsulta(novoInicio, novoFim),
            remarcadoEm);
    }
}
