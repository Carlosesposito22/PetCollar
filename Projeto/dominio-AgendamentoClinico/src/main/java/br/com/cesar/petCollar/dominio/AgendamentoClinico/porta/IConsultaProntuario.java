package br.com.cesar.petCollar.dominio.AgendamentoClinico.porta;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

/**
 * Porta de saída (anticorrupção) para consultar o prontuário do paciente no
 * contexto AtendimentoClinico, sem acoplar AgendamentoClinico ao agregado Prontuario.
 * Usada para validar a RN 1 (prontuário ativo).
 */
public interface IConsultaProntuario {

    StatusProntuario obterStatus(PacienteId pacienteId);
}
