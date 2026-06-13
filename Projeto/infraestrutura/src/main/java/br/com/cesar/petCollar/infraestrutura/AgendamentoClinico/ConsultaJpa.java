package br.com.cesar.petCollar.infraestrutura.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.EventoAgendamento;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HistoricoRemarcacao;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.HorarioConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.MotivoConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.StatusConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.TipoConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA do agregado {@link Consulta}. Ids e enums são persistidos como
 * String; referências a outros agregados/contextos (paciente, tutor, médico,
 * especialidade, consulta de origem) ficam apenas como o valor String do Id
 * (§6.2 do guia). As subentidades do próprio agregado (histórico e eventos) são
 * mapeadas com cascade total.
 */
@Entity
@Table(name = "consultas")
public class ConsultaJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String pacienteId;

    @Column(nullable = false)
    private String tutorId;

    @Column(nullable = false)
    private String medicoId;

    @Column(nullable = false)
    private String especialidadeId;

    @Column(nullable = false)
    private String tipo;                 // TipoConsulta.name()

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime horarioInicio;

    @Column(nullable = false)
    private LocalDateTime horarioFim;

    @Column(nullable = false)
    private String status;               // StatusConsulta.name()

    private String consultaOrigemId;     // referência cross-agregado (RN 11), pode ser nulo

    @Column(nullable = false)
    private int quantidadeRemarcacoes;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    private LocalDateTime confirmadaEm;
    private LocalDateTime canceladaEm;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "consulta_id")
    private List<HistoricoRemarcacaoJpa> historicoRemarcacoes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "consulta_id")
    private List<EventoAgendamentoJpa> eventos = new ArrayList<>();

    protected ConsultaJpa() {}

    public static ConsultaJpa fromDomain(Consulta c) {
        ConsultaJpa jpa = new ConsultaJpa();
        jpa.id = c.getId().getValor();
        jpa.pacienteId = c.getPacienteId().getValor();
        jpa.tutorId = c.getTutorId().getValor();
        jpa.medicoId = c.getMedicoId().getValor();
        jpa.especialidadeId = c.getEspecialidadeId().getValor();
        jpa.tipo = c.getTipo().name();
        jpa.motivo = c.getMotivo().getValor();
        jpa.horarioInicio = c.getHorario().getInicio();
        jpa.horarioFim = c.getHorario().getFim();
        jpa.status = c.getStatus().name();
        jpa.consultaOrigemId = c.getConsultaOrigemId() == null ? null : c.getConsultaOrigemId().getValor();
        jpa.quantidadeRemarcacoes = c.getQuantidadeRemarcacoes();
        jpa.criadaEm = c.getCriadaEm();
        jpa.confirmadaEm = c.getConfirmadaEm();
        jpa.canceladaEm = c.getCanceladaEm();
        jpa.historicoRemarcacoes = c.getHistoricoRemarcacoes().stream()
            .map(HistoricoRemarcacaoJpa::fromDomain)
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        jpa.eventos = c.getEventos().stream()
            .map(EventoAgendamentoJpa::fromDomain)
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return jpa;
    }

    /**
     * Projeta esta consulta como {@link ResumoAtendimento} para o protocolo de
     * inacessibilidade (ACL — §6.2). Uma consulta AGENDADA ou CONFIRMADA é considerada
     * "em andamento"; a última interação do tutor é a confirmação ou, se ainda não
     * confirmada, a criação da consulta.
     */
    public ResumoAtendimento toResumoAtendimento() {
        boolean emAndamento = "AGENDADA".equals(status) || "CONFIRMADA".equals(status);
        java.time.LocalDateTime ultimaInteracao = confirmadaEm != null ? confirmadaEm : criadaEm;
        return new ResumoAtendimento(
            AtendimentoId.de(id),
            PacienteId.de(pacienteId),
            TutorId.de(tutorId),
            ultimaInteracao,
            emAndamento);
    }

    public Consulta toDomain() {
        List<HistoricoRemarcacao> historico = historicoRemarcacoes.stream()
            .map(HistoricoRemarcacaoJpa::toDomain).toList();
        List<EventoAgendamento> listaEventos = eventos.stream()
            .map(EventoAgendamentoJpa::toDomain).toList();
        return new Consulta(
            ConsultaId.de(id),
            PacienteId.de(pacienteId),
            TutorId.de(tutorId),
            MedicoId.de(medicoId),
            EspecialidadeId.de(especialidadeId),
            TipoConsulta.valueOf(tipo),
            MotivoConsulta.de(motivo),
            new HorarioConsulta(horarioInicio, horarioFim),
            StatusConsulta.valueOf(status),
            consultaOrigemId == null ? null : ConsultaId.de(consultaOrigemId),
            quantidadeRemarcacoes,
            criadaEm,
            confirmadaEm,
            canceladaEm,
            new ArrayList<>(historico),
            new ArrayList<>(listaEventos));
    }
}
