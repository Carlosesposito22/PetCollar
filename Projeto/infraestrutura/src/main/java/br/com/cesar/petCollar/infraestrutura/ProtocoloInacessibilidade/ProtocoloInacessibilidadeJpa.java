package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.configuracao.ConfiguracaoProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.EventoEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.MotivoEncerramento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TipoEncerramento;

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
import java.util.stream.Collectors;

@Entity
@Table(name = "protocolos_inacessibilidade")
public class ProtocoloInacessibilidadeJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String atendimentoId;

    @Column(nullable = false)
    private String pacienteId;

    @Column(nullable = false)
    private String tutorPrincipalId;

    @Column(nullable = false)
    private String configuracaoId;

    @Column(nullable = false)
    private String status;

    private String nivelEscalonamentoAtual;

    @Column(nullable = false)
    private boolean secundariosAcionados;

    private LocalDateTime ativadoEm;
    private LocalDateTime encerradoEm;

    private String motivoEncerramentoTipo;

    @Column(columnDefinition = "TEXT")
    private String motivoEncerramentoDetalhes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "protocolo_id")
    private List<TentativaContatoJpa> tentativas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "protocolo_id")
    private List<EventoEscalonamentoJpa> eventosEscalonamento = new ArrayList<>();

    protected ProtocoloInacessibilidadeJpa() {}

    public static ProtocoloInacessibilidadeJpa fromDomain(ProtocoloInacessibilidade p) {
        ProtocoloInacessibilidadeJpa jpa = new ProtocoloInacessibilidadeJpa();
        jpa.id = p.getId().getValor();
        jpa.atendimentoId = p.getAtendimentoId().getValor();
        jpa.pacienteId = p.getPacienteId().getValor();
        jpa.tutorPrincipalId = p.getTutorPrincipalId().getValor();
        jpa.configuracaoId = p.getConfiguracaoId().getValor();
        jpa.status = p.getStatus().name();
        jpa.nivelEscalonamentoAtual = p.getNivelEscalonamentoAtual() == null
            ? null : p.getNivelEscalonamentoAtual().name();
        jpa.secundariosAcionados = p.todosResponsaveisSecundariosAcionados();
        jpa.ativadoEm = p.getAtivadoEm();
        jpa.encerradoEm = p.getEncerradoEm();
        if (p.getMotivoEncerramento() != null) {
            jpa.motivoEncerramentoTipo = p.getMotivoEncerramento().getTipo().name();
            jpa.motivoEncerramentoDetalhes = p.getMotivoEncerramento().getDetalhes();
        }
        jpa.tentativas = p.getTentativas().stream()
            .map(TentativaContatoJpa::fromDomain)
            .collect(Collectors.toCollection(ArrayList::new));
        jpa.eventosEscalonamento = p.getEventosEscalonamento().stream()
            .map(EventoEscalonamentoJpa::fromDomain)
            .collect(Collectors.toCollection(ArrayList::new));
        return jpa;
    }

    public ProtocoloInacessibilidade toDomain() {
        List<TentativaContato> tentativasDominio = tentativas.stream()
            .map(TentativaContatoJpa::toDomain).toList();
        List<EventoEscalonamento> eventosDominio = eventosEscalonamento.stream()
            .map(EventoEscalonamentoJpa::toDomain).toList();
        MotivoEncerramento motivo = motivoEncerramentoTipo == null ? null
            : new MotivoEncerramento(
                TipoEncerramento.valueOf(motivoEncerramentoTipo), motivoEncerramentoDetalhes);
        return new ProtocoloInacessibilidade(
            ProtocoloId.de(id),
            AtendimentoId.de(atendimentoId),
            PacienteId.de(pacienteId),
            TutorId.de(tutorPrincipalId),
            ConfiguracaoProtocoloId.de(configuracaoId),
            StatusProtocolo.valueOf(status),
            nivelEscalonamentoAtual == null ? null : NivelEscalonamento.valueOf(nivelEscalonamentoAtual),
            secundariosAcionados,
            new ArrayList<>(tentativasDominio),
            new ArrayList<>(eventosDominio),
            ativadoEm,
            encerradoEm,
            motivo);
    }
}
