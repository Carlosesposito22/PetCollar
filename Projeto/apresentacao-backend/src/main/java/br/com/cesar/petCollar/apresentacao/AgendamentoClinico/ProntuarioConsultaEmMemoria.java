package br.com.cesar.petCollar.apresentacao.AgendamentoClinico;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.IConsultaProntuario;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.porta.StatusProntuario;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Stand-in em memória da porta {@link IConsultaProntuario} (anticorrupção para o
 * contexto AtendimentoClinico). Por padrão considera o prontuário ATIVO; permite
 * marcar pacientes como INATIVO para exercitar a RN 1.
 */
@Component
public class ProntuarioConsultaEmMemoria implements IConsultaProntuario {

    private final ConcurrentMap<String, StatusProntuario> status = new ConcurrentHashMap<>();

    public void definirStatus(PacienteId pacienteId, StatusProntuario novoStatus) {
        status.put(pacienteId.getValor(), novoStatus);
    }

    @Override
    public StatusProntuario obterStatus(PacienteId pacienteId) {
        return status.getOrDefault(pacienteId.getValor(), StatusProntuario.ATIVO);
    }
}
