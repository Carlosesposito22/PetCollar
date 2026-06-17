package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.IDiretivaConsentimentoRepositorio;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;

import java.util.List;

public class ConsultaDiretivaConsentimentoService {

    private final IDiretivaConsentimentoRepositorio diretivas;

    public ConsultaDiretivaConsentimentoService(IDiretivaConsentimentoRepositorio diretivas) {
        if (diretivas == null)
            throw new IllegalArgumentException("Repositório de diretivas de consentimento não pode ser nulo.");
        this.diretivas = diretivas;
    }

    public boolean podeExecutarConduta(PacienteId pacienteId, TipoConduta conduta) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (conduta == null)
            throw new IllegalArgumentException("Tipo de conduta não pode ser nulo.");
        return diretivas.verificarAutorizacao(pacienteId, conduta);
    }

    public List<TipoConduta> listarCondutasAutorizadas(PacienteId pacienteId) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        return diretivas.listarCondutasAutorizadas(pacienteId);
    }
}
