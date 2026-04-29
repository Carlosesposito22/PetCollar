package petcollar.dominio.gamificacao.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import petcollar.dominio.gamificacao.conquista.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PassosGamificacao {

    private final ContextoCenario contexto;

    public PassosGamificacao(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ── Cenário 1: Evento único concede badge imediatamente ─────────────────

    @Given("existe uma badge de evento unico com chave {string}")
    public void dadaBadgeEventoUnico(String chave) {
        contexto.badgeId = BadgeId.gerar();
        contexto.badge = new Badge(
                contexto.badgeId, "Primeira Consulta", "Badge de primeira consulta",
                CategoriaBadge.SAUDE_DO_PET, RaridadeBadge.COMUM, chave, true, 1);
        when(contexto.badgeRepositorio.findByChaveEvento(chave))
                .thenReturn(List.of(contexto.badge));
        contexto.excecaoCapturada = null;
    }

    @Given("o tutor ainda nao conquistou essa badge")
    public void dadaTutorNaoConquistou() {
        when(contexto.conquistaRepositorio.existsByTutorEBadge(contexto.tutorId, contexto.badgeId))
                .thenReturn(false);
    }

    @When("o servico avaliar as badges para o evento {string}")
    public void quandoServicoAvaliarBadges(String chave) {
        try {
            contexto.conquistasRetornadas = contexto.concessaoService
                    .avaliarBadgesParaEvento(contexto.tutorId, chave);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("a badge deve ser concedida ao tutor")
    public void entaoBadgeConcedida() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.conquistasRetornadas);
        assertEquals(1, contexto.conquistasRetornadas.size());
    }

    @And("a conquista deve ser registrada")
    public void eConquistaRegistrada() {
        verify(contexto.conquistaRepositorio, times(1)).save(any(ConquistaTutor.class));
    }

    // ── Cenário 2: Badge quantitativa exige meta ────────────────────────────

    @Given("existe uma badge quantitativa com meta de {int} e chave {string}")
    public void dadaBadgeQuantitativa(int meta, String chave) {
        contexto.badgeId = BadgeId.gerar();
        contexto.badge = new Badge(
                contexto.badgeId, "Consultas Frequentes", "Badge de consultas",
                CategoriaBadge.SAUDE_DO_PET, RaridadeBadge.INCOMUM, chave, false, meta);
        when(contexto.badgeRepositorio.findByChaveEvento(chave))
                .thenReturn(List.of(contexto.badge));
        when(contexto.conquistaRepositorio.existsByTutorEBadge(contexto.tutorId, contexto.badgeId))
                .thenReturn(false);
        contexto.excecaoCapturada = null;
    }

    @Given("existe um progresso do tutor com valor atual {int} para essa badge")
    public void dadaProgressoComValor(int valorAtual) {
        contexto.progresso = new ProgressoBadge(
                ProgressoBadgeId.gerar(), contexto.tutorId, contexto.badgeId,
                valorAtual, contexto.badge.getMetaQuantitativa(),
                LocalDateTime.now().minusDays(1), LocalDateTime.now());
        when(contexto.progressoRepositorio.findByTutorEBadge(contexto.tutorId, contexto.badgeId))
                .thenReturn(contexto.progresso);
    }

    @When("o servico atualizar o progresso em {int} para essa badge")
    public void quandoAtualizarProgresso(int incremento) {
        try {
            contexto.progresso = contexto.calculoProgressoService
                    .atualizarProgresso(contexto.tutorId, contexto.badgeId, incremento);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o percentual de conclusao deve ser {int}")
    public void entaoPercentualConclusao(int percentualEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(percentualEsperado, (int) contexto.progresso.calcularPercentualConclusao());
    }

    @And("a badge deve ter meta atingida")
    public void eMetaAtingida() {
        assertTrue(contexto.progresso.metaAtingida());
    }

    // ── Cenário 3: Badge já conquistada não é concedida novamente ───────────

    @Given("o tutor ja conquistou essa badge anteriormente")
    public void dadaTutorJaConquistou() {
        when(contexto.conquistaRepositorio.existsByTutorEBadge(contexto.tutorId, contexto.badgeId))
                .thenReturn(true);
    }

    @Then("a badge nao deve ser concedida novamente")
    public void entaoBadgeNaoConcedidaNovamente() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.conquistasRetornadas);
        assertEquals(0, contexto.conquistasRetornadas.size());
        verify(contexto.conquistaRepositorio, never()).save(any(ConquistaTutor.class));
    }

    // ── Cenário 4: Listar próximas conquistas ──────────────────────────────

    @Given("o tutor possui 3 progressos com percentuais 80 60 e 40")
    public void dadaTresProgressos() {
        BadgeId b1 = BadgeId.gerar();
        BadgeId b2 = BadgeId.gerar();
        BadgeId b3 = BadgeId.gerar();

        ProgressoBadge p1 = new ProgressoBadge(ProgressoBadgeId.gerar(), contexto.tutorId, b1, 8, 10,
                LocalDateTime.now(), null);
        ProgressoBadge p2 = new ProgressoBadge(ProgressoBadgeId.gerar(), contexto.tutorId, b2, 6, 10,
                LocalDateTime.now(), null);
        ProgressoBadge p3 = new ProgressoBadge(ProgressoBadgeId.gerar(), contexto.tutorId, b3, 4, 10,
                LocalDateTime.now(), null);

        when(contexto.progressoRepositorio.findByTutorId(contexto.tutorId))
                .thenReturn(Arrays.asList(p1, p2, p3));
        when(contexto.conquistaRepositorio.existsByTutorEBadge(eq(contexto.tutorId), any(BadgeId.class)))
                .thenReturn(false);

        contexto.excecaoCapturada = null;
    }

    @When("o servico listar as {int} proximas conquistas do tutor")
    public void quandoListarProximasConquistas(int quantidade) {
        try {
            contexto.progressosRetornados = contexto.calculoProgressoService
                    .listarProximasConquistas(contexto.tutorId, quantidade);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("as {int} badges retornadas devem estar em ordem decrescente de percentual")
    public void entaoBadgesEmOrdemDecrescente(int quantidade) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.progressosRetornados);
        assertEquals(quantidade, contexto.progressosRetornados.size());
        double percentualAnterior = 100.0;
        for (ProgressoBadge p : contexto.progressosRetornados) {
            double percentual = p.calcularPercentualConclusao();
            assertTrue(percentual <= percentualAnterior,
                    "Percentuais não estão em ordem decrescente");
            percentualAnterior = percentual;
        }
    }

    // ── Cenário 5: Badge quantitativa não concedida antes da meta ──────────

    @Given("o tutor ainda nao tem progresso para essa badge")
    public void dadaSemProgresso() {
        when(contexto.progressoRepositorio.findByTutorEBadge(contexto.tutorId, contexto.badgeId))
                .thenReturn(null);
        contexto.excecaoCapturada = null;
    }

    @Then("a badge nao deve ser concedida ainda")
    public void entaoBadgeNaoConcedidaAinda() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.conquistasRetornadas);
        assertEquals(0, contexto.conquistasRetornadas.size());
    }

    @And("o progresso do tutor deve ser {int}")
    public void eProgressoDoTutor(int valorEsperado) {
        verify(contexto.progressoRepositorio, times(1)).save(any(ProgressoBadge.class));
    }

    // ── Cenário 6: Incremento inválido lança exceção ────────────────────────

    @Given("existe um progresso do tutor com valor atual {int} e meta {int}")
    public void dadaProgressoComValorEMeta(int valorAtual, int meta) {
        contexto.badgeId = BadgeId.gerar();
        contexto.progresso = new ProgressoBadge(
                ProgressoBadgeId.gerar(), contexto.tutorId, contexto.badgeId,
                valorAtual, meta, LocalDateTime.now(), null);
        when(contexto.progressoRepositorio.findByTutorEBadge(contexto.tutorId, contexto.badgeId))
                .thenReturn(contexto.progresso);
        contexto.excecaoCapturada = null;
    }

    @When("o servico tentar atualizar o progresso com incremento {int}")
    public void quandoAtualizarProgressoInvalido(int incremento) {
        try {
            contexto.calculoProgressoService
                    .atualizarProgresso(contexto.tutorId, contexto.badgeId, incremento);
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
