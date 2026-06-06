package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato;

/**
 * Níveis de escalonamento progressivo acionados quando tutor e responsáveis
 * secundários não respondem (RN 6). Enum com comportamento: cada nível conhece
 * sua ordem de prioridade (crescente) e a criticidade da notificação que dispara
 * (RN 9). A configuração vigente decide quais níveis estão habilitados; a ordem
 * aqui define a sequência canônica de avanço.
 */
public enum NivelEscalonamento {

    NIVEL_1_ADMINISTRATIVO(1, NivelCriticidade.ALTA),
    NIVEL_2_COORDENACAO(2, NivelCriticidade.ALTA),
    NIVEL_3_CLINICO(3, NivelCriticidade.CRITICA),
    NIVEL_4_DIRECAO(4, NivelCriticidade.CRITICA);

    private final int ordem;
    private final NivelCriticidade criticidade;

    NivelEscalonamento(int ordem, NivelCriticidade criticidade) {
        this.ordem = ordem;
        this.criticidade = criticidade;
    }

    public int ordem() {
        return ordem;
    }

    public NivelCriticidade criticidade() {
        return criticidade;
    }
}
