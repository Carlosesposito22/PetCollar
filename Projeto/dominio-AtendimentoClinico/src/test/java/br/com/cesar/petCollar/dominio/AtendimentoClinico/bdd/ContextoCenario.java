package petcollar.dominio.atendimentoclinico.bdd;

import org.mockito.Mockito;
import petcollar.dominio.atendimentoclinico.relatorio.*;

import java.util.List;

public class ContextoCenario {

    // ── Mocks de repositório ──────────────────────────────────────────────────
    public final IRelatorioClinicoRepositorio repositorioRelatorio =
            Mockito.mock(IRelatorioClinicoRepositorio.class);

    // ── Serviços de domínio (recebem mocks via construtor) ────────────────────
    public final GeracaoEvolucaoComparativaService servicoEvolucao =
            new GeracaoEvolucaoComparativaService(repositorioRelatorio);

    public final AssinaturaDigitalService servicoAssinatura =
            new AssinaturaDigitalService(repositorioRelatorio);

    // ── Estado do cenário (preenchido pelos passos) ───────────────────────────
    public RelatorioClinico relatorio;
    public RelatorioClinicoId relatorioId;
    public PacienteId pacienteId;
    public EvolucaoComparativa evolucaoGerada;
    public Exception excecaoCapturada;
}
