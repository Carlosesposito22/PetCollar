package petcollar.dominio.farmacovigilancia;

import java.util.List;

/**
 * RN-142, RN-144
 * Aplica fator de redução com base nas tags clínicas do paciente.
 * Converte mg para volume em mL.
 */
public class AjusteContextualPorTagsService {

    public double calcularDoseMaximaEfetivaMgPorKg(double doseMaximaMgPorKg,
                                                    List<TipoTagClinica> tags) {
        if (tags == null)
            throw new IllegalArgumentException("tags não pode ser nula.");

        double fatorReducao = 1.0;

        for (TipoTagClinica tag : tags) {
            switch (tag) {
                case INSUFICIENCIA_RENAL:
                    fatorReducao = Math.min(fatorReducao, 0.50);
                    break;
                case INSUFICIENCIA_HEPATICA:
                    fatorReducao = Math.min(fatorReducao, 0.60);
                    break;
                case GERIATRICO:
                    fatorReducao = Math.min(fatorReducao, 0.75);
                    break;
                default:
                    break;
            }
        }

        return doseMaximaMgPorKg * fatorReducao;
    }

    public double converterMgParaMl(double doseMg, Medicamento medicamento) {
        if (medicamento == null)
            throw new IllegalArgumentException("medicamento não pode ser nulo.");
        if (doseMg <= 0)
            throw new IllegalArgumentException("doseMg deve ser positiva.");
        return doseMg / medicamento.getConcentracaoMgPorMl();
    }
}
