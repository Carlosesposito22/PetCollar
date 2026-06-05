package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;

import java.time.LocalDateTime;

public record ConsultaElegivelRetornoDTO(String id, String pacienteId, String medicoId,
                                         String especialidadeId, String status,
                                         LocalDateTime inicio) {

    public static ConsultaElegivelRetornoDTO de(Consulta c) {
        return new ConsultaElegivelRetornoDTO(
            c.getId().getValor(),
            c.getPacienteId().getValor(),
            c.getMedicoId().getValor(),
            c.getEspecialidadeId().getValor(),
            c.getStatus().name(),
            c.getHorario().getInicio());
    }
}
