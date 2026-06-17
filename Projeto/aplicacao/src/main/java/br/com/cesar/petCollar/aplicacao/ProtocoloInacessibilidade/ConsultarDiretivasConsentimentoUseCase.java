package br.com.cesar.petCollar.aplicacao.ProtocoloInacessibilidade;

import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.porta.TipoConduta;
import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.servico.ConsultaDiretivaConsentimentoService;

import java.util.List;

/**
 * Caso de uso que consulta as diretivas de consentimento do tutor para um
 * paciente (RN 10), retornando as condutas clínicas autorizadas previamente.
 * Usado durante a execução do protocolo de inacessibilidade para garantir que
 * só condutas autorizadas sejam aplicadas.
 */
public class ConsultarDiretivasConsentimentoUseCase {

    private final ConsultaDiretivaConsentimentoService consultaService;

    public ConsultarDiretivasConsentimentoUseCase(ConsultaDiretivaConsentimentoService consultaService) {
        if (consultaService == null)
            throw new IllegalArgumentException("ConsultaDiretivaConsentimentoService é obrigatório.");
        this.consultaService = consultaService;
    }

    public List<TipoConduta> executar(PacienteId pacienteId) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        return consultaService.listarCondutasAutorizadas(pacienteId);
    }

    public boolean condutaAutorizada(PacienteId pacienteId, TipoConduta conduta) {
        if (pacienteId == null)
            throw new IllegalArgumentException("Id do paciente não pode ser nulo.");
        if (conduta == null)
            throw new IllegalArgumentException("Tipo de conduta não pode ser nulo.");
        return consultaService.podeExecutarConduta(pacienteId, conduta);
    }
}
