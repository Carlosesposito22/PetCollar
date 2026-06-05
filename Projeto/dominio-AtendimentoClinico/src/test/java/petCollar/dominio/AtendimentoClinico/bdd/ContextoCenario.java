package petCollar.dominio.AtendimentoClinico.bdd;

import org.mockito.Mockito;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import petCollar.dominio.AtendimentoClinico.relatorio.*;
import petCollar.dominio.AtendimentoClinico.nutricao.*;

public class ContextoCenario {

    public final IRelatorioClinicoRepositorio repositorioRelatorio = Mockito.mock(IRelatorioClinicoRepositorio.class);

    public final GeracaoEvolucaoComparativaService servicoEvolucao = new GeracaoEvolucaoComparativaService(repositorioRelatorio);

    public final AssinaturaDigitalService servicoAssinatura = new AssinaturaDigitalService(repositorioRelatorio);

    public RelatorioClinico relatorio;
    public RelatorioClinicoId relatorioId;
    public PacienteId pacienteId;
    public EvolucaoComparativa evolucaoGerada;

    public final IPlanoNutricionalRepositorio repositorioPlanoNutricional = Mockito.mock(IPlanoNutricionalRepositorio.class);

    public final CalculoNEMService calculoNEMService = new CalculoNEMService();

    public final AnaliseComparativaPesoService analiseComparativaPesoService = new AnaliseComparativaPesoService();

    public final GeracaoCronogramaTransicaoService geracaoCronogramaTransicaoService = new GeracaoCronogramaTransicaoService();

    public PlanoNutricional plano;
    public PlanoNutricionalId planoId;
    public PesoIdeal pesoIdeal;
    public NivelAtividade nivelAtividadeAtual;
    public Double nemResultante;
    public Double gramasDiariasResultante;
    public AlertaManejoCritico alertaResultante;
    public CronogramaTransicao cronogramaResultante;
    public Exception excecaoCapturada;
}


