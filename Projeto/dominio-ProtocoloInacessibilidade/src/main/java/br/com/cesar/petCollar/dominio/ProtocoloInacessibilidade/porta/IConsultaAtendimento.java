package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;

import java.util.List;
import java.util.Optional;

public interface IConsultaAtendimento {

    Optional<ResumoAtendimento> buscarResumo(AtendimentoId atendimentoId);

    List<ResumoAtendimento> listarEmAndamento();
}
