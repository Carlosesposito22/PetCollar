package petcollar.dominio.assinaturafaturamento.bdd;

import org.mockito.Mockito;
import petcollar.dominio.assinaturafaturamento.*;
import petcollar.dominio.assinaturafaturamento.indicacao.*;

import java.util.List;

public class ContextoCenario {

    // ── Mocks de repositório — AssinaturaFaturamento ────────────────────────
    public final IPlanoRepositorio planoRepositorio =
            Mockito.mock(IPlanoRepositorio.class);

    public final IAssinaturaRepositorio assinaturaRepositorio =
            Mockito.mock(IAssinaturaRepositorio.class);

    public final ICobrancaRepositorio cobrancaRepositorio =
            Mockito.mock(ICobrancaRepositorio.class);

    public final IPagamentoRepositorio pagamentoRepositorio =
            Mockito.mock(IPagamentoRepositorio.class);

    // ── Serviços — AssinaturaFaturamento ────────────────────────────────────
    public final ContratacaoPlanoService contratacaoPlanoService =
            new ContratacaoPlanoService(planoRepositorio, assinaturaRepositorio, cobrancaRepositorio);

    public final GestaoInadimplenciaService gestaoInadimplenciaService =
            new GestaoInadimplenciaService(assinaturaRepositorio, cobrancaRepositorio);

    public final GeracaoQRCodeService geracaoQRCodeService =
            new GeracaoQRCodeService(cobrancaRepositorio, pagamentoRepositorio);

    public final CalculoJurosService calculoJurosService =
            new CalculoJurosService();

    // ── Mocks de repositório — Programa de Indicação ────────────────────────
    public final ILinkIndicacaoRepositorio linkIndicacaoRepositorio =
            Mockito.mock(ILinkIndicacaoRepositorio.class);

    public final IConversaoIndicacaoRepositorio conversaoRepositorio =
            Mockito.mock(IConversaoIndicacaoRepositorio.class);

    public final IRecompensaIndicacaoRepositorio recompensaRepositorio =
            Mockito.mock(IRecompensaIndicacaoRepositorio.class);

    // ── Serviços — Programa de Indicação ────────────────────────────────────
    public final GeracaoLinkIndicacaoService geracaoLinkService =
            new GeracaoLinkIndicacaoService(linkIndicacaoRepositorio);

    public final ValidacaoFraudeService validacaoFraudeService =
            new ValidacaoFraudeService(conversaoRepositorio);

    public final AtribuicaoLastClickService lastClickService =
            new AtribuicaoLastClickService(conversaoRepositorio);

    public final AplicacaoDescontoIndicadoService descontoIndicadoService =
            new AplicacaoDescontoIndicadoService();

    public final AplicacaoDescontoIndicadorService descontoIndicadorService =
            new AplicacaoDescontoIndicadorService();

    public final ConfirmacaoConversaoService confirmacaoConversaoService =
            new ConfirmacaoConversaoService(conversaoRepositorio, recompensaRepositorio,
                    validacaoFraudeService, descontoIndicadoService, descontoIndicadorService);

    public final DisparoGamificacaoService disparoGamificacaoService =
            new DisparoGamificacaoService(recompensaRepositorio);

    // ── Estado do cenário — AssinaturaFaturamento ────────────────────────────
    public Plano plano;
    public PlanoId planoId;

    public Assinatura assinatura;
    public AssinaturaId assinaturaId;

    public Cobranca cobranca;
    public CobrancaId cobrancaId;
    public List<Cobranca> cobrancasEmAtraso;

    public Pagamento pagamento;

    public Dinheiro valorCalculado;
    public double valorOriginalCobranca;

    // ── Estado do cenário — Programa de Indicação ───────────────────────────
    public LinkIndicacao linkIndicacao;
    public LinkIndicacaoId linkIndicacaoId;

    public ConversaoIndicacao conversaoIndicacao;
    public ConversaoId conversaoId;

    public RecompensaIndicacao recompensaIndicacao;
    public RecompensaIndicacaoId recompensaIndicacaoId;

    public IndicacaoConfirmadaEvent indicacaoConfirmadaEvent;
    public IndicacaoValidadaParaGamificacaoEvent gamificacaoEvent;

    public double percentualDescontoResolvido;

    public Exception excecaoCapturada;
}
