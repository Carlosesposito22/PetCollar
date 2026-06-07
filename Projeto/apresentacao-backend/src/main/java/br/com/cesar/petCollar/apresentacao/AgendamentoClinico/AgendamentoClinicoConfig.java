package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

/**
 * Config de AgendamentoClinico migrada para infraestrutura.AgendamentoClinico.
 * Os services de domínio e o seed operacional vivem lá.
 * Os beans de ACL (ProntuarioConsultaEmMemoria, ExameConsultaEmMemoria,
 * ServicoNotificacaoLog) são detectados automaticamente via @Service/@Repository
 * neste pacote.
 */
public final class AgendamentoClinicoConfig {
    private AgendamentoClinicoConfig() {}
}
