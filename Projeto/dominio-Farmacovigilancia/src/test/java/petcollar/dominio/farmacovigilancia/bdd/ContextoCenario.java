package petcollar.dominio.farmacovigilancia.bdd;

import org.mockito.Mockito;
import petcollar.dominio.farmacovigilancia.*;

import java.time.LocalDate;
import java.util.List;

public class ContextoCenario {

    public final IMedicamentoRepositorio medicamentoRepositorio =
            Mockito.mock(IMedicamentoRepositorio.class);

    public final IMatrizInteracaoRepositorio matrizInteracaoRepositorio =
            Mockito.mock(IMatrizInteracaoRepositorio.class);

    public final IPrescricaoRepositorio prescricaoRepositorio =
            Mockito.mock(IPrescricaoRepositorio.class);

    public final CalculadoraDosagemSeguraService calculadoraDosagem =
            new CalculadoraDosagemSeguraService();

    public final ConsultaInteracoesService consultaInteracoes =
            new ConsultaInteracoesService(matrizInteracaoRepositorio);

    public final AjusteContextualPorTagsService ajusteContextual =
            new AjusteContextualPorTagsService();

    public final CronogramaAdministracaoService cronogramaService =
            new CronogramaAdministracaoService(medicamentoRepositorio);

    public final EmissaoPrescricaoService emissaoService =
            new EmissaoPrescricaoService(prescricaoRepositorio);

    public Medicamento medicamento;
    public MedicamentoId medicamentoId;
    public Prescricao prescricao;
    public PrescricaoId prescricaoId;
    public StatusDosagem statusDosagemResultado;
    public List<RegraInteracao> interacoesDetectadas;
    public double doseMaximaEfetiva;
    public double pesoKg;
    public double dosePrescritaMg;
    public LocalDate dataFimTratamento;
    public Exception excecaoCapturada;
    public TipoTagClinica tagClinicaSelecionada;
}
