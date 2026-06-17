package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

public record RequisicaoFinalizarConsultaDTO(boolean temRetorno, boolean comExames,
                                             java.util.List<String> examesSolicitados) {}
