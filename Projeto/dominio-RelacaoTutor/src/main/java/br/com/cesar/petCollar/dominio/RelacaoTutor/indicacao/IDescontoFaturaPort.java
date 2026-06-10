package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Port de saída para o contexto AssinaturaFaturamento (RN-5, RN-8, RN-9).
 * Desacopla o domínio de indicação das regras de cobrança.
 */
public interface IDescontoFaturaPort {

    /**
     * Aplica o percentual de desconto na próxima fatura em aberto do Tutor (RN-5, RN-9).
     * A implementação deve aplicar a regra mais vantajosa quando houver concorrência
     * com outros cupons (RN-9).
     * Retorna o id da cobrança que recebeu o desconto, ou vazio se não houver fatura aberta.
     */
    Optional<String> aplicarDescontoProximaFatura(TutorId tutorId, BigDecimal percentual);

    /**
     * Verifica se o método de pagamento utilizado pelo indicado é idêntico a um
     * já cadastrado pelo Tutor indicador (RN-8).
     */
    boolean metodoPagamentoCoincideComIndicador(TutorId tutorIndicador, String tokenMetodoPagamento);
}
