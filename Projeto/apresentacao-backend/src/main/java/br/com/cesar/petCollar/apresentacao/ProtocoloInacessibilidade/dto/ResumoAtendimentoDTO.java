package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade.dto;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;

import java.time.LocalDateTime;

public record ResumoAtendimentoDTO(String atendimentoId, String pacienteId,
                                   String tutorPrincipalId, LocalDateTime ultimaInteracaoTutorEm,
                                   String nomePaciente) {

    public static ResumoAtendimentoDTO de(ResumoAtendimento r) {
        return new ResumoAtendimentoDTO(
            r.getAtendimentoId().getValor(),
            r.getPacienteId().getValor(),
            r.getTutorPrincipalId().getValor(),
            r.getUltimaInteracaoTutorEm(),
            r.getNomePaciente());
    }
}
