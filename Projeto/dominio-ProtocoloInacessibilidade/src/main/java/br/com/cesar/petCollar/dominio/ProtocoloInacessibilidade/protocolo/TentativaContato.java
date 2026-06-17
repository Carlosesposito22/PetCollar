package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.CanalContato;

import java.time.LocalDateTime;

public final class TentativaContato {

    private final TentativaId id;
    private final String destinatarioId;
    private final TipoDestinatario tipoDestinatario;
    private final CanalContato canal;
    private final StatusTentativa status;
    private final LocalDateTime executadaEm;
    private final String mensagemRetorno;

    public TentativaContato(TentativaId id, String destinatarioId, TipoDestinatario tipoDestinatario,
                            CanalContato canal, StatusTentativa status, LocalDateTime executadaEm,
                            String mensagemRetorno) {
        if (id == null)
            throw new IllegalArgumentException("Id da tentativa não pode ser nulo.");
        if (destinatarioId == null || destinatarioId.isBlank())
            throw new IllegalArgumentException("Destinatário da tentativa não pode ser vazio.");
        if (tipoDestinatario == null)
            throw new IllegalArgumentException("Tipo de destinatário não pode ser nulo.");
        if (canal == null)
            throw new IllegalArgumentException("Canal da tentativa não pode ser nulo.");
        if (status == null)
            throw new IllegalArgumentException("Status da tentativa não pode ser nulo.");
        if (executadaEm == null)
            throw new IllegalArgumentException("Instante da tentativa não pode ser nulo.");
        this.id = id;
        this.destinatarioId = destinatarioId;
        this.tipoDestinatario = tipoDestinatario;
        this.canal = canal;
        this.status = status;
        this.executadaEm = executadaEm;
        this.mensagemRetorno = mensagemRetorno;
    }

    public boolean houveSucesso() {
        return status == StatusTentativa.EXECUTADA_COM_SUCESSO;
    }

    public TentativaId getId()                  { return id; }
    public String getDestinatarioId()           { return destinatarioId; }
    public TipoDestinatario getTipoDestinatario(){ return tipoDestinatario; }
    public CanalContato getCanal()              { return canal; }
    public StatusTentativa getStatus()          { return status; }
    public LocalDateTime getExecutadaEm()       { return executadaEm; }
    public String getMensagemRetorno()          { return mensagemRetorno; }
}
