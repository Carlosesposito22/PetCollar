package br.com.cesar.petCollar.dominio.ProtocoloInacessibilidade.contato;

public enum CanalContato {

    TELEFONE(1, 10),
    WHATSAPP(2, 15),
    SMS(3, 30),
    EMAIL(4, 60);

    private final int prioridadeBase;
    private final int tempoEsperaRespostaMinutos;

    CanalContato(int prioridadeBase, int tempoEsperaRespostaMinutos) {
        this.prioridadeBase = prioridadeBase;
        this.tempoEsperaRespostaMinutos = tempoEsperaRespostaMinutos;
    }

    public int prioridadeBase() {
        return prioridadeBase;
    }

    public int tempoEsperaRespostaMinutos() {
        return tempoEsperaRespostaMinutos;
    }
}
