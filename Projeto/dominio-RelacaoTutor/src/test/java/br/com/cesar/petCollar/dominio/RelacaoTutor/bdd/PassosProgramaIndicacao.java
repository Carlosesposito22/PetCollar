package br.com.cesar.petCollar.dominio.RelacaoTutor.bdd;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CPF;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CodigoIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.Indicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.RegistroClique;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.RegistroCliqueId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProgramaIndicacaoService;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.StatusIndicacao;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PassosProgramaIndicacao {

    private final ContextoCenario ctx;

    public PassosProgramaIndicacao(ContextoCenario ctx) {
        this.ctx = ctx;
    }

    @Dado("um Tutor com conta ativa")
    public void dadoTutorComContaAtiva() {
        ctx.tutorId = TutorId.gerar();
        ctx.contaAtiva = true;
        when(ctx.linkRepositorio.buscarPorTutorId(ctx.tutorId)).thenReturn(Optional.empty());
        when(ctx.linkRepositorio.existePorCodigo(any())).thenReturn(false);
        ctx.excecaoCapturada = null;
    }

    @Dado("um Tutor com conta inativa")
    public void dadoTutorComContaInativa() {
        ctx.tutorId = TutorId.gerar();
        ctx.contaAtiva = false;
        ctx.excecaoCapturada = null;
    }

    @Dado("o Tutor ja possui um link de indicacao cadastrado")
    public void dadoTutorJaPossuiLink() {
        ctx.linkPreExistente = new LinkIndicacao(
            LinkIndicacaoId.gerar(), ctx.tutorId, CodigoIndicacao.de("EXISTNT1"));
        when(ctx.linkRepositorio.buscarPorTutorId(ctx.tutorId))
            .thenReturn(Optional.of(ctx.linkPreExistente));
    }

    @Quando("o Tutor solicitar o link de indicacao")
    public void quandoSolicitarLink() {
        try {
            ctx.linkRetornado = ctx.servico.obterOuGerarLink(ctx.tutorId, ctx.contaAtiva);
        } catch (Exception e) {
            ctx.excecaoCapturada = e;
        }
    }

    @Entao("o link de indicacao deve ser gerado com sucesso")
    public void entaoLinkGeradoComSucesso() {
        assertNull(ctx.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(ctx.linkRetornado);
        verify(ctx.linkRepositorio).salvar(any(LinkIndicacao.class));
    }

    @E("o link deve conter um codigo alfanumerico de 8 caracteres")
    public void eCodigoAlfanumerico8Chars() {
        String codigo = ctx.linkRetornado.getCodigo().getValor();
        assertTrue(codigo.matches("[A-Z0-9]{8}"),
            "Código deve ter 8 caracteres alfanuméricos maiúsculos, mas foi: " + codigo);
    }

    @Entao("o link retornado deve ser o mesmo link ja existente")
    public void entaoMesmoLink() {
        assertNull(ctx.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(ctx.linkPreExistente.getId(), ctx.linkRetornado.getId());
        verify(ctx.linkRepositorio, never()).salvar(any());
    }

    @Dado("um Tutor com CPF {string}")
    public void dadoTutorComCpf(String cpf) {
        ctx.tutorId = TutorId.gerar();
        ctx.contaAtiva = true;
        ctx.excecaoCapturada = null;
    }

    @E("um indicado com o mesmo CPF {string}")
    public void eIndicadoComMesmoCpf(String cpf) {

    }

    @Quando("o indicado tentar se inscrever como indicacao do proprio Tutor")
    public void quandoTentarAutoIndicacao() {
        CPF cpfIgual = CPF.de("12345678901");
        try {
            ctx.indicacaoCriada = ctx.servico.criarIndicacaoParaInscrito(cpfIgual, cpfIgual);
        } catch (Exception e) {
            ctx.excecaoCapturada = e;
        }
    }

    @E("um indicado com CPF {string} que ja possui conversao registrada")
    public void eIndicadoCpfJaConvertido(String cpf) {
        when(ctx.indicacaoRepositorio.existeConversaoPorCpf(CPF.de(cpf))).thenReturn(true);
        ctx.excecaoCapturada = null;
    }

    @Quando("o indicado tentar se inscrever como indicacao")
    public void quandoTentarInscricaoComCpfJaConvertido() {
        CPF cpfIndicado  = CPF.de("98765432100");
        CPF cpfIndicador = CPF.de("11111111111");
        try {
            ctx.indicacaoCriada = ctx.servico.criarIndicacaoParaInscrito(cpfIndicado, cpfIndicador);
        } catch (Exception e) {
            ctx.excecaoCapturada = e;
        }
    }

    @Dado("um Tutor A com codigo de link {string}")
    public void dadoTutorAComCodigo(String codigo) {
        ctx.tutorIdA = TutorId.gerar();
        ctx.excecaoCapturada = null;
    }

    @E("um Tutor B com codigo de link {string}")
    public void eTutorBComCodigo(String codigo) {
        ctx.tutorIdB = TutorId.gerar();
    }

    @E("o indicado com CPF {string} clicou primeiro no link do Tutor A")
    public void eIndicadoClicouPrimeiroNoLinkA(String cpf) {

    }

    @E("o indicado com CPF {string} clicou depois no link do Tutor B")
    public void eIndicadoClicoouDepoisNoLinkB(String cpf) {
        CPF cpfIndicado = CPF.de(cpf);

        LinkIndicacaoId linkBId = LinkIndicacaoId.gerar();
        RegistroClique ultimoClique = new RegistroClique(
            RegistroCliqueId.gerar(),
            cpfIndicado,
            linkBId,
            ctx.tutorIdB,
            LocalDateTime.now()
        );
        when(ctx.registroCliqueRepositorio.buscarUltimoPorCpf(cpfIndicado))
            .thenReturn(Optional.of(ultimoClique));
        when(ctx.indicacaoRepositorio.existeConversaoPorCpf(cpfIndicado)).thenReturn(false);
    }

    @Quando("o indicado se inscrever na plataforma")
    public void quandoIndicadoSeInscrever() {
        CPF cpfIndicado  = CPF.de("11122233344");
        CPF cpfIndicador = CPF.de("99988877766");
        try {
            ctx.indicacaoCriada = ctx.servico.criarIndicacaoParaInscrito(cpfIndicado, cpfIndicador);
        } catch (Exception e) {
            ctx.excecaoCapturada = e;
        }
    }

    @Entao("a indicacao deve ser atribuida ao Tutor B")
    public void entaoIndicacaoAtribuidaAoTutorB() {
        assertNull(ctx.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(ctx.indicacaoCriada);
        assertEquals(ctx.tutorIdB, ctx.indicacaoCriada.getTutorIndicadorId());
    }

    @Dado("um Tutor indicador com conta ativa")
    public void dadoTutorIndicadorComContaAtiva() {
        ctx.tutorId = TutorId.gerar();
        ctx.contaAtiva = true;
        ctx.excecaoCapturada = null;
    }

    @E("uma indicacao pendente para o indicado com CPF {string}")
    public void eIndicacaoPendente(String cpf) {
        CPF cpfIndicado = CPF.de(cpf);
        IndicacaoId indicacaoId = IndicacaoId.gerar();
        Indicacao indicacaoPendente = new Indicacao(
            indicacaoId,
            ctx.tutorId,
            LinkIndicacaoId.gerar(),
            cpfIndicado,
            LocalDateTime.now().minusDays(1)
        );
        when(ctx.indicacaoRepositorio.buscarPorId(indicacaoId))
            .thenReturn(Optional.of(indicacaoPendente));
        when(ctx.descontoFatura.metodoPagamentoCoincideComIndicador(any(), any()))
            .thenReturn(false);
        when(ctx.descontoFatura.aplicarDescontoProximaFatura(any(), any()))
            .thenReturn(Optional.of("cobranca-abc-123"));
        ctx.indicacaoCriada = indicacaoPendente;
    }

    @Quando("o gateway confirmar o pagamento da primeira mensalidade do indicado")
    public void quandoGatewayConfirmarPagamento() {
        try {
            ctx.servico.confirmarConversao(ctx.indicacaoCriada.getId(), "token-pagamento-xyz");
        } catch (Exception e) {
            ctx.excecaoCapturada = e;
        }
    }

    @Entao("a indicacao deve ter status {string}")
    public void entaoStatusIndicacao(String statusEsperado) {
        assertNull(ctx.excecaoCapturada,
            "Não deveria ter lançado exceção, mas foi: " +
            (ctx.excecaoCapturada != null ? ctx.excecaoCapturada.getMessage() : ""));
        assertEquals(StatusIndicacao.valueOf(statusEsperado), ctx.indicacaoCriada.getStatus());
    }

    @E("a indicacao deve estar invalidada")
    public void eIndicacaoInvalidada() {
        assertEquals(StatusIndicacao.INVALIDA, ctx.indicacaoCriada.getStatus());
    }

    @E("o desconto de 15 porcento deve ser aplicado na proxima fatura do Tutor indicador")
    public void eDescontoAplicadoNaFatura() {
        verify(ctx.descontoFatura).aplicarDescontoProximaFatura(
            eq(ctx.tutorId), eq(new BigDecimal("0.15")));
    }

    @E("a Conquista Lendaria deve ser concedida ao Tutor indicador")
    public void eConquistaLendariaConceida() {
        verify(ctx.motorGamificacao).concederConquistaLendaria(ctx.tutorId);
    }

    @E("o indicado usou o mesmo metodo de pagamento do Tutor indicador")
    public void eMetodoPagamentoCoincide() {
        when(ctx.descontoFatura.metodoPagamentoCoincideComIndicador(any(), any()))
            .thenReturn(true);
    }

    @Quando("um administrador confirmar manualmente a conversao")
    public void quandoAdminConfirmarManualmente() {

        when(ctx.descontoFatura.aplicarDescontoProximaFatura(any(), any()))
            .thenReturn(Optional.of("cobranca-manual-456"));
        try {
            ctx.servico.confirmarConversaoManual(ctx.indicacaoCriada.getId());
        } catch (Exception e) {
            ctx.excecaoCapturada = e;
        }
    }

    @Quando("o sistema consultar a indicacao pendente para o CPF {string}")
    public void quandoConsultarIndicacaoPendentePorCpf(String cpf) {
        CPF cpfIndicado = CPF.de(cpf);
        when(ctx.indicacaoRepositorio.buscarPendenteParaCpfIndicado(cpfIndicado))
            .thenReturn(Optional.of(ctx.indicacaoCriada));
        ctx.indicacaoConsultada = ctx.servico
            .buscarIndicacaoPendenteParaCpfIndicado(cpfIndicado)
            .orElse(null);
    }

    @Entao("a indicacao pendente deve ser encontrada com status {string}")
    public void entaoIndicacaoPendenteEncontradaComStatus(String statusEsperado) {
        assertNotNull(ctx.indicacaoConsultada,
            "A indicação pendente deveria ter sido encontrada.");
        assertEquals(StatusIndicacao.valueOf(statusEsperado),
            ctx.indicacaoConsultada.getStatus());
    }

    @E("o percentual de desconto de boas-vindas deve ser de 30 porcento")
    public void ePercentualDescontoBebindasDeve30() {
        assertEquals(0,
            ProgramaIndicacaoService.PERCENTUAL_DESCONTO_INDICADO
                .compareTo(new BigDecimal("0.30")),
            "O percentual de desconto de boas-vindas deve ser 30%.");
    }

    @Entao("deve ocorrer o erro {string}")
    public void entaoDeveOcorrerErro(String mensagemEsperada) {
        assertNotNull(ctx.excecaoCapturada,
            "Deveria ter lançado exceção com mensagem: " + mensagemEsperada);
        assertEquals(mensagemEsperada, ctx.excecaoCapturada.getMessage());
    }
}
