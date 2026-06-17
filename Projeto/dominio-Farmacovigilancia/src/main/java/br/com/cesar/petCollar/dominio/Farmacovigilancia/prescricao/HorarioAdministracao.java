package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

public record HorarioAdministracao(String valor) {

    public HorarioAdministracao {
        if (valor == null || !valor.matches("^([01]\\d|2[0-3]):[0-5]\\d$"))
            throw new IllegalArgumentException(
                    "Horário inválido — use HH:mm (ex.: 08:00, 14:30). Valor recebido: " + valor);
    }
}
