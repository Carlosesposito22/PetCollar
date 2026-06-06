package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato.NivelEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.EventoEscalonamento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.StatusProtocolo;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.TentativaContato;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Visão consolidada do protocolo apresentada ao tutor (RN 15): status atual, nível
 * de escalonamento e o histórico auditável de tentativas e escalonamentos. Montada
 * a partir do agregado pelo {@link ConsultaStatusProtocoloService}.
 */
public final class VisaoProtocolo {

    private final ProtocoloId protocoloId;
    private final AtendimentoId atendimentoId;
    private final StatusProtocolo status;
    private final NivelEscalonamento nivelEscalonamentoAtual;
    private final LocalDateTime ativadoEm;
    private final LocalDateTime encerradoEm;
    private final List<TentativaContato> tentativas;
    private final List<EventoEscalonamento> eventosEscalonamento;

    private VisaoProtocolo(ProtocoloId protocoloId, AtendimentoId atendimentoId, StatusProtocolo status,
                           NivelEscalonamento nivelEscalonamentoAtual, LocalDateTime ativadoEm,
                           LocalDateTime encerradoEm, List<TentativaContato> tentativas,
                           List<EventoEscalonamento> eventosEscalonamento) {
        this.protocoloId = protocoloId;
        this.atendimentoId = atendimentoId;
        this.status = status;
        this.nivelEscalonamentoAtual = nivelEscalonamentoAtual;
        this.ativadoEm = ativadoEm;
        this.encerradoEm = encerradoEm;
        this.tentativas = tentativas;
        this.eventosEscalonamento = eventosEscalonamento;
    }

    public static VisaoProtocolo de(ProtocoloInacessibilidade protocolo) {
        return new VisaoProtocolo(
            protocolo.getId(),
            protocolo.getAtendimentoId(),
            protocolo.getStatus(),
            protocolo.getNivelEscalonamentoAtual(),
            protocolo.getAtivadoEm(),
            protocolo.getEncerradoEm(),
            protocolo.getTentativas(),
            protocolo.getEventosEscalonamento());
    }

    public ProtocoloId getProtocoloId()                    { return protocoloId; }
    public AtendimentoId getAtendimentoId()                { return atendimentoId; }
    public StatusProtocolo getStatus()                     { return status; }
    public NivelEscalonamento getNivelEscalonamentoAtual() { return nivelEscalonamentoAtual; }
    public LocalDateTime getAtivadoEm()                    { return ativadoEm; }
    public LocalDateTime getEncerradoEm()                  { return encerradoEm; }
    public List<TentativaContato> getTentativas()          { return tentativas; }
    public List<EventoEscalonamento> getEventosEscalonamento() { return eventosEscalonamento; }
}
