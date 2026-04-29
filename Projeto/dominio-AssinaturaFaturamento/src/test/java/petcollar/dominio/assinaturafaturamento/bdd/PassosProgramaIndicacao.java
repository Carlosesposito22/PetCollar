package petcollar.dominio.assinaturafaturamento.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import petcollar.dominio.assinaturafaturamento.CobrancaId;
import petcollar.dominio.assinaturafaturamento.StatusAssinatura;
import petcollar.dominio.assinaturafaturamento.indicacao.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PassosProgramaIndicacao {

    private final ContextoCenario contexto;

    public PassosProgramaIndicacao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ── Cenário 1: Tutor ativo gera link ────────────────────────────────────

    @Given("o Tutor com status de conta {string} esta autenticado")
    public void dadoTutorComStatusDeConta(String statusStr) {
        contexto.excecaoCapturada = null;
        contexto.linkIndicacao = null;
        when(contexto.linkIndicacaoRepositorio.findByTutorId(anyString())).thenReturn(null);
    }

    @When("o servico GeracaoLinkIndicacaoService for chamado para o Tutor com status {string}")
    public void quandoGeracaoLinkForChamado(String statusStr) {
        try {
            StatusAssinatura status = StatusAssinatura.valueOf(statusStr);
            contexto.linkIndicacao = contexto.geracaoLinkService.gerarOuObterLink(
                    "tutor-padrao", "000.000.000-00", status);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("um CodigoIndicacao alfanumerico unico deve ser gerado")
    public void entaoCodigoIndicacaoGerado() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertNotNull(contexto.linkIndicacao);
        assertNotNull(contexto.linkIndicacao.getCodigoIndicacao());
        assertFalse(contexto.linkIndicacao.getCodigoIndicacao().getValor().isBlank());
    }

    @And("a UrlCompartilhamento permanente para WhatsApp deve ser criada")
    public void eUrlCompartilhamentoCriada() {
        assertNotNull(contexto.linkIndicacao.getUrlCompartilhamento());
        assertFalse(contexto.linkIndicacao.getUrlCompartilhamento().getUrlCompleta().isBlank());
    }

    @And("o StatusLink deve ser {string}")
    public void eStatusLinkDeve(String statusEsperado) {
        assertEquals(StatusLink.valueOf(statusEsperado), contexto.linkIndicacao.getStatus());
    }

    // ── Cenário 2: Idempotência ──────────────────────────────────────────────

    @Given("o Tutor ja possui um LinkIndicacao com status {string}")
    public void dadoTutorJaPossuiLink(String statusStr) {
        contexto.excecaoCapturada = null;
        CodigoIndicacao codigo = CodigoIndicacao.gerar();
        UrlCompartilhamento url = new UrlCompartilhamento("https://petcollar.app/indicar", codigo);
        contexto.linkIndicacaoId = LinkIndicacaoId.gerar();
        contexto.linkIndicacao = new LinkIndicacao(
                contexto.linkIndicacaoId, "tutor-padrao", "000.000.000-00", codigo, url,
                StatusLink.valueOf(statusStr), 0, 0, LocalDateTime.now(), null);
        when(contexto.linkIndicacaoRepositorio.findByTutorId("tutor-padrao"))
                .thenReturn(contexto.linkIndicacao);
    }

    @When("o servico GeracaoLinkIndicacaoService for chamado novamente")
    public void quandoGeracaoLinkChamadaNovamente() {
        try {
            LinkIndicacao retornado = contexto.geracaoLinkService.gerarOuObterLink(
                    "tutor-padrao", "000.000.000-00", StatusAssinatura.ATIVA);
            contexto.linkIndicacao = retornado;
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o mesmo LinkIndicacao existente deve ser retornado sem criar um novo")
    public void entaoMesmoLinkRetornado() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertNotNull(contexto.linkIndicacao);
        assertEquals(contexto.linkIndicacaoId, contexto.linkIndicacao.getId());
        verify(contexto.linkIndicacaoRepositorio, never()).save(any());
    }

    // ── Cenário 3: Tutor inativo não pode gerar link ─────────────────────────

    @Then("o sistema deve rejeitar a operacao")
    public void entaoSistemaDeveRejeitarOperacao() {
        assertNotNull(contexto.excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, contexto.excecaoCapturada);
    }

    @And("nenhum link deve ser gerado")
    public void eNenhumLinkGerado() {
        assertNull(contexto.linkIndicacao);
        verify(contexto.linkIndicacaoRepositorio, never()).save(any());
    }

    // ── Cenário 4: Prevenção de auto-indicação ───────────────────────────────

    @Given("existe um LinkIndicacao cujo proprietario tem CPF {string}")
    public void dadoLinkComCpfProprietario(String cpfProprietario) {
        contexto.excecaoCapturada = null;
        CodigoIndicacao codigo = CodigoIndicacao.gerar();
        UrlCompartilhamento url = new UrlCompartilhamento("https://petcollar.app/indicar", codigo);
        contexto.linkIndicacaoId = LinkIndicacaoId.gerar();
        contexto.linkIndicacao = new LinkIndicacao(
                contexto.linkIndicacaoId, "tutor-indicador", cpfProprietario, codigo, url);
    }

    @Given("existe uma ConversaoIndicacao com CPF indicado {string}")
    public void dadoConversaoComCpfIndicado(String cpfIndicado) {
        LinkIndicacaoId linkId = contexto.linkIndicacaoId != null
                ? contexto.linkIndicacaoId
                : LinkIndicacaoId.gerar();
        contexto.conversaoId = ConversaoId.gerar();
        contexto.conversaoIndicacao = new ConversaoIndicacao(
                contexto.conversaoId, linkId, "tutor-indicador", cpfIndicado);
        when(contexto.conversaoRepositorio.findById(contexto.conversaoId))
                .thenReturn(contexto.conversaoIndicacao);
    }

    @When("o ValidacaoFraudeService validar auto-indicacao para o CPF do proprietario {string}")
    public void quandoValidarAutoIndicacao(String cpfIndicador) {
        try {
            contexto.validacaoFraudeService.validarAutoIndicacao(
                    contexto.conversaoIndicacao, cpfIndicador);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o StatusConversao deve ser {string}")
    public void entaoStatusConversaoDeve(String statusEsperado) {
        assertNotNull(contexto.conversaoIndicacao);
        assertEquals(StatusConversao.valueOf(statusEsperado),
                contexto.conversaoIndicacao.getStatus());
    }

    @And("um EventoRastreio {string} deve constar na trilha de auditoria")
    public void eEventoRastreioNaTrilha(String tipoEventoStr) {
        TipoEventoRastreio tipoEsperado = TipoEventoRastreio.valueOf(tipoEventoStr);
        List<EventoRastreio> trilha = contexto.conversaoIndicacao.getTrilhaAuditoria();
        assertTrue(trilha.stream().anyMatch(e -> e.getTipo() == tipoEsperado),
                "Esperado evento " + tipoEventoStr + " na trilha de auditoria.");
    }

    // ── Cenário 5: CPF já convertido anteriormente ───────────────────────────

    @Given("o CPF {string} ja foi contabilizado como indicacao confirmada")
    public void dadoCpfJaConvertido(String cpf) {
        contexto.excecaoCapturada = null;
        when(contexto.conversaoRepositorio.existsByCpfIndicadoEStatusConfirmada(cpf))
                .thenReturn(true);
    }

    @When("o ValidacaoFraudeService validar CPF ja convertido")
    public void quandoValidarCpfJaConvertido() {
        try {
            contexto.validacaoFraudeService.validarCpfJaConvertido(contexto.conversaoIndicacao);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    // ── Cenário 6: Método de pagamento duplicado ─────────────────────────────

    @Given("existe uma ConversaoIndicacao em status {string} com hashMetodo igual ao do indicador")
    public void dadoConversaoComMetodoDuplicado(String statusStr) {
        contexto.excecaoCapturada = null;
        contexto.conversaoId = ConversaoId.gerar();
        contexto.conversaoIndicacao = new ConversaoIndicacao(
                contexto.conversaoId, LinkIndicacaoId.gerar(),
                "tutor-indicador", "333.333.333-33",
                null,
                StatusConversao.valueOf(statusStr),
                List.of(),
                LocalDateTime.now().minusMinutes(10),
                null);
        when(contexto.conversaoRepositorio.findById(contexto.conversaoId))
                .thenReturn(contexto.conversaoIndicacao);
        when(contexto.conversaoRepositorio.existsByHashMetodoForTutorIndicador(
                anyString(), anyString())).thenReturn(true);
    }

    @When("o ConfirmacaoConversaoService processar o pagamento")
    public void quandoConfirmacaoProcessarPagamento() {
        try {
            contexto.indicacaoConfirmadaEvent = contexto.confirmacaoConversaoService.confirmar(
                    contexto.conversaoId,
                    new AssinaturaMetodoPagamento("hash-duplicado", "GATEWAY_X"),
                    CobrancaId.gerar(),
                    CobrancaId.gerar());
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    // ── Cenário 7: Conversão válida dispara recompensa de duas vias ──────────

    @Given("existe uma ConversaoIndicacao com status {string} sem fraude detectada")
    public void dadoConversaoSemFraude(String statusStr) {
        contexto.excecaoCapturada = null;
        contexto.conversaoId = ConversaoId.gerar();
        contexto.cobrancaId = CobrancaId.gerar();
        contexto.conversaoIndicacao = new ConversaoIndicacao(
                contexto.conversaoId, LinkIndicacaoId.gerar(),
                "tutor-indicador", "444.444.444-44",
                null,
                StatusConversao.valueOf(statusStr),
                List.of(),
                LocalDateTime.now().minusMinutes(5),
                null);
        when(contexto.conversaoRepositorio.findById(contexto.conversaoId))
                .thenReturn(contexto.conversaoIndicacao);
        when(contexto.conversaoRepositorio.existsByHashMetodoForTutorIndicador(
                anyString(), anyString())).thenReturn(false);
    }

    @When("o webhook do gateway confirmar o pagamento da primeira mensalidade do indicado")
    public void quandoWebhookConfirmarPagamento() {
        try {
            contexto.indicacaoConfirmadaEvent = contexto.confirmacaoConversaoService.confirmar(
                    contexto.conversaoId,
                    new AssinaturaMetodoPagamento("hash-novo", "GATEWAY_X"),
                    contexto.cobrancaId,
                    CobrancaId.gerar());
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o desconto de 30 por cento deve estar aplicado na Cobranca do indicado")
    public void entaoDesconto30PorCento() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        ArgumentCaptor<RecompensaIndicacao> captor =
                ArgumentCaptor.forClass(RecompensaIndicacao.class);
        verify(contexto.recompensaRepositorio, times(1)).save(captor.capture());
        contexto.recompensaIndicacao = captor.getValue();
        assertTrue(contexto.recompensaIndicacao.getDescontoIndicado().isAplicado());
        assertEquals(0.30, contexto.recompensaIndicacao.getDescontoIndicado().getPercentualDesconto());
    }

    @And("o desconto de 15 por cento deve estar aplicado na fatura do indicador")
    public void eDesconto15PorCento() {
        assertTrue(contexto.recompensaIndicacao.getDescontoIndicador().isAplicado());
        assertEquals(0.15, contexto.recompensaIndicacao.getDescontoIndicador().getPercentualDesconto());
    }

    @And("um IndicacaoConfirmadaEvent deve ser emitido")
    public void eIndicacaoConfirmadaEventEmitido() {
        assertNotNull(contexto.indicacaoConfirmadaEvent);
        assertEquals(contexto.conversaoId, contexto.indicacaoConfirmadaEvent.getConversaoId());
    }

    // ── Cenário 8: Conquista Lendária ────────────────────────────────────────

    @Given("um IndicacaoConfirmadaEvent foi emitido para o TutorIndicador")
    public void dadoIndicacaoConfirmadaEvent() {
        contexto.excecaoCapturada = null;
        contexto.conversaoId = ConversaoId.gerar();
        contexto.indicacaoConfirmadaEvent = new IndicacaoConfirmadaEvent(
                contexto.conversaoId, "tutor-indicador", "555.555.555-55");

        contexto.recompensaIndicacaoId = RecompensaIndicacaoId.gerar();
        CobrancaId cobrancaIndicado = CobrancaId.gerar();
        CobrancaId cobrancaIndicador = CobrancaId.gerar();
        contexto.recompensaIndicacao = new RecompensaIndicacao(
                contexto.recompensaIndicacaoId, contexto.conversaoId,
                "tutor-indicador", cobrancaIndicado, cobrancaIndicador);

        when(contexto.recompensaRepositorio.findByConversaoId(contexto.conversaoId))
                .thenReturn(contexto.recompensaIndicacao);
    }

    @When("o DisparoGamificacaoService processar o evento")
    public void quandoDisparoGamificacaoProcessar() {
        try {
            contexto.gamificacaoEvent = contexto.disparoGamificacaoService
                    .disparar(contexto.indicacaoConfirmadaEvent);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("um IndicacaoValidadaParaGamificacaoEvent deve ser publicado com chaveEvento {string}")
    public void entaoEventoGamificacaoPublicado(String chaveEsperada) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertNotNull(contexto.gamificacaoEvent);
        assertEquals(chaveEsperada, contexto.gamificacaoEvent.getChaveEvento());
    }

    @And("o campo gamificacaoDisparada da RecompensaIndicacao deve ser verdadeiro")
    public void eGamificacaoDisparadaVerdadeiro() {
        assertTrue(contexto.recompensaIndicacao.isGamificacaoDisparada());
    }

    // ── Cenário 9: Last Click ────────────────────────────────────────────────

    @Given("existe uma ConversaoIndicacao associada ao link do Tutor {string}")
    public void dadoConversaoAssociadaAoTutor(String tutorIdOrigem) {
        contexto.excecaoCapturada = null;
        contexto.conversaoId = ConversaoId.gerar();
        contexto.linkIndicacaoId = LinkIndicacaoId.gerar();
        contexto.conversaoIndicacao = new ConversaoIndicacao(
                contexto.conversaoId, contexto.linkIndicacaoId,
                tutorIdOrigem, "666.666.666-66");
        when(contexto.conversaoRepositorio.findById(contexto.conversaoId))
                .thenReturn(contexto.conversaoIndicacao);
    }

    @When("o AtribuicaoLastClickService atribuir o ultimo clique ao link do Tutor {string}")
    public void quandoLastClickAtribuir(String novoTutorId) {
        try {
            contexto.conversaoIndicacao = contexto.lastClickService.atribuirUltimoClique(
                    contexto.conversaoId, LinkIndicacaoId.gerar(), novoTutorId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o TutorIndicador atribuido deve ser {string}")
    public void entaoTutorIndicadorDeve(String tutorIdEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertEquals(tutorIdEsperado, contexto.conversaoIndicacao.getTutorIndicadorId());
    }

    // ── Cenário 10: Desconto não cumulativo ──────────────────────────────────

    @Given("existe uma RecompensaIndicacao com desconto de indicacao de 30 por cento")
    public void dadoRecompensaComDesconto30() {
        contexto.excecaoCapturada = null;
        contexto.conversaoId = ConversaoId.gerar();
        contexto.recompensaIndicacaoId = RecompensaIndicacaoId.gerar();
        contexto.recompensaIndicacao = new RecompensaIndicacao(
                contexto.recompensaIndicacaoId, contexto.conversaoId,
                "tutor-indicador", CobrancaId.gerar(), CobrancaId.gerar());
    }

    @Given("o indicado possui um cupom promocional de {int} por cento")
    public void dadoCupomPromocional(int percentualCupom) {
        // o percentual do cupom é passado diretamente ao @Quando
        contexto.percentualDescontoResolvido = percentualCupom / 100.0;
    }

    @When("o AplicacaoDescontoIndicadoService resolver o desconto mais vantajoso")
    public void quandoDescontoIndicadoResolver() {
        try {
            contexto.percentualDescontoResolvido = contexto.descontoIndicadoService
                    .resolver(contexto.recompensaIndicacao, contexto.percentualDescontoResolvido);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o percentual de desconto aplicado deve ser {double}")
    public void entaoPercentualDeDesconto(double percentualEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertEquals(percentualEsperado, contexto.percentualDescontoResolvido, 0.001);
    }

    @And("o desconto de indicacao deve estar marcado como aplicado")
    public void eDescontoIndicacaoMarcadoAplicado() {
        assertTrue(contexto.recompensaIndicacao.getDescontoIndicado().isAplicado());
    }
}
