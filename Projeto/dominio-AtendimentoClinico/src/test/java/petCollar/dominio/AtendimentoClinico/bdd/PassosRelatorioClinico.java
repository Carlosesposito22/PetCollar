package petCollar.dominio.AtendimentoClinico.bdd;

import io.cucumber.java.ParameterType;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import petCollar.dominio.AtendimentoClinico.relatorio.*;
import br.com.cesar.petCollar.dominio.compartilhado.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PassosRelatorioClinico {

    private final ContextoCenario contexto;

    public PassosRelatorioClinico(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    /**
     * Conversor de número decimal independente de locale.
     *
     * <p>O {@code {num}} embutido do Cucumber usa o locale padrão da JVM ao
     * interpretar o valor. Em ambiente pt-BR o ponto é tratado como separador de
     * milhar, fazendo {@code "5.2"} virar {@code 52.0} e {@code "0.4"} virar
     * {@code 4.0}. {@link Double#parseDouble} sempre trata o ponto como separador
     * decimal, tornando os cenários determinísticos em qualquer máquina.
     */
    @ParameterType("[-+]?[0-9]*\\.?[0-9]+")
    public double num(String valor) {
        return Double.parseDouble(valor);
    }

    // ── Passos @Dado ──────────────────────────────────────────────────────────

    @Dado("existe um atendimento em curso para o paciente")
    public void dadaAtendimentoEmCurso() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
            contexto.relatorioId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.ROTINEIRO);
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    @Dado("os sinais vitais foram aferidos com peso {num} kg e temperatura {num} graus")
    public void dadaSinaisVitais(double pesoKg, double temperaturaCelsius) {
        SinaisVitais sinaisVitais = new SinaisVitais(pesoKg, temperaturaCelsius, 80, LocalDateTime.now());
        contexto.relatorio.registrarSinaisVitais(sinaisVitais);
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
    }

    @Dado("existe um relatorio com sinais vitais registrados de peso {num} kg")
    public void dadaRelatorioComSinaisVitais(double pesoKg) {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
            contexto.relatorioId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.ROTINEIRO);
        SinaisVitais sinaisVitais = new SinaisVitais(pesoKg, 38.5, 80, LocalDateTime.now());
        contexto.relatorio.registrarSinaisVitais(sinaisVitais);
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    @Dado("existe um historico de atendimento anterior com peso {num} kg")
    public void dadaHistoricoComPeso(double pesoAnteriorKg) {
        RelatorioClinicoId relatorioAnteriorId = RelatorioClinicoId.gerar();
        RelatorioClinico relatorioAnterior = new RelatorioClinico(
            relatorioAnteriorId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.ROTINEIRO);
        SinaisVitais sinaisAnteriores =
            new SinaisVitais(pesoAnteriorKg, 38.2, 76, LocalDateTime.now().minusDays(30));
        relatorioAnterior.registrarSinaisVitais(sinaisAnteriores);
        when(contexto.repositorioRelatorio.listarPorPaciente(contexto.pacienteId))
            .thenReturn(List.of(contexto.relatorio, relatorioAnterior));
    }

    @Dado("existe um relatorio sem historico anterior com peso {num} kg")
    public void dadaRelatorioSemHistorico(double pesoKg) {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
            contexto.relatorioId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.ROTINEIRO);
        SinaisVitais sinaisVitais = new SinaisVitais(pesoKg, 38.0, 72, LocalDateTime.now());
        contexto.relatorio.registrarSinaisVitais(sinaisVitais);
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
        when(contexto.repositorioRelatorio.listarPorPaciente(contexto.pacienteId))
            .thenReturn(List.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    @Dado("existe um relatorio rotineiro com diagnostico e resumo para o tutor preenchidos")
    public void dadaRelatorioRotineiroCompleto() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
            contexto.relatorioId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.ROTINEIRO);
        contexto.relatorio.preencherDiagnosticoTecnico("Dermatite alérgica moderada.");
        contexto.relatorio.preencherResumoParaTutor("Seu pet tem uma alergia na pele. Aplicar pomada 2x ao dia.");
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    @Dado("existe um relatorio ja assinado digitalmente")
    public void dadaRelatorioJaAssinado() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
            contexto.relatorioId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.ROTINEIRO,
            null, null, "Diagnóstico de dermatite.", "Aplicar pomada diária.",
            "Seu pet está bem.", null, null,
            List.of(), List.of(), true,
            LocalDateTime.now().minusHours(1), LocalDateTime.now());
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    @Dado("existe um relatorio rotineiro sem resumo para o tutor")
    public void dadaRelatorioRotineiroSemResumo() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
            contexto.relatorioId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.ROTINEIRO);
        contexto.relatorio.preencherDiagnosticoTecnico("Otite externa bilateral.");
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    @Dado("existe um relatorio cirurgico com diagnostico e resumo mas sem cuidados pos-operatorios")
    public void dadaRelatorioCirurgicoSemCuidados() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
            contexto.relatorioId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.CIRURGICO);
        contexto.relatorio.preencherDiagnosticoTecnico("Orquiectomia eletiva.");
        contexto.relatorio.preencherResumoParaTutor("Cirurgia realizada com sucesso.");
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    @Dado("existe um relatorio cirurgico totalmente preenchido")
    public void dadaRelatorioCirurgicoCompleto() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
            contexto.relatorioId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.CIRURGICO);
        contexto.relatorio.preencherDiagnosticoTecnico("Orquiectomia eletiva.");
        contexto.relatorio.preencherResumoParaTutor("Cirurgia realizada com sucesso. Seu pet precisa de repouso.");
        contexto.relatorio.preencherCuidadosPosOperatorios("Manter colar elizabetano por 7 dias. Não molhar a incisão.");
        contexto.relatorio.preencherTempoRecuperacaoEstimado("10 a 14 dias.");
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    @Dado("existe um relatorio preventivo com apenas resumo para o tutor")
    public void dadaRelatorioPreventivComResumo() {
        contexto.relatorioId = RelatorioClinicoId.gerar();
        contexto.pacienteId = PacienteId.gerar();
        contexto.relatorio = new RelatorioClinico(
            contexto.relatorioId, AtendimentoId.gerar(),
            contexto.pacienteId, MedicoId.gerar(), TipoRelatorio.PREVENTIVO);
        contexto.relatorio.preencherResumoParaTutor("Check-up realizado. Paciente saudável, sem alterações.");
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
        contexto.excecaoCapturada = null;
    }

    // ── Passos @Quando ────────────────────────────────────────────────────────

    @Quando("o servico consolidar os sinais vitais do atendimento")
    public void quandoServicoConsolidaSinaisVitais() {
        try {
            contexto.servicoEvolucao.consolidarSinaisVitais(
                contexto.relatorioId, contexto.relatorio.getSinaisVitais());
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Quando("o servico gerar a evolucao comparativa")
    public void quandoServicoGeraEvolucao() {
        try {
            contexto.servicoEvolucao.gerarEvolucaoComparativa(contexto.relatorioId, 4.8);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Quando("o servico gerar a evolucao comparativa sem historico")
    public void quandoServicoGeraEvolucaoSemHistorico() {
        try {
            contexto.servicoEvolucao.gerarEvolucaoComparativa(contexto.relatorioId, 0.0);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Quando("o servico assinar digitalmente o relatorio")
    public void quandoServicoAssina() {
        try {
            contexto.servicoRelatorio.assinarRelatorio(contexto.relatorioId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Quando("o servico tentar modificar o diagnostico do relatorio")
    public void quandoServicoTentaModificar() {
        try {
            contexto.servicoAssinatura.atualizarDiagnostico(
                contexto.relatorioId, "Novo diagnóstico não autorizado.");
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Quando("o servico tentar assinar o relatorio incompleto")
    public void quandoServicoTentaAssinarIncompleto() {
        try {
            contexto.servicoRelatorio.assinarRelatorio(contexto.relatorioId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Quando("o servico adicionar um anexo do tipo {string} com nome {string}")
    public void quandoServicoAdicionaAnexo(String tipoStr, String nomeArquivo) {
        try {
            AnexoRelatorio anexo = new AnexoRelatorio(
                nomeArquivo, TipoAnexo.valueOf(tipoStr),
                "https://storage.petcollar.com/" + nomeArquivo);
            contexto.servicoAssinatura.adicionarAnexo(contexto.relatorioId, anexo);
            when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
                .thenReturn(Optional.of(contexto.relatorio));
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Quando("o servico adicionar 4 anexos ao relatorio")
    public void quandoServicoAdicionaQuatroAnexos() {
        for (int i = 1; i <= 4; i++) {
            AnexoRelatorio anexo = new AnexoRelatorio(
                "foto" + i + ".jpg", TipoAnexo.FOTO_LESAO,
                "https://storage.petcollar.com/foto" + i + ".jpg");
            contexto.relatorio.adicionarAnexo(anexo);
        }
        when(contexto.repositorioRelatorio.buscarPorId(contexto.relatorioId))
            .thenReturn(Optional.of(contexto.relatorio));
    }

    @Quando("o servico tentar adicionar um quinto anexo")
    public void quandoServicoTentaAdicionarQuintoAnexo() {
        try {
            AnexoRelatorio quinto = new AnexoRelatorio(
                "foto5.jpg", TipoAnexo.FOTO_LESAO,
                "https://storage.petcollar.com/foto5.jpg");
            contexto.servicoAssinatura.adicionarAnexo(contexto.relatorioId, quinto);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    // ── Passos @Então / @E ────────────────────────────────────────────────────

    @Então("os sinais vitais devem ser registrados no relatorio")
    public void entaoSinaisVitaisRegistrados() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.relatorio.getSinaisVitais());
    }

    @E("o repositorio deve ter salvo o relatorio")
    public void eVerificaSaveRelatorio() {
        verify(contexto.repositorioRelatorio, atLeastOnce()).salvar(contexto.relatorio);
    }

    @Então("a variacao de peso deve ser {num} kg")
    public void entaoVariacaoPeso(double variacaoEsperada) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.relatorio.getEvolucaoComparativa());
        assertEquals(variacaoEsperada,
            contexto.relatorio.getEvolucaoComparativa().getVariacaoPesoKg(), 0.001);
    }

    @E("o resumo textual deve conter informacao de ganho de peso")
    public void eResumoTextualGanhoPeso() {
        assertNotNull(contexto.relatorio.getEvolucaoComparativa());
        assertTrue(contexto.relatorio.getEvolucaoComparativa().getResumoTextual()
            .contains("ganho de peso"));
    }

    @Então("o resumo textual deve indicar primeiro atendimento registrado")
    public void entaoResumoTextualPrimeiroAtendimento() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.relatorio.getEvolucaoComparativa());
        assertTrue(contexto.relatorio.getEvolucaoComparativa().getResumoTextual()
            .contains("Primeiro atendimento registrado"));
    }

    @Então("o relatorio deve ter a flag imutavel verdadeira")
    public void entaoRelatorioImutavel() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertTrue(contexto.relatorio.isImutavel());
    }

    @E("o campo assinadoEm deve ser preenchido")
    public void eAssinadoEmPreenchido() {
        assertNotNull(contexto.relatorio.getAssinadoEm());
    }

    @Então("deve ser lancada uma excecao de estado invalido")
    public void entaoExcecaoEstadoInvalido() {
        assertNotNull(contexto.excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, contexto.excecaoCapturada);
    }

    @Então("o relatorio deve conter {int} anexos")
    public void entaoQuantidadeAnexos(int quantidadeEsperada) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(quantidadeEsperada, contexto.relatorio.getAnexos().size());
    }
}
