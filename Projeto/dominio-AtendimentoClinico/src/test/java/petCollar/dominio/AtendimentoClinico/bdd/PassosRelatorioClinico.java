package petcollar.dominio.atendimentoclinico.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import petCollar.dominio.AtendimentoClinico.relatorio.*;
import br.com.cesar.petCollar.dominio.compartilhado.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PassosRelatorioClinico {

    private final ContextoCenario contexto;

    public PassosRelatorioClinico(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ── Passos @Given ──────────────────────────────────────────────────────────

    @Given("existe um atendimento em curso para o paciente")
    public void dadaAtendimentoEmCurso() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
                contexto.relatorioId,
                AtendimentoId.gerar(),
                contexto.pacienteId,
                MedicoId.gerar()
        );
        when(contexto.repositorioRelatorio.findById(contexto.relatorioId))
                .thenReturn(contexto.relatorio);
        contexto.excecaoCapturada = null;
    }

    @Given("os sinais vitais foram aferidos com peso {double} kg e temperatura {double} graus")
    public void dadaSinaisVitais(double pesoKg, double temperaturaCelsius) {
        SinaisVitais sinaisVitais = new SinaisVitais(pesoKg, temperaturaCelsius, 80, LocalDateTime.now());
        contexto.relatorio.registrarSinaisVitais(sinaisVitais);
        when(contexto.repositorioRelatorio.findById(contexto.relatorioId))
                .thenReturn(contexto.relatorio);
    }

    @Given("existe um relatorio com sinais vitais registrados de peso {double} kg")
    public void dadaRelatorioComSinaisVitais(double pesoKg) {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
                contexto.relatorioId,
                AtendimentoId.gerar(),
                contexto.pacienteId,
                MedicoId.gerar()
        );
        SinaisVitais sinaisVitais = new SinaisVitais(pesoKg, 38.5, 80, LocalDateTime.now());
        contexto.relatorio.registrarSinaisVitais(sinaisVitais);
        when(contexto.repositorioRelatorio.findById(contexto.relatorioId))
                .thenReturn(contexto.relatorio);
        contexto.excecaoCapturada = null;
    }

    @Given("existe um historico de atendimento anterior com peso {double} kg")
    public void dadaHistoricoComPeso(double pesoAnteriorKg) {
        // Simula que há outro relatório no histórico do paciente (relatório anterior)
        RelatorioClinicoId relatorioAnteriorId = RelatorioClinicoId.gerar();
        RelatorioClinico relatorioAnterior = new RelatorioClinico(
                relatorioAnteriorId,
                AtendimentoId.gerar(),
                contexto.pacienteId,
                MedicoId.gerar()
        );
        SinaisVitais sinaisAnteriores = new SinaisVitais(pesoAnteriorKg, 38.2, 76, LocalDateTime.now().minusDays(30));
        relatorioAnterior.registrarSinaisVitais(sinaisAnteriores);

        when(contexto.repositorioRelatorio.findByPacienteId(contexto.pacienteId))
                .thenReturn(List.of(contexto.relatorio, relatorioAnterior));
    }

    @Given("existe um relatorio sem historico anterior com peso {double} kg")
    public void dadaRelatorioSemHistorico(double pesoKg) {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
                contexto.relatorioId,
                AtendimentoId.gerar(),
                contexto.pacienteId,
                MedicoId.gerar()
        );
        SinaisVitais sinaisVitais = new SinaisVitais(pesoKg, 38.0, 72, LocalDateTime.now());
        contexto.relatorio.registrarSinaisVitais(sinaisVitais);
        when(contexto.repositorioRelatorio.findById(contexto.relatorioId))
                .thenReturn(contexto.relatorio);
        // Sem histórico: somente o próprio relatório retorna
        when(contexto.repositorioRelatorio.findByPacienteId(contexto.pacienteId))
                .thenReturn(List.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    @Given("existe um relatorio nao assinado com diagnostico e orientacoes preenchidos")
    public void dadaRelatorioNaoAssinadoCompleto() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
                contexto.relatorioId,
                AtendimentoId.gerar(),
                contexto.pacienteId,
                MedicoId.gerar()
        );
        contexto.relatorio.preencherDiagnosticoTecnico("Dermatite alérgica moderada.");
        contexto.relatorio.preencherOrientacoesManejo("Aplicar pomada anti-inflamatória 2x ao dia.");
        when(contexto.repositorioRelatorio.findById(contexto.relatorioId))
                .thenReturn(contexto.relatorio);
        contexto.excecaoCapturada = null;
    }

    @Given("existe um relatorio ja assinado digitalmente")
    public void dadaRelatorioJaAssinado() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
                contexto.relatorioId,
                AtendimentoId.gerar(),
                contexto.pacienteId,
                MedicoId.gerar(),
                null,
                null,
                "Diagnóstico de dermatite.",
                "Aplicar pomada diária.",
                "Seu pet está bem.",
                List.of(),
                true,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
        );
        when(contexto.repositorioRelatorio.findById(contexto.relatorioId))
                .thenReturn(contexto.relatorio);
        contexto.excecaoCapturada = null;
    }

    @Given("existe um relatorio nao assinado sem diagnostico tecnico")
    public void dadaRelatorioSemDiagnostico() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
                contexto.relatorioId,
                AtendimentoId.gerar(),
                contexto.pacienteId,
                MedicoId.gerar()
        );
        // Preenche somente orientações, sem diagnóstico
        contexto.relatorio.preencherOrientacoesManejo("Repouso por 3 dias.");
        when(contexto.repositorioRelatorio.findById(contexto.relatorioId))
                .thenReturn(contexto.relatorio);
        contexto.excecaoCapturada = null;
    }

    // ── Passos @Quando ────────────────────────────────────────────────────────

    @When("o servico consolidar os sinais vitais do atendimento")
    public void quandoServicoConsolidaSinaisVitais() {
        try {
            contexto.servicoEvolucao.consolidarSinaisVitais(
                    contexto.relatorioId,
                    contexto.relatorio.getSinaisVitais()
            );
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico gerar a evolucao comparativa")
    public void quandoServicoGeraEvolucao() {
        try {
            contexto.servicoEvolucao.gerarEvolucaoComparativa(contexto.relatorioId, 4.8);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico gerar a evolucao comparativa sem historico")
    public void quandoServicoGeraEvolucaoSemHistorico() {
        try {
            contexto.servicoEvolucao.gerarEvolucaoComparativa(contexto.relatorioId, 0.0);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico assinar digitalmente o relatorio")
    public void quandoServicoAssina() {
        try {
            contexto.servicoAssinatura.assinarRelatorio(contexto.relatorioId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico tentar modificar o diagnostico do relatorio")
    public void quandoServicoTentaModificar() {
        try {
            contexto.servicoAssinatura.atualizarDiagnostico(
                    contexto.relatorioId,
                    "Novo diagnóstico não autorizado."
            );
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico tentar assinar o relatorio sem diagnostico")
    public void quandoServicoTentaAssinarSemDiagnostico() {
        try {
            contexto.servicoAssinatura.assinarRelatorio(contexto.relatorioId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico adicionar um anexo do tipo {string} com nome {string}")
    public void quandoServicoAdicionaAnexo(String tipoStr, String nomeArquivo) {
        try {
            AnexoRelatorio anexo = new AnexoRelatorio(
                    nomeArquivo,
                    TipoAnexo.valueOf(tipoStr),
                    "https://storage.petcollar.com/" + nomeArquivo
            );
            contexto.servicoAssinatura.adicionarAnexo(contexto.relatorioId, anexo);
            // Reconfigurar mock após mutação de estado
            when(contexto.repositorioRelatorio.findById(contexto.relatorioId))
                    .thenReturn(contexto.relatorio);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    // ── Passos @Então / @E ────────────────────────────────────────────────────

    @Then("os sinais vitais devem ser registrados no relatorio")
    public void entaoSinaisVitaisRegistrados() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.relatorio.getSinaisVitais());
    }

    @And("o repositorio deve ter salvo o relatorio")
    public void eVerificaSaveRelatorio() {
        verify(contexto.repositorioRelatorio, atLeastOnce()).save(contexto.relatorio);
    }

    @Then("a variacao de peso deve ser {double} kg")
    public void entaoVariacaoPeso(double variacaoEsperada) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.relatorio.getEvolucaoComparativa());
        assertEquals(variacaoEsperada,
                contexto.relatorio.getEvolucaoComparativa().getVariacaoPesoKg(),
                0.001);
    }

    @And("o resumo textual deve conter informacao de ganho de peso")
    public void eResumoTextualGanhoPeso() {
        assertNotNull(contexto.relatorio.getEvolucaoComparativa());
        assertTrue(contexto.relatorio.getEvolucaoComparativa().getResumoTextual()
                .contains("ganho de peso"));
    }

    @Then("o resumo textual deve indicar primeiro atendimento registrado")
    public void entaoResumoTextualPrimeiroAtendimento() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.relatorio.getEvolucaoComparativa());
        assertTrue(contexto.relatorio.getEvolucaoComparativa().getResumoTextual()
                .contains("Primeiro atendimento registrado"));
    }

    @Then("o relatorio deve ter a flag imutavel verdadeira")
    public void entaoRelatorioImutavel() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertTrue(contexto.relatorio.isImutavel());
    }

    @And("o campo assinadoEm deve ser preenchido")
    public void eAssinadoEmPreenchido() {
        assertNotNull(contexto.relatorio.getAssinadoEm());
    }

    @Then("deve ser lancada uma excecao de estado invalido")
    public void entaoExcecaoEstadoInvalido() {
        assertNotNull(contexto.excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, contexto.excecaoCapturada);
    }

    @Then("o relatorio deve conter {int} anexos")
    public void entaoQuantidadeAnexos(int quantidadeEsperada) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(quantidadeEsperada, contexto.relatorio.getAnexos().size());
    }
}