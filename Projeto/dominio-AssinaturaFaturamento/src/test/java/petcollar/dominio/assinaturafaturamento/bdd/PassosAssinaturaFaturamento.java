package petcollar.dominio.assinaturafaturamento.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import petcollar.dominio.assinaturafaturamento.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PassosAssinaturaFaturamento {

    private final ContextoCenario contexto;

    public PassosAssinaturaFaturamento(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ── Contratação ─────────────────────────────────────────────────────────

    @Given("existe um plano ativo disponivel para contratacao")
    public void dadaPlanoAtivoDisponivel() {
        contexto.planoId = PlanoId.gerar();
        contexto.plano = new Plano(
                contexto.planoId,
                "Plano Básico",
                Dinheiro.emReais(99.90)
        );
        when(contexto.planoRepositorio.findById(contexto.planoId))
                .thenReturn(contexto.plano);
        contexto.excecaoCapturada = null;
    }

    @Given("existe um plano desativado")
    public void dadaPlanoDesativado() {
        contexto.planoId = PlanoId.gerar();
        contexto.plano = new Plano(
                contexto.planoId,
                "Plano Desativado",
                Dinheiro.emReais(99.90),
                false,
                LocalDateTime.now().minusDays(30)
        );
        when(contexto.planoRepositorio.findById(contexto.planoId))
                .thenReturn(contexto.plano);
        contexto.excecaoCapturada = null;
    }

    @When("o servico contratar o plano para o tutor {string}")
    public void quandoServicoContratar(String tutorId) {
        try {
            contexto.assinatura = contexto.contratacaoPlanoService
                    .contratar(contexto.planoId, tutorId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico tentar contratar o plano para o tutor {string}")
    public void quandoServicoTentarContratar(String tutorId) {
        try {
            contexto.assinatura = contexto.contratacaoPlanoService
                    .contratar(contexto.planoId, tutorId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("a assinatura deve ter status {string}")
    public void entaoVerificaStatusAssinatura(String statusEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(
                StatusAssinatura.valueOf(statusEsperado),
                contexto.assinatura.getStatus()
        );
    }

    @And("a primeira cobranca deve ter sido salva no repositorio")
    public void eVerificaSaveCobranca() {
        verify(contexto.cobrancaRepositorio, times(1)).save(any(Cobranca.class));
    }

    // ── Confirmação de pagamento ─────────────────────────────────────────────

    @Given("existe uma assinatura com status {string}")
    public void dadaAssinaturaComStatus(String statusStr) {
        contexto.assinaturaId = AssinaturaId.gerar();
        contexto.planoId = PlanoId.gerar();
        StatusAssinatura status = StatusAssinatura.valueOf(statusStr);
        contexto.assinatura = new Assinatura(
                contexto.assinaturaId,
                "tutor-001",
                contexto.planoId,
                status,
                0,
                LocalDateTime.now().minusDays(1),
                null
        );
        when(contexto.assinaturaRepositorio.findById(contexto.assinaturaId))
                .thenReturn(contexto.assinatura);
        contexto.excecaoCapturada = null;
    }

    @When("o servico confirmar o primeiro pagamento da assinatura")
    public void quandoServicoConfirmarPrimeiroPagamento() {
        try {
            contexto.assinatura = contexto.contratacaoPlanoService
                    .confirmarPrimeiroPagamento(contexto.assinaturaId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    // ── Cálculo de juros ────────────────────────────────────────────────────

    @Given("existe uma cobranca com valor original de {double} reais")
    public void dadaCobrancaComValorOriginal(double valor) {
        contexto.valorOriginalCobranca = valor;
        contexto.excecaoCapturada = null;
    }

    @When("o servico calcular o valor atualizado com {int} meses de atraso")
    public void quandoServicoCalcularJuros(int meses) {
        try {
            contexto.valorCalculado = contexto.calculoJurosService
                    .calcularValorAtualizado(Dinheiro.emReais(contexto.valorOriginalCobranca), meses);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o valor com juros deve ser {double} reais")
    public void entaoVerificaValorComJuros(double valorEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(valorEsperado, contexto.valorCalculado.getValor(), 0.001);
    }

    // ── Gestão de inadimplência ──────────────────────────────────────────────

    @Given("existe uma assinatura com {int} mensalidades consecutivas em atraso")
    public void dadaAssinaturaComMensalidadesEmAtraso(int quantidade) {
        contexto.assinaturaId = AssinaturaId.gerar();
        contexto.planoId = PlanoId.gerar();
        contexto.assinatura = new Assinatura(
                contexto.assinaturaId,
                "tutor-001",
                contexto.planoId,
                StatusAssinatura.ATIVA,
                quantidade,
                LocalDateTime.now().minusMonths(quantidade),
                LocalDateTime.now()
        );
        when(contexto.assinaturaRepositorio.findById(contexto.assinaturaId))
                .thenReturn(contexto.assinatura);
        contexto.excecaoCapturada = null;
    }

    @When("o servico avaliar o status da assinatura")
    public void quandoServicoAvaliarStatus() {
        try {
            contexto.assinatura = contexto.gestaoInadimplenciaService
                    .avaliarStatusAssinatura(contexto.assinaturaId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Given("existe uma assinatura com status {string} e {int} cobrancas em atraso")
    public void dadaAssinaturaInadimplenteComCobrancas(String statusStr, int qtdCobrancas) {
        contexto.assinaturaId = AssinaturaId.gerar();
        contexto.planoId = PlanoId.gerar();
        StatusAssinatura status = StatusAssinatura.valueOf(statusStr);
        contexto.assinatura = new Assinatura(
                contexto.assinaturaId,
                "tutor-001",
                contexto.planoId,
                status,
                qtdCobrancas,
                LocalDateTime.now().minusMonths(qtdCobrancas),
                LocalDateTime.now()
        );
        when(contexto.assinaturaRepositorio.findById(contexto.assinaturaId))
                .thenReturn(contexto.assinatura);

        List<Cobranca> cobrancasEmAtraso = new ArrayList<>();
        for (int i = 0; i < qtdCobrancas; i++) {
            CobrancaId cobrancaId = CobrancaId.gerar();
            Cobranca cobranca = new Cobranca(
                    cobrancaId,
                    contexto.assinaturaId,
                    Dinheiro.emReais(99.90),
                    LocalDate.now().minusMonths(qtdCobrancas - i),
                    StatusCobranca.EM_ATRASO,
                    LocalDateTime.now().minusMonths(qtdCobrancas - i),
                    LocalDateTime.now()
            );
            cobrancasEmAtraso.add(cobranca);
            when(contexto.cobrancaRepositorio.findById(cobrancaId)).thenReturn(cobranca);
        }
        contexto.cobrancasEmAtraso = cobrancasEmAtraso;
        when(contexto.cobrancaRepositorio.findByAssinaturaIdEStatus(
                contexto.assinaturaId, StatusCobranca.EM_ATRASO))
                .thenReturn(cobrancasEmAtraso);

        contexto.excecaoCapturada = null;
    }

    @When("as cobrancas forem quitadas")
    public void quandoCobrancasForemQuitadas() {
        try {
            contexto.assinatura = contexto.gestaoInadimplenciaService
                    .registrarQuitacao(contexto.assinaturaId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o contador de mensalidades em atraso deve ser zero")
    public void entaoContadorDeveSerZero() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(0, contexto.assinatura.getMensalidadesConsecutivasEmAtraso());
    }

    // ── Geração de QR Code ───────────────────────────────────────────────────

    @Given("existe uma cobranca em atraso da assinatura")
    public void dadaCobrancaEmAtraso() {
        contexto.assinaturaId = AssinaturaId.gerar();
        contexto.cobrancaId = CobrancaId.gerar();
        contexto.cobranca = new Cobranca(
                contexto.cobrancaId,
                contexto.assinaturaId,
                Dinheiro.emReais(99.90),
                LocalDate.now().minusMonths(1),
                StatusCobranca.EM_ATRASO,
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now()
        );
        when(contexto.cobrancaRepositorio.findById(contexto.cobrancaId))
                .thenReturn(contexto.cobranca);
        contexto.excecaoCapturada = null;
    }

    @When("o servico gerar o QR Code para a cobranca")
    public void quandoServicoGerarQRCode() {
        try {
            contexto.pagamento = contexto.geracaoQRCodeService
                    .gerarQRCodeParaCobranca(contexto.cobrancaId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("um pagamento com QR Code deve ser criado com status {string}")
    public void entaoVerificaPagamentoStatus(String statusEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.pagamento);
        assertEquals(StatusPagamento.valueOf(statusEsperado), contexto.pagamento.getStatus());
        assertNotNull(contexto.pagamento.getQrCode());
        verify(contexto.pagamentoRepositorio, times(1)).save(contexto.pagamento);
    }

    @Given("existe uma assinatura suspensa com {int} cobrancas em atraso")
    public void dadaAssinaturaSuspensaComCobrancas(int qtdCobrancas) {
        contexto.assinaturaId = AssinaturaId.gerar();
        List<Cobranca> cobrancas = new ArrayList<>();
        for (int i = 0; i < qtdCobrancas; i++) {
            Cobranca cobranca = new Cobranca(
                    CobrancaId.gerar(),
                    contexto.assinaturaId,
                    Dinheiro.emReais(99.90),
                    LocalDate.now().minusMonths(qtdCobrancas - i),
                    StatusCobranca.EM_ATRASO,
                    LocalDateTime.now().minusMonths(qtdCobrancas - i),
                    LocalDateTime.now()
            );
            cobrancas.add(cobranca);
        }
        contexto.cobrancasEmAtraso = cobrancas;
        contexto.excecaoCapturada = null;
    }

    @When("o servico gerar o QR Code consolidado para a assinatura")
    public void quandoServicoGerarQRCodeConsolidado() {
        try {
            contexto.pagamento = contexto.geracaoQRCodeService
                    .gerarQRCodeConsolidado(contexto.assinaturaId, contexto.cobrancasEmAtraso);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("um pagamento consolidado deve ser criado com status {string}")
    public void entaoVerificaPagamentoConsolidadoStatus(String statusEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.pagamento);
        assertEquals(StatusPagamento.valueOf(statusEsperado), contexto.pagamento.getStatus());
        assertNotNull(contexto.pagamento.getQrCode());
    }

    @And("o valor do pagamento deve corresponder a soma das cobrancas")
    public void eVerificaValorConsolidado() {
        double somaEsperada = contexto.cobrancasEmAtraso.stream()
                .mapToDouble(c -> c.getValorOriginal().getValor())
                .sum();
        assertEquals(somaEsperada, contexto.pagamento.getValor().getValor(), 0.001);
        verify(contexto.pagamentoRepositorio, times(1)).save(contexto.pagamento);
    }

    @Given("nao existe cobranca com o id informado")
    public void dadaNaoExisteCobranca() {
        contexto.cobrancaId = CobrancaId.gerar();
        when(contexto.cobrancaRepositorio.findById(contexto.cobrancaId)).thenReturn(null);
        contexto.excecaoCapturada = null;
    }

    @When("o servico tentar gerar o QR Code para essa cobranca")
    public void quandoServicoTentarGerarQRCode() {
        try {
            contexto.pagamento = contexto.geracaoQRCodeService
                    .gerarQRCodeParaCobranca(contexto.cobrancaId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("deve ser lancada uma excecao de argumento invalido")
    public void entaoExcecaoArgumentoInvalido() {
        assertNotNull(contexto.excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, contexto.excecaoCapturada);
    }
}
