package petcollar.dominio.beneficiosplano.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import petcollar.dominio.beneficiosplano.BeneficioCatalogoId;
import petcollar.dominio.beneficiosplano.BeneficioTutor;
import petcollar.dominio.beneficiosplano.BeneficioTutorId;
import petcollar.dominio.beneficiosplano.CodigoGUID;
import petcollar.dominio.beneficiosplano.PeriodoRenovacao;
import petcollar.dominio.beneficiosplano.StatusBeneficio;
import petcollar.dominio.beneficiosplano.StatusTicket;
import petcollar.dominio.beneficiosplano.TicketBeneficio;
import petcollar.dominio.beneficiosplano.TicketBeneficioId;
import petcollar.dominio.compartilhado.PlanoId;
import petcollar.dominio.compartilhado.TutorId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PassosGerenciamentoCarenciaBeneficiosPlano {

    private final ContextoCenario contexto;

    public PassosGerenciamentoCarenciaBeneficiosPlano(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    private BeneficioTutor novoBeneficioTutor(LocalDateTime dataLiberacao, StatusBeneficio status, int usosRestantes, LocalDateTime inicioPeriodoAtual) {
        return new BeneficioTutor(
                BeneficioTutorId.gerar(),
                TutorId.gerar(),
                PlanoId.gerar(),
                BeneficioCatalogoId.gerar(),
                dataLiberacao,
                status,
                PeriodoRenovacao.MENSAL,
                2,
                usosRestantes,
                inicioPeriodoAtual,
                contexto.agora.minusDays(2),
                null
        );
    }

    private void configurarBeneficioNoRepositorio() {
        when(contexto.beneficioTutorRepositorio.findById(contexto.beneficioTutorId)).thenReturn(contexto.beneficioTutor);
    }

    private void configurarTicketNoRepositorio() {
        when(contexto.ticketBeneficioRepositorio.findByStatus(StatusTicket.GERADO)).thenReturn(List.of(contexto.ticketBeneficio));
    }

    @Given("existe um beneficio do tutor com data de liberacao no futuro")
    public void existeUmBeneficioDoTutorComDataDeLiberacaoNoFuturo() {
        contexto.beneficioTutorId = BeneficioTutorId.gerar();
        contexto.beneficioTutor = novoBeneficioTutor(
                contexto.agora.plusDays(1),
                StatusBeneficio.EM_CARENCIA,
                2,
                contexto.agora.minusDays(1)
        );
        contexto.beneficioTutorId = contexto.beneficioTutor.getId();
        configurarBeneficioNoRepositorio();
        contexto.excecaoCapturada = null;
    }

    @Given("o beneficio tem {int} usos restantes no periodo")
    public void oBeneficioTemUsosRestantesNoPeriodo(Integer usosRestantes) {
        contexto.beneficioTutor = new BeneficioTutor(
                contexto.beneficioTutor.getId(),
                contexto.beneficioTutor.getTutorId(),
                contexto.beneficioTutor.getPlanoId(),
                contexto.beneficioTutor.getBeneficioCatalogoId(),
                contexto.beneficioTutor.getDataLiberacao(),
                contexto.beneficioTutor.getStatus(),
                contexto.beneficioTutor.getPeriodoRenovacao(),
                contexto.beneficioTutor.getLimiteUsosPorPeriodo(),
                usosRestantes,
                contexto.beneficioTutor.getInicioPeriodoAtual(),
                contexto.beneficioTutor.getCriadoEm(),
                contexto.beneficioTutor.getAtualizadoEm()
        );
        configurarBeneficioNoRepositorio();
    }

    @Given("existe um beneficio do tutor com data de liberacao no passado")
    public void existeUmBeneficioDoTutorComDataDeLiberacaoNoPassado() {
        contexto.beneficioTutorId = BeneficioTutorId.gerar();
        contexto.beneficioTutor = novoBeneficioTutor(
                contexto.agora.minusDays(1),
                StatusBeneficio.DISPONIVEL,
                2,
                contexto.agora.minusDays(2)
        );
        contexto.beneficioTutorId = contexto.beneficioTutor.getId();
        configurarBeneficioNoRepositorio();
        contexto.excecaoCapturada = null;
    }

    @Given("existe um beneficio disponivel com {int} usos restantes")
    public void existeUmBeneficioDisponivelComUsosRestantes(Integer usosRestantes) {
        contexto.beneficioTutorId = BeneficioTutorId.gerar();
        contexto.beneficioTutor = novoBeneficioTutor(
                contexto.agora.minusDays(1),
                StatusBeneficio.DISPONIVEL,
                usosRestantes,
                contexto.agora.minusDays(2)
        );
        contexto.beneficioTutorId = contexto.beneficioTutor.getId();
        configurarBeneficioNoRepositorio();
        contexto.excecaoCapturada = null;
    }

    @Given("existe um beneficio com status {string} e {int} usos restantes")
    public void existeUmBeneficioComStatusEUsosRestantes(String statusStr, Integer usosRestantes) {
        StatusBeneficio status = StatusBeneficio.valueOf(statusStr);
        LocalDateTime dataLiberacao = status == StatusBeneficio.EM_CARENCIA ? contexto.agora.plusDays(1) : contexto.agora.minusDays(1);
        contexto.beneficioTutorId = BeneficioTutorId.gerar();
        contexto.beneficioTutor = novoBeneficioTutor(
                dataLiberacao,
                status,
                usosRestantes,
                contexto.agora.minusMonths(1)
        );
        contexto.beneficioTutorId = contexto.beneficioTutor.getId();
        configurarBeneficioNoRepositorio();
        contexto.excecaoCapturada = null;
    }

    @Given("o periodo atual do beneficio comecou ha {int} meses")
    public void oPeriodoAtualDoBeneficioComecouHaMeses(Integer meses) {
        contexto.beneficioTutor = new BeneficioTutor(
                contexto.beneficioTutor.getId(),
                contexto.beneficioTutor.getTutorId(),
                contexto.beneficioTutor.getPlanoId(),
                contexto.beneficioTutor.getBeneficioCatalogoId(),
                contexto.beneficioTutor.getDataLiberacao(),
                contexto.beneficioTutor.getStatus(),
                contexto.beneficioTutor.getPeriodoRenovacao(),
                contexto.beneficioTutor.getLimiteUsosPorPeriodo(),
                contexto.beneficioTutor.getUsosRestantesPeriodoAtual(),
                contexto.agora.minusMonths(meses),
                contexto.beneficioTutor.getCriadoEm(),
                contexto.beneficioTutor.getAtualizadoEm()
        );
        configurarBeneficioNoRepositorio();
    }

    @Given("ja existe um ticket ativo para este beneficio")
    public void jaExisteUmTicketAtivoParaEsteBeneficio() {
        when(contexto.ticketBeneficioRepositorio.existsByBeneficioTutorIdAndStatus(contexto.beneficioTutorId, StatusTicket.GERADO)).thenReturn(true);
    }

    @Given("existe um ticket com status {string} gerado ha mais de {int} hora")
    public void existeUmTicketComStatusGeradoHaMaisDeHora(String statusStr, Integer horas) {
        contexto.beneficioTutorId = BeneficioTutorId.gerar();
        contexto.beneficioTutor = novoBeneficioTutor(
                contexto.agora.minusDays(1),
                StatusBeneficio.DISPONIVEL,
                1,
                contexto.agora.minusMonths(1)
        );
        contexto.beneficioTutorId = contexto.beneficioTutor.getId();
        contexto.ticketBeneficioId = TicketBeneficioId.gerar();
        LocalDateTime geradoEm = contexto.agora.minusHours(horas + 1L);
        contexto.ticketBeneficio = new TicketBeneficio(
                contexto.ticketBeneficioId,
                contexto.beneficioTutorId,
                CodigoGUID.gerar(),
                StatusTicket.valueOf(statusStr),
                geradoEm,
                geradoEm.plusHours(1),
                null,
                null,
                null,
                null
        );
        when(contexto.ticketBeneficioRepositorio.findByStatus(StatusTicket.GERADO)).thenReturn(List.of(contexto.ticketBeneficio));
        when(contexto.beneficioTutorRepositorio.findById(contexto.beneficioTutorId)).thenReturn(contexto.beneficioTutor);
        contexto.excecaoCapturada = null;
    }

    @Given("existe um ticket com status {string} gerado recentemente")
    public void existeUmTicketComStatusGeradoRecentemente(String statusStr) {
        contexto.beneficioTutorId = BeneficioTutorId.gerar();
        contexto.beneficioTutor = novoBeneficioTutor(
                contexto.agora.minusDays(1),
                StatusBeneficio.DISPONIVEL,
                1,
                contexto.agora.minusMonths(1)
        );
        contexto.beneficioTutorId = contexto.beneficioTutor.getId();
        contexto.ticketBeneficioId = TicketBeneficioId.gerar();
        LocalDateTime geradoEm = contexto.agora.minusMinutes(30);
        contexto.ticketBeneficio = new TicketBeneficio(
                contexto.ticketBeneficioId,
                contexto.beneficioTutorId,
                CodigoGUID.gerar(),
                StatusTicket.valueOf(statusStr),
                geradoEm,
                geradoEm.plusHours(1),
                null,
                null,
                null,
                null
        );
        when(contexto.ticketBeneficioRepositorio.findByStatus(StatusTicket.GERADO)).thenReturn(List.of(contexto.ticketBeneficio));
        when(contexto.beneficioTutorRepositorio.findById(contexto.beneficioTutorId)).thenReturn(contexto.beneficioTutor);
        contexto.excecaoCapturada = null;
    }

    @Given("o beneficio associado nao existe no repositorio")
    public void oBeneficioAssociadoNaoExisteNoRepositorio() {
        when(contexto.beneficioTutorRepositorio.findById(contexto.beneficioTutorId)).thenReturn(null);
    }

    @When("o servico calcular o status do beneficio")
    public void oServicoCalcularOStatusDoBeneficio() {
        try {
            contexto.statusCalculado = contexto.calculoStatusBeneficioService.calcularStatus(contexto.beneficioTutor, contexto.agora);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico gerar um ticket para o beneficio")
    public void oServicoGerarUmTicketParaOBeneficio() {
        try {
            contexto.ticketBeneficio = contexto.geracaoTicketService.gerarTicket(contexto.beneficioTutorId, contexto.agora);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico cancelar os tickets expirados")
    public void oServicoCancelarOsTicketsExpirados() {
        try {
            contexto.ticketsExpirados = contexto.expiracaoTicketService.cancelarTicketsExpirados(contexto.agora);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o status deve ser {string}")
    public void oStatusDeveSer(String statusEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertNotNull(contexto.beneficioTutor, "Benefício não pode ser nulo.");
        assertEquals(StatusBeneficio.valueOf(statusEsperado), contexto.statusCalculado);
        assertEquals(StatusBeneficio.valueOf(statusEsperado), contexto.beneficioTutor.getStatus());
    }

    @Then("os usos restantes do periodo atual devem ser {int}")
    public void osUsosRestantesDoPeriodoAtualDevemSer(Integer usosEsperados) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertEquals(usosEsperados.intValue(), contexto.beneficioTutor.getUsosRestantesPeriodoAtual());
    }

    @Then("os usos restantes do beneficio devem ser {int}")
    public void osUsosRestantesDoBeneficioDevemSer(Integer usosEsperados) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertEquals(usosEsperados.intValue(), contexto.beneficioTutor.getUsosRestantesPeriodoAtual());
    }

    @Then("o ticket deve ter status {string}")
    public void oTicketDeveTerStatus(String statusEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertNotNull(contexto.ticketBeneficio, "Ticket não pode ser nulo.");
        assertEquals(StatusTicket.valueOf(statusEsperado), contexto.ticketBeneficio.getStatus());
    }

    @And("o ticket deve ter um codigo GUID unico")
    public void oTicketDeveTerUmCodigoGUIDUnico() {
        assertNotNull(contexto.ticketBeneficio, "Ticket não pode ser nulo.");
        assertNotNull(contexto.ticketBeneficio.getCodigoGUID());
        assertNotNull(contexto.ticketBeneficio.getCodigoGUID().getValor());
        assertFalse(contexto.ticketBeneficio.getCodigoGUID().getValor().isBlank());
        assertDoesNotThrow(() -> UUID.fromString(contexto.ticketBeneficio.getCodigoGUID().getValor()));
    }

    @And("o uso deve ser devolvido ao saldo do beneficio")
    public void oUsoDeveSerDevolvidoAoSaldoDoBeneficio() {
        assertNotNull(contexto.ticketBeneficio);
        assertNotNull(contexto.beneficioTutor);
        assertEquals(StatusTicket.EXPIRADO, contexto.ticketBeneficio.getStatus());
        assertEquals(2, contexto.beneficioTutor.getUsosRestantesPeriodoAtual());
        verify(contexto.ticketBeneficioRepositorio, times(1)).save(contexto.ticketBeneficio);
        verify(contexto.beneficioTutorRepositorio, times(1)).save(contexto.beneficioTutor);
    }

    @And("o repositorio deve ter salvo o ticket")
    public void oRepositorioDeveTerSalvoOTicket() {
        verify(contexto.ticketBeneficioRepositorio, times(1)).save(contexto.ticketBeneficio);
    }

    @And("o repositorio deve ter salvo o beneficio")
    public void oRepositorioDeveTerSalvoOBeneficio() {
        verify(contexto.beneficioTutorRepositorio, times(1)).save(contexto.beneficioTutor);
    }

    @And("o repositorio deve ter salvo o beneficio atualizado")
    public void oRepositorioDeveTerSalvoOBeneficioAtualizado() {
        verify(contexto.beneficioTutorRepositorio, atLeastOnce()).save(contexto.beneficioTutor);
    }

    @Then("nenhum ticket deve ter sido expirado")
    public void nenhumTicketDeveTerSidoExpirado() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção.");
        assertTrue(contexto.ticketsExpirados == null || contexto.ticketsExpirados.isEmpty());
        assertEquals(StatusTicket.GERADO, contexto.ticketBeneficio.getStatus());
    }

    @Then("deve ser lancada uma excecao de estado invalido")
    public void deveSerLancadaUmaExcecaoDeEstadoInvalido() {
        assertNotNull(contexto.excecaoCapturada, "Era esperado lançar exceção.");
        assertInstanceOf(IllegalStateException.class, contexto.excecaoCapturada);
    }

    @Then("deve ser lancada uma excecao de argumento invalido")
    public void deveSerLancadaUmaExcecaoDeArgumentoInvalido() {
        assertNotNull(contexto.excecaoCapturada, "Era esperado lançar exceção.");
        assertInstanceOf(IllegalArgumentException.class, contexto.excecaoCapturada);
    }
}

