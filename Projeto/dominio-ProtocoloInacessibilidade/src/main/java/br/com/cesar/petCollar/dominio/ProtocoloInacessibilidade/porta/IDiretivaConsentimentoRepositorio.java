package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;

/**
 * Porta de saída (anticorrupção) para consultar, no contexto AtendimentoClinico, as
 * diretivas de consentimento previamente assinadas pelo tutor (RN 10). Permite
 * autorizar ou bloquear condutas clínicas enquanto o tutor está inacessível.
 */
public interface IDiretivaConsentimentoRepositorio {

    /** Tipos de conduta para os quais o tutor autorizou previamente a execução. */
    List<TipoConduta> listarCondutasAutorizadas(PacienteId pacienteId);

    /** {@code true} se o tutor autorizou previamente a conduta indicada (RN 10). */
    boolean verificarAutorizacao(PacienteId pacienteId, TipoConduta conduta);
}
