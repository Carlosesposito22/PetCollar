package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusProntuario;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpaRepository;

import org.springframework.stereotype.Component;

@Component
public class ProntuarioConsultaJpa implements IConsultaProntuario {

    private final PacienteJpaRepository pacientes;

    public ProntuarioConsultaJpa(PacienteJpaRepository pacientes) {
        this.pacientes = pacientes;
    }

    @Override
    public StatusProntuario obterStatus(PacienteId pacienteId) {
        return pacientes.existsById(pacienteId.getValor())
            ? StatusProntuario.ATIVO
            : StatusProntuario.INATIVO;
    }
}
