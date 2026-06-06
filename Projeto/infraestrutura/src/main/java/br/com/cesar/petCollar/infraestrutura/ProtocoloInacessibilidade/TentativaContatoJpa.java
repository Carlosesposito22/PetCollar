package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusTentativa;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TipoDestinatario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidade JPA da subentidade {@link TentativaContato} (filho do agregado
 * {@link br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade}).
 * Ids e enums são persistidos como String (§6.1 do guia).
 */
@Entity
@Table(name = "tentativas_contato")
public class TentativaContatoJpa {

    @Id
    private String id;

    @Column(nullable = false)
    private String destinatarioId;

    @Column(nullable = false)
    private String tipoDestinatario;     // TipoDestinatario.name()

    @Column(nullable = false)
    private String canal;                // CanalContato.name()

    @Column(nullable = false)
    private String status;               // StatusTentativa.name()

    @Column(nullable = false)
    private LocalDateTime executadaEm;

    @Column(columnDefinition = "TEXT")
    private String mensagemRetorno;

    protected TentativaContatoJpa() {}

    public static TentativaContatoJpa fromDomain(TentativaContato t) {
        TentativaContatoJpa jpa = new TentativaContatoJpa();
        jpa.id = t.getId().getValor();
        jpa.destinatarioId = t.getDestinatarioId();
        jpa.tipoDestinatario = t.getTipoDestinatario().name();
        jpa.canal = t.getCanal().name();
        jpa.status = t.getStatus().name();
        jpa.executadaEm = t.getExecutadaEm();
        jpa.mensagemRetorno = t.getMensagemRetorno();
        return jpa;
    }

    public TentativaContato toDomain() {
        return new TentativaContato(
            TentativaId.de(id),
            destinatarioId,
            TipoDestinatario.valueOf(tipoDestinatario),
            CanalContato.valueOf(canal),
            StatusTentativa.valueOf(status),
            executadaEm,
            mensagemRetorno);
    }
}
