package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;

public interface IDiretivaConsentimentoRepositorio {

    List<TipoConduta> listarCondutasAutorizadas(PacienteId pacienteId);

    boolean verificarAutorizacao(PacienteId pacienteId, TipoConduta conduta);
}
