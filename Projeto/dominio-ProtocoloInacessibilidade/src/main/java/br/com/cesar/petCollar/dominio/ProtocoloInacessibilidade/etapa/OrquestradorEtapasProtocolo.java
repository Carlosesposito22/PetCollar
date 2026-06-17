package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;

public class OrquestradorEtapasProtocolo {

    private final EtapaContatoTutorService etapaTutor;
    private final EtapaContatoResponsaveisSecundariosService etapaSecundarios;
    private final EtapaEscalonamentoService etapaEscalonamento;

    public OrquestradorEtapasProtocolo(EtapaContatoTutorService etapaTutor,
                                       EtapaContatoResponsaveisSecundariosService etapaSecundarios,
                                       EtapaEscalonamentoService etapaEscalonamento) {
        if (etapaTutor == null || etapaSecundarios == null || etapaEscalonamento == null)
            throw new IllegalArgumentException("As três etapas do protocolo são obrigatórias.");
        this.etapaTutor = etapaTutor;
        this.etapaSecundarios = etapaSecundarios;
        this.etapaEscalonamento = etapaEscalonamento;
    }

    public ResultadoEtapa executarProximaEtapa(ProtocoloInacessibilidade protocolo) {
        if (protocolo == null)
            throw new IllegalArgumentException("Protocolo não pode ser nulo.");
        return switch (protocolo.getStatus()) {
            case ATIVADO, EM_TENTATIVA_TUTOR -> etapaTutor.executar(protocolo.getId());
            case EM_TENTATIVA_SECUNDARIOS -> protocolo.todosResponsaveisSecundariosAcionados()
                ? etapaEscalonamento.executar(protocolo.getId())
                : etapaSecundarios.executar(protocolo.getId());
            case EM_ESCALONAMENTO -> etapaEscalonamento.executar(protocolo.getId());
            default -> throw new IllegalStateException(
                "Protocolo não está em um estado executável: " + protocolo.getStatus());
        };
    }
}
