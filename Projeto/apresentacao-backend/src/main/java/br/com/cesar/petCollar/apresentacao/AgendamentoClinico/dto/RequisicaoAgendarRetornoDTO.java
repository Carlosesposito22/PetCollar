package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

import java.time.LocalDateTime;

public record RequisicaoAgendarRetornoDTO(String pacienteId, String tutorId, String medicoId,
                                          String especialidadeId, String motivo,
                                          LocalDateTime inicio, LocalDateTime fim,
                                          String consultaOrigemId) {}
