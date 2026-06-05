package br.com.cesar.petCollar.dominio.AgendamentoClinico.porta;

/**
 * Read-model de um exame vinculado a uma consulta de origem, exposto pela porta
 * {@link IConsultaExame}. O identificador é a String do Id do exame (referência
 * entre agregados/contextos — nunca o agregado Exame em si).
 */
public record ExameResumo(String exameId, String descricao, StatusExame status) {

    public ExameResumo {
        if (exameId == null || exameId.isBlank())
            throw new IllegalArgumentException("Id do exame não pode ser vazio.");
        if (status == null)
            throw new IllegalArgumentException("Status do exame não pode ser nulo.");
    }

    public boolean estaConcluido() {
        return status == StatusExame.CONCLUIDO;
    }
}
