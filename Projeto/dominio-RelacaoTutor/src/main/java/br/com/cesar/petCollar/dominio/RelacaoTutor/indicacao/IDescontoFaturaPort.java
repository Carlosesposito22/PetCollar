package br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao;

import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

import java.math.BigDecimal;
import java.util.Optional;

public interface IDescontoFaturaPort {

    Optional<String> aplicarDescontoProximaFatura(TutorId tutorId, BigDecimal percentual);

    boolean metodoPagamentoCoincideComIndicador(TutorId tutorIndicador, String tokenMetodoPagamento);
}
