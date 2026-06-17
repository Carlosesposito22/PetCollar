package br.com.cesar.petCollar.dominio.AgendamentoClinico.porta;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

public interface IConsultaProntuario {

    StatusProntuario obterStatus(PacienteId pacienteId);
}
