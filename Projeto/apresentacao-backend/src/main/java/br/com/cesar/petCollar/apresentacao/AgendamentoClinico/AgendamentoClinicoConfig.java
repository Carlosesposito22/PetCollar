package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

/**
 * Config de AgendamentoClinico migrada para infraestrutura.AgendamentoClinico
 * (services de domínio + wiring JPA). O seed operacional vive em {@link SeedInicial}.
 * Os adapters de ACL apoiados no banco ({@code ProntuarioConsultaJpa},
 * {@code ExameConsultaJpa}) e o {@code ServicoNotificacaoLog} são detectados via
 * component-scan (@Component/@Repository), sem stand-ins em memória.
 */
public final class AgendamentoClinicoConfig {
    private AgendamentoClinicoConfig() {}
}
