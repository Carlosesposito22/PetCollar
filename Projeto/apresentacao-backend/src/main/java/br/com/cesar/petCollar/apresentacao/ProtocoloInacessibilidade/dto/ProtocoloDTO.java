package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;

import java.time.LocalDateTime;
import java.util.List;

public record ProtocoloDTO(String id, String atendimentoId, String pacienteId, String tutorPrincipalId,
                           String configuracaoId, String status, String nivelEscalonamentoAtual,
                           boolean responsaveisSecundariosAcionados, LocalDateTime ativadoEm,
                           LocalDateTime encerradoEm, String motivoEncerramento,
                           List<TentativaContatoDTO> tentativas,
                           List<EventoEscalonamentoDTO> eventosEscalonamento) {

    public static ProtocoloDTO de(ProtocoloInacessibilidade p) {
        return new ProtocoloDTO(
            p.getId().getValor(),
            p.getAtendimentoId().getValor(),
            p.getPacienteId().getValor(),
            p.getTutorPrincipalId().getValor(),
            p.getConfiguracaoId().getValor(),
            p.getStatus().name(),
            p.getNivelEscalonamentoAtual() == null ? null : p.getNivelEscalonamentoAtual().name(),
            p.todosResponsaveisSecundariosAcionados(),
            p.getAtivadoEm(),
            p.getEncerradoEm(),
            p.getMotivoEncerramento() == null ? null : p.getMotivoEncerramento().toString(),
            p.getTentativas().stream().map(TentativaContatoDTO::de).toList(),
            p.getEventosEscalonamento().stream().map(EventoEscalonamentoDTO::de).toList());
    }
}
