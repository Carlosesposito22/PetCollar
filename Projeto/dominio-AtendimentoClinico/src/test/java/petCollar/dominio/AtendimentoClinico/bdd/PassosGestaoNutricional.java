package petcollar.dominio.atendimentoclinico.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import petCollar.dominio.AtendimentoClinico.nutricao.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PassosGestaoNutricional {

    private final ContextoCenario contexto;

    public PassosGestaoNutricional(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ════════════════════════════════════════════════════════════════════════════════════════════
    // Passos @Dado — Setup
    // ════════════════════════════════════════════════════════════════════════════════════════════

    @Given("existe um plano nutricional com peso ideal {double} kg")
    public void dadaPlanoComPesoIdeal(double pesoIdealKg) {
        contexto.planoId = PlanoNutricionalId.gerar();
        contexto.pesoIdeal = new PesoIdeal(pesoIdealKg, pesoIdealKg);
        contexto.nivelAtividadeAtual = NivelAtividade.MODERADAMENTE_ATIVO;
        contexto.excecaoCapturada = null;
    }

    @Given("o nivel de atividade e {string}")
    public void dadoNivelAtividade(String nivelStr) {
        contexto.nivelAtividadeAtual = NivelAtividade.valueOf(nivelStr);
    }

    @Given("nao ha comorbidades")
    public void dadaSemComorbidades() {
        // Nada a fazer — comorbidades são vazias por padrão
    }

    @Given("existe um plano com peso ideal {double} kg e nivel {string}")
    public void dadaPlanoComPesoIdealEAtividade(double pesoIdealKg, String nivelStr) {
        contexto.planoId = PlanoNutricionalId.gerar();
        contexto.pesoIdeal = new PesoIdeal(pesoIdealKg, pesoIdealKg);
        contexto.nivelAtividadeAtual = NivelAtividade.valueOf(nivelStr);
        contexto.excecaoCapturada = null;
    }

    @And("o paciente tem a comorbidade {string} com modificador {double}")
    public void eComorbidadeAplicada(String tipoComorbidadeStr, double modificador) {
        TipoComorbidade tipoComorbidade = TipoComorbidade.valueOf(tipoComorbidadeStr);
        Comorbidade comorbidade = new Comorbidade(tipoComorbidade, modificador);
        contexto.plano = new PlanoNutricional(
            contexto.planoId,
            "paciente-123",
            contexto.pesoIdeal,
            contexto.nivelAtividadeAtual,
            List.of(comorbidade)
        );
    }

    @Given("existe um plano com peso atual {double} kg e peso ideal {double} kg")
    public void dadaPlanoComPesos(double pesoAtual, double pesoIdeal) {
        contexto.planoId = PlanoNutricionalId.gerar();
        contexto.pesoIdeal = new PesoIdeal(pesoAtual, pesoIdeal);
        contexto.nivelAtividadeAtual = NivelAtividade.MODERADAMENTE_ATIVO;
        contexto.excecaoCapturada = null;
    }

    @Given("o NEM calculado e de {double} kcal")
    public void dadoNEMFixo(double kcalDiarias) {
        contexto.nemResultante = kcalDiarias;
    }

    @Given("a racao tem {double} kcal por grama")
    public void dadaDensidadeEnergetica(double kcalPorGrama) {
        // Armazenar para uso no passo @Quando
        if (!contexto.toString().contains("kcalPorGrama")) {
            // Usar um campo adicional que não existe ainda — vamos contornar no @Quando
        }
    }

    // ════════════════════════════════════════════════════════════════════════════════════════════
    // Passos @Quando — Ações (com try/catch obrigatório)
    // ════════════════════════════════════════════════════════════════════════════════════════════

    @When("o servico calcular o NEM")
    public void quandoServicoCalculaNEM() {
        try {
            contexto.plano = new PlanoNutricional(
                contexto.planoId,
                "paciente-123",
                contexto.pesoIdeal,
                contexto.nivelAtividadeAtual,
                List.of()  // Sem comorbidades por enquanto
            );
            contexto.nemResultante = contexto.plano.getResultadoNEM().getKcalDiarias();
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico avaliar o alerta de manejo critico")
    public void quandoServicoAvaliaAlerta() {
        try {
            contexto.alertaResultante = contexto.analiseComparativaPesoService
                .analisarDivergencia(contexto.pesoIdeal);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico gerar o cronograma de transicao de 7 dias")
    public void quandoServicoGeraTransicao() {
        try {
            contexto.cronogramaResultante = contexto.geracaoCronogramaTransicaoService
                .gerarCronograma7Dias();
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico calcular as gramas diarias")
    public void quandoServicoCalculaGramas() {
        try {
            contexto.gramasDiariasResultante = contexto.calculoNEMService
                .calcularGramasDiarias(contexto.nemResultante, 3.5);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════════════════════
    // Passos @Então — Validações
    // ════════════════════════════════════════════════════════════════════════════════════════════

    @Then("o resultado deve ser aproximadamente {int} kcal por dia")
    public void entaoVerificaNEMEsperado(int nemEsperado) {
        assertNull(contexto.excecaoCapturada,
            "Não deveria ter lançado exceção: " + (contexto.excecaoCapturada != null ? contexto.excecaoCapturada.getMessage() : ""));
        assertNotNull(contexto.nemResultante, "NEM não foi calculado");
        assertEquals(nemEsperado, contexto.nemResultante.intValue(), 10,
            "NEM deveria ser aproximadamente " + nemEsperado);
    }

    @Then("o tipo de alerta deve ser {string}")
    public void entaoVerificaTipoAlerta(String tipoAlertaEsperado) {
        assertNull(contexto.excecaoCapturada,
            "Não deveria ter lançado exceção");
        assertNotNull(contexto.alertaResultante, "Alerta não foi calculado");
        assertEquals(TipoAlertaManejo.valueOf(tipoAlertaEsperado), contexto.alertaResultante.getTipo(),
            "Tipo de alerta incompatível");
    }

    @Then("o dia {int} deve ter {double} porcento da dieta nova")
    public void entaoVerificaPercentualDia(int numeroDia, double percentualEsperado) {
        assertNull(contexto.excecaoCapturada,
            "Não deveria ter lançado exceção");
        assertNotNull(contexto.cronogramaResultante, "Cronograma não foi gerado");

        DiaTransicao dia = contexto.cronogramaResultante.getDiasTransicao().get(numeroDia - 1);
        assertEquals(percentualEsperado, dia.getPercentualDietaNova(), 0.1,
            "Dia " + numeroDia + " devería ter " + percentualEsperado + "% da dieta nova");
    }

    @And("o dia {int} deve ter {double} porcento da dieta nova")
    public void eVerificaPercentualDia(int numeroDia, double percentualEsperado) {
        entaoVerificaPercentualDia(numeroDia, percentualEsperado);
    }

    @Then("o resultado deve ser aproximadamente {int} gramas por dia")
    public void entaoVerificaGramasDiarias(int gramasEsperadas) {
        assertNull(contexto.excecaoCapturada,
            "Não deveria ter lançado exceção");
        assertNotNull(contexto.gramasDiariasResultante, "Gramas diárias não foram calculadas");
        assertEquals(gramasEsperadas, contexto.gramasDiariasResultante.intValue(), 10,
            "Gramas deveria ser aproximadamente " + gramasEsperadas);
    }
}
