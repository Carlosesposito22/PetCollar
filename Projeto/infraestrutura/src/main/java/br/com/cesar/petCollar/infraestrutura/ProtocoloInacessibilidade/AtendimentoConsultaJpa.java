package br.com.cesar.petCollar.infraestrutura.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.AtendimentoId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IConsultaAtendimento;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.ResumoAtendimento;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.ConsultaJpa;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.ConsultaJpaRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter JPA da porta {@link IConsultaAtendimento} (ACL para o contexto
 * AgendamentoClinico). Substituí o stand-in em memória; anotado com {@code @Primary}
 * para que o Spring prefira esta implementação.
 */
@Component
public class AtendimentoConsultaJpa implements IConsultaAtendimento {

    private final ConsultaJpaRepository jpa;

    public AtendimentoConsultaJpa(ConsultaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<ResumoAtendimento> buscarResumo(AtendimentoId atendimentoId) {
        return jpa.findById(atendimentoId.getValor())
                  .map(ConsultaJpa::toResumoAtendimento);
    }

    @Override
    public List<ResumoAtendimento> listarEmAndamento() {
        return jpa.findByStatusIn(List.of("AGENDADA", "CONFIRMADA"))
                  .stream()
                  .map(ConsultaJpa::toResumoAtendimento)
                  .toList();
    }
}
