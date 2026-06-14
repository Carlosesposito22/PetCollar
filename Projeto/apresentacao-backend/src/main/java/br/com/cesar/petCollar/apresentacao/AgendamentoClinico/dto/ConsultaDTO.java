package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;

import java.time.LocalDateTime;
import java.util.List;

public record ConsultaDTO(String id, String pacienteId, String tutorId, String medicoId,
                          String medicoNome, String especialidadeId, String tipo, String motivo,
                          LocalDateTime inicio, LocalDateTime fim, String status,
                          String consultaOrigemId, int quantidadeRemarcacoes,
                          List<HistoricoRemarcacaoDTO> historicoRemarcacoes) {

    public static ConsultaDTO de(Consulta c, String medicoNome) {
        return new ConsultaDTO(
            c.getId().getValor(),
            c.getPacienteId().getValor(),
            c.getTutorId().getValor(),
            c.getMedicoId().getValor(),
            medicoNome,
            c.getEspecialidadeId().getValor(),
            c.getTipo().name(),
            c.getMotivo().getValor(),
            c.getHorario().getInicio(),
            c.getHorario().getFim(),
            c.getStatus().name(),
            c.getConsultaOrigemId() == null ? null : c.getConsultaOrigemId().getValor(),
            c.getQuantidadeRemarcacoes(),
            c.getHistoricoRemarcacoes().stream().map(HistoricoRemarcacaoDTO::de).toList());
    }
}
