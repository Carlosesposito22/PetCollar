package br.com.cesar.petCollar.apresentacao.AgendamentoClinico.dto;

/**
 * Requisição para o médico finalizar uma consulta realizada, indicando se há
 * direito a retorno e se existem exames pendentes de confirmação pelo tutor.
 *
 * <ul>
 *   <li>{@code temRetorno = false} → consulta encerrada sem elegibilidade de retorno</li>
 *   <li>{@code temRetorno = true, comExames = false} → retorno simples (status AGUARDANDO_RETORNO)</li>
 *   <li>{@code temRetorno = true, comExames = true} → retorno com exames pendentes (status EXAMES_SOLICITADOS)</li>
 * </ul>
 */
public record RequisicaoFinalizarConsultaDTO(boolean temRetorno, boolean comExames) {}
