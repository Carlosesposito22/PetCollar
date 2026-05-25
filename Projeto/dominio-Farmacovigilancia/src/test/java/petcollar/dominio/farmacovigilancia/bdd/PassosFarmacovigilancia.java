package petcollar.dominio.farmacovigilancia.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import petcollar.dominio.farmacovigilancia.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PassosFarmacovigilancia {

    private final ContextoCenario contexto;

    public PassosFarmacovigilancia(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ── Cenários de dosagem ─────────────────────────────────────────────────

    @Given("existe um medicamento com dose maxima de {double} mg por kg")
    public void dadaMedicamentoComDoseMaxima(double doseMaxima) {
        contexto.medicamentoId = MedicamentoId.gerar();
        contexto.medicamento = new Medicamento(
                contexto.medicamentoId,
                "MedicamentoTeste",
                0.0,
                doseMaxima,
                10.0,
                RestricaoManejo.NENHUMA);
        contexto.excecaoCapturada = null;
    }

    @And("o paciente pesa {double} kg com dose prescrita de {double} mg")
    public void eoPacientePesaComDose(double pesoKg, double dosePrescritaMg) {
        contexto.pesoKg = pesoKg;
        contexto.dosePrescritaMg = dosePrescritaMg;
    }

    @When("o servico validar a dosagem")
    public void quandoServicoValidarDosagem() {
        try {
            contexto.statusDosagemResultado = contexto.calculadoraDosagem.validar(
                    contexto.medicamento,
                    contexto.pesoKg,
                    contexto.dosePrescritaMg);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o status da dosagem deve ser {string}")
    public void entaoStatusDosagemDeveSer(String statusEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(StatusDosagem.valueOf(statusEsperado), contexto.statusDosagemResultado);
    }

    // ── Cenários de interação ───────────────────────────────────────────────

    @Given("existe uma prescricao com dois medicamentos com interacao critica bloqueante")
    public void dadaPrescricaoComInteracaoBloqueante() {
        MedicamentoId idA = MedicamentoId.gerar();
        MedicamentoId idB = MedicamentoId.gerar();

        contexto.prescricaoId = PrescricaoId.gerar();
        contexto.prescricao = new Prescricao(contexto.prescricaoId);

        ItemPrescricao itemA = new ItemPrescricao(
                ItemPrescricaoId.gerar(), idA, 20.0, 7, LocalDateTime.now());
        ItemPrescricao itemB = new ItemPrescricao(
                ItemPrescricaoId.gerar(), idB, 15.0, 7, LocalDateTime.now());

        contexto.prescricao.adicionarItem(itemA);
        contexto.prescricao.adicionarItem(itemB);

        RegraInteracao regra = new RegraInteracao(idA, idB, GravidadeInteracao.CRITICA,
                "Interação crítica entre medicamentos", true);
        MatrizInteracao entrada = new MatrizInteracao(MatrizInteracaoId.gerar(), regra);

        when(contexto.matrizInteracaoRepositorio.findByMedicamento(idA))
                .thenReturn(Collections.singletonList(entrada));
        when(contexto.matrizInteracaoRepositorio.findByMedicamento(idB))
                .thenReturn(Collections.emptyList());

        contexto.excecaoCapturada = null;
    }

    @When("o servico verificar as interacoes medicamentosas")
    public void quandoServicoVerificarInteracoes() {
        try {
            contexto.interacoesDetectadas = contexto.consultaInteracoes.verificar(contexto.prescricao);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("a prescricao deve ter status {string}")
    public void entaoPrescricaoDeveTermStatus(String statusEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(StatusPrescricao.valueOf(statusEsperado), contexto.prescricao.getStatus());
    }

    @And("o motivo deve indicar interacao bloqueante")
    public void eMotivoDeveIndicarInteracaoBloqueante() {
        assertNotNull(contexto.interacoesDetectadas);
        assertTrue(contexto.interacoesDetectadas.stream().anyMatch(RegraInteracao::isBloqueante));
    }

    // ── Cenários de ajuste contextual por tags ──────────────────────────────

    @Given("existe um paciente com tag {string}")
    public void dadaPacienteComTag(String tagStr) {
        contexto.tagClinicaSelecionada = TipoTagClinica.valueOf(tagStr);
        contexto.excecaoCapturada = null;
    }

    @Given("um medicamento com dose maxima de {double} mg por kg")
    public void dadaMedicamentoComDoseMaximaSimples(double doseMaxima) {
        contexto.medicamento = new Medicamento(
                MedicamentoId.gerar(), "MedicamentoComTag", 0.0, doseMaxima, 1.0, RestricaoManejo.NENHUMA);
    }

    @When("o servico calcular o fator de reducao contextual")
    public void quandoServicoCalcularFatorReducao() {
        try {
            List<TipoTagClinica> tags = Arrays.asList(contexto.tagClinicaSelecionada);
            contexto.doseMaximaEfetiva = contexto.ajusteContextual
                    .calcularDoseMaximaEfetivaMgPorKg(
                            contexto.medicamento.getDoseMaximaSeguraMgPorKg(), tags);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("a dose maxima efetiva deve ser {double} mg por kg")
    public void entaoDoseMaximaEfetivaDeveSer(double esperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(esperado, contexto.doseMaximaEfetiva, 0.001);
    }

    // ── Cenários de cronograma ──────────────────────────────────────────────

    @Given("existe uma prescricao com item A de duracao {int} dias e item B de duracao {int} dias")
    public void dadaPrescricaoComDoisItens(int duracaoA, int duracaoB) {
        contexto.prescricaoId = PrescricaoId.gerar();
        contexto.prescricao = new Prescricao(contexto.prescricaoId);

        MedicamentoId idMed = MedicamentoId.gerar();
        ItemPrescricao itemA = new ItemPrescricao(
                ItemPrescricaoId.gerar(), idMed, 10.0, duracaoA, LocalDateTime.now());
        ItemPrescricao itemB = new ItemPrescricao(
                ItemPrescricaoId.gerar(), idMed, 10.0, duracaoB, LocalDateTime.now());

        contexto.prescricao.adicionarItem(itemA);
        contexto.prescricao.adicionarItem(itemB);
        contexto.excecaoCapturada = null;
    }

    @And("ambos os itens foram iniciados hoje")
    public void eAmbosIniciadosHoje() {
        // Os itens já foram criados com LocalDateTime.now() no passo anterior
    }

    @When("o servico calcular a data de fim do tratamento")
    public void quandoServicoCalcularDataFim() {
        try {
            contexto.dataFimTratamento = contexto.cronogramaService
                    .calcularDataFimTratamento(contexto.prescricao);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("a data de fim deve ser daqui a {int} dias")
    public void entaoDataFimDeveSerDaquiADias(int dias) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        LocalDate esperado = LocalDate.now().plusDays(dias);
        assertEquals(esperado, contexto.dataFimTratamento);
    }

    // ── Cenários de emissão ─────────────────────────────────────────────────

    @Given("existe uma prescricao com todos os itens com status {string}")
    public void dadaPrescricaoComItensComStatus(String statusStr) {
        contexto.prescricaoId = PrescricaoId.gerar();
        StatusDosagem statusDosagem = StatusDosagem.valueOf(statusStr);

        ItemPrescricao item = new ItemPrescricao(
                ItemPrescricaoId.gerar(),
                MedicamentoId.gerar(),
                10.0,
                7,
                LocalDateTime.now(),
                statusDosagem,
                LocalDateTime.now());

        contexto.prescricao = new Prescricao(
                contexto.prescricaoId,
                Collections.singletonList(item),
                StatusPrescricao.RASCUNHO,
                null,
                LocalDateTime.now(),
                null);

        when(contexto.prescricaoRepositorio.findById(contexto.prescricaoId))
                .thenReturn(contexto.prescricao);
        contexto.excecaoCapturada = null;
    }

    @When("o servico emitir a prescricao")
    public void quandoServicoEmitirPrescricao() {
        try {
            contexto.prescricao = contexto.emissaoService.emitir(contexto.prescricaoId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o status da prescricao deve ser {string}")
    public void entaoStatusPrescricaoDeveSer(String statusEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(StatusPrescricao.valueOf(statusEsperado), contexto.prescricao.getStatus());
    }

    @Given("existe uma prescricao com um item com status {string}")
    public void dadaPrescricaoComItemTravado(String statusStr) {
        contexto.prescricaoId = PrescricaoId.gerar();
        StatusDosagem statusDosagem = StatusDosagem.valueOf(statusStr);

        ItemPrescricao item = new ItemPrescricao(
                ItemPrescricaoId.gerar(),
                MedicamentoId.gerar(),
                10.0,
                7,
                LocalDateTime.now(),
                statusDosagem,
                LocalDateTime.now());

        contexto.prescricao = new Prescricao(
                contexto.prescricaoId,
                Collections.singletonList(item),
                StatusPrescricao.RASCUNHO,
                null,
                LocalDateTime.now(),
                null);

        when(contexto.prescricaoRepositorio.findById(contexto.prescricaoId))
                .thenReturn(contexto.prescricao);
        contexto.excecaoCapturada = null;
    }

    @When("o servico tentar emitir a prescricao")
    public void quandoServicoTentarEmitirPrescricao() {
        try {
            contexto.emissaoService.emitir(contexto.prescricaoId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("deve ser lancada uma excecao de estado invalido")
    public void entaoExcecaoEstadoInvalido() {
        assertNotNull(contexto.excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, contexto.excecaoCapturada);
    }
}
