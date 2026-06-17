package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato;

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
