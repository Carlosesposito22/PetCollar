package br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao;

/**
 * Horário do dia (formato HH:mm) em que uma dose do item da prescrição deve
 * ser administrada. Validado para garantir o formato esperado.
 */
public record HorarioAdministracao(String valor) {

    public HorarioAdministracao {
        if (valor == null || !valor.matches("^([01]\\d|2[0-3]):[0-5]\\d$"))
            throw new IllegalArgumentException(
                    "Horário inválido — use HH:mm (ex.: 08:00, 14:30). Valor recebido: " + valor);
    }
}
