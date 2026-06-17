package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import java.util.List;

public interface IResponsavelSecundarioRepositorio {

    List<ResponsavelSecundario> listarPorPaciente(PacienteId pacienteId);
}
