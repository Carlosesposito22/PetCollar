package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.etapa;

import br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.protocolo.ProtocoloInacessibilidade;

/**
 * Orquestrador das etapas progressivas do protocolo. Recebe as três subclasses
 * concretas do {@link EtapaProtocoloService Template Method} e despacha, conforme
 * a máquina de estados do agregado, qual etapa executar a seguir — sem conhecer o
 * interior de cada etapa (o polimorfismo do Template Method cuida das variações).
 *
 * <p>É a partir daqui que um gatilho de infraestrutura (o scheduler de timeout, um
 * caso de uso, etc.) faz o protocolo avançar: tutor → responsáveis secundários →
 * escalonamento, até encerrar. Cada invocação executa <b>uma</b> etapa; chamar
 * repetidamente conduz o protocolo até um estado encerrado. Como cada etapa valida
 * o estado de entrada, a operação é idempotente em relação a estados não
 * executáveis (lança {@link IllegalStateException} para encerrados/inativos).
 */
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

    /**
     * Executa a próxima etapa adequada ao estado atual do protocolo.
     *
     * @throws IllegalStateException se o protocolo não estiver em um estado executável
     */
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
