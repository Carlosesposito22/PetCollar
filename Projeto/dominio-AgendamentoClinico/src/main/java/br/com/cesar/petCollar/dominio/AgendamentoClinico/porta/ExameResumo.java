package br.com.cesar.petCollar.dominio.AgendamentoClinico.porta;

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
