package br.com.cesar.petCollar.apresentacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementação em memória desativada (não é bean Spring). A persistência real é
 * feita por {@code AtendimentoConsultaJpa} na camada de infraestrutura.
 * Mantida apenas como referência/fallback local de desenvolvimento.
 */
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
