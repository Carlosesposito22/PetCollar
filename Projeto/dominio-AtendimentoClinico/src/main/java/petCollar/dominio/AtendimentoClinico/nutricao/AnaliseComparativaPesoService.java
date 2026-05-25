package petCollar.dominio.AtendimentoClinico.nutricao;

public class AnaliseComparativaPesoService {

    private static final double LIMITE_DIVERGENCIA_CRITICA = 20.0;

    public AlertaManejoCritico analisarDivergencia(PesoIdeal pesoIdeal) {
        if (pesoIdeal == null)
            throw new IllegalArgumentException("Peso ideal não pode ser nulo.");

        double divergencia = pesoIdeal.getDivergenciaPercentual();

        if (divergencia > LIMITE_DIVERGENCIA_CRITICA) {
            if (pesoIdeal.getPesoAtualKg() > pesoIdeal.getPesoIdealKg()) {
                String mensagem = String.format(
                    "Paciente acima do peso ideal: %.2f kg vs %.2f kg (divergência: %.2f%%)",
                    pesoIdeal.getPesoAtualKg(),
                    pesoIdeal.getPesoIdealKg(),
                    divergencia
                );
                return AlertaManejoCritico.obesidade(mensagem);
            } else {
                String mensagem = String.format(
                    "Paciente abaixo do peso ideal: %.2f kg vs %.2f kg (divergência: %.2f%%)",
                    pesoIdeal.getPesoAtualKg(),
                    pesoIdeal.getPesoIdealKg(),
                    divergencia
                );
                return AlertaManejoCritico.caquexia(mensagem);
            }
        }

        return AlertaManejoCritico.nenhum();
    }
}
