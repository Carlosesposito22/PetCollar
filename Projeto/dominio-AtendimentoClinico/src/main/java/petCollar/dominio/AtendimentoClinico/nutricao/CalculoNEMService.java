package petCollar.dominio.AtendimentoClinico.nutricao;
import java.util.List;

public class CalculoNEMService {

    private static final double CONSTANTE_NEM_BASE = 70.0;
    private static final double EXPOENTE_PESO = 0.75;

    public double calcularNEMBase(double pesoIdealKg) {
        if (pesoIdealKg <= 0)
            throw new IllegalArgumentException("Peso ideal deve ser maior que zero.");
        return CONSTANTE_NEM_BASE * Math.pow(pesoIdealKg, EXPOENTE_PESO);
    }

    public double obterFatorAtividade(NivelAtividade nivelAtividade) {
        if (nivelAtividade == null)
            throw new IllegalArgumentException("Nível de atividade não pode ser nulo.");
        return nivelAtividade.getFator();
    }

    public double calcularModificadorComorbidades(List<Comorbidade> comorbidades) {
        if (comorbidades == null || comorbidades.isEmpty())
            return 1.0;

        double produto = 1.0;
        for (Comorbidade comorbidade : comorbidades) {
            produto *= comorbidade.getModificadorMetabolico();
        }
        return produto;
    }

    public double calcularNEMTotal(PesoIdeal pesoIdeal, NivelAtividade nivelAtividade,
                                    List<Comorbidade> comorbidades) {
        if (pesoIdeal == null)
            throw new IllegalArgumentException("Peso ideal não pode ser nulo.");
        if (nivelAtividade == null)
            throw new IllegalArgumentException("Nível de atividade não pode ser nulo.");

        double nemBase = calcularNEMBase(pesoIdeal.getPesoIdealKg());
        double fatorAtividade = obterFatorAtividade(nivelAtividade);
        double modificadorComorbidades = calcularModificadorComorbidades(comorbidades);

        return nemBase * fatorAtividade * modificadorComorbidades;
    }

    public double calcularGramasDiarias(double kcalDiarias, double kcalPorGrama) {
        if (kcalDiarias < 0)
            throw new IllegalArgumentException("Kcal diárias não podem ser negativas.");
        if (kcalPorGrama <= 0)
            throw new IllegalArgumentException("Kcal por grama deve ser maior que zero.");

        return kcalDiarias / kcalPorGrama;
    }
}
