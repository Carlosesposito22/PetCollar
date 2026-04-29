package petcollar.dominio.farmacovigilancia;

/**
 * RN-137, RN-138, RN-139, RN-140
 * Valida a dose informada contra a dose máxima do medicamento × peso do paciente.
 */
public class CalculadoraDosagemSeguraService {

    private static final double LIMIAR_PROXIMO = 0.80;

    public StatusDosagem validar(Medicamento medicamento, double pesoKg, double dosePrescritaMg) {
        if (medicamento == null)
            throw new IllegalArgumentException("medicamento não pode ser nulo.");
        if (pesoKg <= 0)
            throw new IllegalArgumentException("pesoKg deve ser positivo.");
        if (dosePrescritaMg <= 0)
            throw new IllegalArgumentException("dosePrescritaMg deve ser positiva.");

        double doseMaxima = medicamento.getDoseMaximaSeguraMgPorKg() * pesoKg;

        if (dosePrescritaMg > doseMaxima) {
            return StatusDosagem.TRAVADO_POR_DOSE;
        } else if (dosePrescritaMg >= doseMaxima * LIMIAR_PROXIMO) {
            return StatusDosagem.PROXIMO_DO_LIMITE;
        } else {
            return StatusDosagem.DENTRO_DO_LIMITE;
        }
    }
}
