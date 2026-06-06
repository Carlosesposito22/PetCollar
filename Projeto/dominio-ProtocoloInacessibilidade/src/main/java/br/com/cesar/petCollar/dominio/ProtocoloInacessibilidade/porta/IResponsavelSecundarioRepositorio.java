package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;

/**
 * Porta de saída (anticorrupção) para consultar, no contexto RelacaoTutor, os
 * responsáveis secundários cadastrados para o paciente. Usada pela RN 4 antes de
 * qualquer escalonamento (RN 5).
 */
public interface IResponsavelSecundarioRepositorio {

    /** Responsáveis secundários do paciente, já ordenados por prioridade (RN 4). */
    List<ResponsavelSecundario> listarPorPaciente(PacienteId pacienteId);
}
