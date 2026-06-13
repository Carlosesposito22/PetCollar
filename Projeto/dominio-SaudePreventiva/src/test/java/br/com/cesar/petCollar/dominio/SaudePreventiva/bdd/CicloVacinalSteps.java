package br.com.cesar.petCollar.dominio.SaudePreventiva.bdd;

import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.DoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.StatusDoseVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.TipoProtocolo;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.VacinaId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class CicloVacinalSteps {

    private final ContextoCenarioCicloVacinal ctx;

    public CicloVacinalSteps(ContextoCenarioCicloVacinal ctx) {
        this.ctx = ctx;
    }

    @Dado("que existe um paciente com id {string}")
    public void existePacienteComId(String pacienteId) {
        doNothing().when(ctx.repositorioMock).salvar(any());
        when(ctx.repositorioMock.listarPorPaciente(PacienteId.de(pacienteId)))
            .thenReturn(List.of());
    }

    @Quando("o médico cria o ciclo {string} com {int} doses e protocolo {string} para a data {string}")
    public void medicoChiaCicloComProtocolo(String nome, int totalDoses, String protocolo, String data) {
        doNothing().when(ctx.repositorioMock).salvar(any());
        ctx.cicloAtual = ctx.servico.criarCicloComPrimeiraDose(
            PacienteId.de("pac-f06-001"), nome,
            TipoProtocolo.valueOf(protocolo), totalDoses, null,
            LocalDate.parse(data));
    }

    @Então("o ciclo deve ter {int} dose registrada")
    public void cicloDeveTerDosesRegistradas(int quantidade) {
        assertEquals(quantidade, ctx.cicloAtual.getDoses().size());
    }

    @E("o número da primeira dose deve ser {int}")
    public void numeroDaPrimeiraDose(int numero) {
        assertEquals(numero, ctx.cicloAtual.getDoses().get(0).getDoseNumero());
    }

    @E("o status da primeira dose deve ser {string}")
    public void statusDaPrimeiraDose(String status) {
        assertEquals(StatusDoseVacinal.valueOf(status), ctx.cicloAtual.getDoses().get(0).status());
    }

    @Dado("um ciclo {string} com protocolo {string} e primeira dose aplicada em {string}")
    public void cicloPrimeiraAplicada(String nome, String protocolo, String data) {
        doNothing().when(ctx.repositorioMock).salvar(any());
        ctx.cicloAtual = ctx.servico.criarCicloComPrimeiraDose(
            PacienteId.de("pac-f06-001"), nome,
            TipoProtocolo.valueOf(protocolo), 3, null, LocalDate.parse(data));
        ctx.cicloAtual.getDoses().get(0).aplicar(
            LocalDate.parse(data), "Dr. Carlos Silva", "L99999");
    }

    @Dado("um ciclo {string} com protocolo {string} de {int} dias e primeira dose aplicada em {string}")
    public void cicloPersonalizado(String nome, String protocolo, int dias, String data) {
        doNothing().when(ctx.repositorioMock).salvar(any());
        ctx.cicloAtual = ctx.servico.criarCicloComPrimeiraDose(
            PacienteId.de("pac-f06-001"), nome,
            TipoProtocolo.valueOf(protocolo), 3, dias, LocalDate.parse(data));
        ctx.cicloAtual.getDoses().get(0).aplicar(
            LocalDate.parse(data), "Dr. Carlos Silva", "L99999");
    }

    @Quando("o serviço calcula a data sugerida para a próxima dose")
    public void servicoCalculaDataSugerida() {
        ctx.dataSugerida = ctx.servico.calcularProximaDataSugerida(ctx.cicloAtual);
    }

    @Então("a data sugerida deve ser {string}")
    public void dataSugeridaDeve(String data) {
        assertEquals(LocalDate.parse(data), ctx.dataSugerida);
    }

    @Dado("uma dose do ciclo {string} agendada para {string}")
    public void doseAgendada(String nome, String data) {
        ctx.doseAtual = new DoseVacinal(VacinaId.gerar(), 1, LocalDate.parse(data));
    }

    @E("a dose não foi aplicada")
    public void doseNaoFoiAplicada() {
        assertFalse(ctx.doseAtual.estaAplicada());
    }

    @Quando("o sistema verifica o status da dose")
    public void sistemaVerificaStatus() {
        // apenas consulta — sem ação
    }

    @Então("o status da dose deve ser {string}")
    public void statusDaDoseDeve(String status) {
        assertEquals(StatusDoseVacinal.valueOf(status), ctx.doseAtual.status());
    }

    @Dado("uma dose pendente do ciclo {string} agendada para {string}")
    public void dosePendente(String nome, String data) {
        ctx.doseAtual = new DoseVacinal(VacinaId.gerar(), 1, LocalDate.parse(data));
    }

    @Quando("o médico aplica a dose em {string} com médico {string} e lote {string}")
    public void medicoAplicaDose(String data, String medico, String lote) {
        ctx.doseAtual.aplicar(LocalDate.parse(data), medico, lote);
    }

    @E("a data de aplicação deve ser {string}")
    public void dataDeAplicacao(String data) {
        assertEquals(LocalDate.parse(data), ctx.doseAtual.getDataAplicacao());
    }

    @E("o médico registrado deve ser {string}")
    public void medicoRegistrado(String medico) {
        assertEquals(medico, ctx.doseAtual.getMedico());
    }

    @Dado("uma dose já aplicada do ciclo {string} em {string}")
    public void doseJaAplicada(String nome, String data) {
        ctx.doseAtual = new DoseVacinal(VacinaId.gerar(), 1, LocalDate.parse(data));
        ctx.doseAtual.aplicar(LocalDate.parse(data), "Dr. Vet", "L000");
    }

    @Quando("se tenta aplicar a dose novamente em {string}")
    public void tentaAplicarNovamente(String data) {
        try {
            ctx.doseAtual.aplicar(LocalDate.parse(data), "Dr. Vet", "L001");
        } catch (Exception e) {
            ctx.excecao = e;
        }
    }

    @Então("deve ser lançada exceção com mensagem contendo {string}")
    public void deveLancarExcecao(String fragmento) {
        assertNotNull(ctx.excecao, "Esperava uma exceção mas nenhuma foi lançada.");
        assertTrue(ctx.excecao.getMessage().toLowerCase().contains(fragmento.toLowerCase()),
            "Mensagem esperada: '" + fragmento + "'. Mensagem real: '" + ctx.excecao.getMessage() + "'");
    }

    @Dado("um ciclo {string} com {int} doses e {int} doses já agendadas")
    public void cicloComDosesAgendadas(String nome, int total, int agendadas) {
        doNothing().when(ctx.repositorioMock).salvar(any());
        ctx.cicloAtual = ctx.servico.criarCicloComPrimeiraDose(
            PacienteId.de("pac-f06-001"), nome, TipoProtocolo.FILHOTE, total, null,
            LocalDate.now().plusDays(7));
        for (int i = 1; i < agendadas; i++) {
            ctx.cicloAtual.adicionarProximaDose(LocalDate.now().plusDays(7 + i * 21L));
        }
    }

    @Quando("se tenta agendar uma terceira dose")
    public void tentaAgendarTerceiraDose() {
        try {
            ctx.cicloAtual.adicionarProximaDose(LocalDate.now().plusDays(100));
        } catch (Exception e) {
            ctx.excecao = e;
        }
    }

    @E("o paciente possui um ciclo com dose em atraso")
    public void pacientePossuiCicloEmAtraso() {
        DoseVacinal doseAtrasada = new DoseVacinal(VacinaId.gerar(), 1,
            LocalDate.now().minusDays(30));
        CicloVacinal cicloAtrasado = new CicloVacinal(
            VacinaId.gerar(), PacienteId.de("pac-f06-002"), "Giardíase",
            1, TipoProtocolo.REFORCO_ANUAL, null, List.of(doseAtrasada), null);
        when(ctx.repositorioMock.listarPorPaciente(PacienteId.de("pac-f06-002")))
            .thenReturn(List.of(cicloAtrasado));
    }

    @Quando("o serviço verifica se o paciente possui vacina em atraso")
    public void servicoVerificaAtraso() {
        ctx.cicloAtual = ctx.servico.listarPorPaciente(PacienteId.de("pac-f06-002")).get(0);
    }

    @Então("o resultado deve ser verdadeiro")
    public void resultadoVerdadeiro() {
        assertTrue(ctx.cicloAtual.possuiDoseEmAtraso());
    }
}
