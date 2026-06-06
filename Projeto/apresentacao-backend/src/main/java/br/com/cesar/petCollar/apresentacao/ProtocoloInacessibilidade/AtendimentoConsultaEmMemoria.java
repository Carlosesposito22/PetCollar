package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Stand-in da porta {@link IConsultaAtendimento} (anticorrupção para o contexto
 * AtendimentoClinico/RecepcaoTriagem). Mantém os atendimentos em memória; será
 * substituído pelo adapter real quando aqueles contextos expuserem o read-model.
 */
@Component
public class AtendimentoConsultaEmMemoria implements IConsultaAtendimento {

    private final ConcurrentMap<String, ResumoAtendimento> atendimentos = new ConcurrentHashMap<>();

    public void registrar(ResumoAtendimento resumo) {
        atendimentos.put(resumo.getAtendimentoId().getValor(), resumo);
    }

    @Override
    public Optional<ResumoAtendimento> buscarResumo(AtendimentoId atendimentoId) {
        return Optional.ofNullable(atendimentos.get(atendimentoId.getValor()));
    }

    @Override
    public List<ResumoAtendimento> listarEmAndamento() {
        return atendimentos.values().stream()
            .filter(ResumoAtendimento::isEmAndamento)
            .toList();
    }
}
