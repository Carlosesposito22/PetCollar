package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.RegistroNotificacaoProtocolo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes_protocolo")
public class NotificacaoProtocoloJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String protocoloId;

    @Column(nullable = false)
    private String destinatarioId;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String corpo;

    @Column(nullable = false)
    private String criticidade;

    @Column(nullable = false)
    private LocalDateTime registradoEm;

    protected NotificacaoProtocoloJpa() {}

    public static NotificacaoProtocoloJpa criar(String id, String protocoloId,
                                                String destinatarioId, String titulo,
                                                String corpo, String criticidade,
                                                LocalDateTime registradoEm) {
        NotificacaoProtocoloJpa jpa = new NotificacaoProtocoloJpa();
        jpa.id = id;
        jpa.protocoloId = protocoloId;
        jpa.destinatarioId = destinatarioId;
        jpa.titulo = titulo;
        jpa.corpo = corpo;
        jpa.criticidade = criticidade;
        jpa.registradoEm = registradoEm;
        return jpa;
    }

    public RegistroNotificacaoProtocolo toDomain() {
        return new RegistroNotificacaoProtocolo(
            id, protocoloId, destinatarioId, titulo, corpo, criticidade, registradoEm);
    }
}
