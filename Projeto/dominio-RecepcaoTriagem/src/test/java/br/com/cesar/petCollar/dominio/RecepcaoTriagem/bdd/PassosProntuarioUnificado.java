package br.com.cesar.petCollar.dominio.RecepcaoTriagem.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import petcollar.dominio.recepcaotriagem.prontuario.CPF;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import petcollar.dominio.recepcaotriagem.prontuario.ResultadoBusca;
import petcollar.dominio.recepcaotriagem.prontuario.ResultadoBuscaId;
import petcollar.dominio.recepcaotriagem.prontuario.Tutor;
import petcollar.dominio.recepcaotriagem.prontuario.TutorId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PassosProntuarioUnificado {

    private final ContextoCenario contexto;

    public PassosProntuarioUnificado(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ── @Dado ────────────────────────────────────────────────────────────────

    @Given("existe um tutor cadastrado com CPF {string}")
    public void dadaTutorCadastrado(String cpfStr) {
        contexto.cpf = CPF.de(cpfStr);
        contexto.tutorId = TutorId.gerar();
        contexto.tutor = new Tutor(contexto.tutorId, "Tutor Teste", contexto.cpf,
                "11999999999", "teste@email.com");
        when(contexto.tutorRepositorio.findByCpf(contexto.cpf))
                .thenReturn(contexto.tutor);
        contexto.excecaoCapturada = null;
    }

    @Given("nao existe tutor cadastrado com CPF {string}")
    public void dadaTutorNaoCadastrado(String cpfStr) {
        contexto.cpf = CPF.de(cpfStr);
        when(contexto.tutorRepositorio.findByCpf(any(CPF.class)))
                .thenReturn(null);
        when(contexto.tutorRepositorio.existsByCpf(any(CPF.class)))
                .thenReturn(false);
        contexto.excecaoCapturada = null;
    }

    @Given("existe um tutor com CPF {string} com um animal vinculado")
    public void dadaTutorComAnimalVinculado(String cpfStr) {
        contexto.cpf = CPF.de(cpfStr);
        contexto.tutorId = TutorId.gerar();
        contexto.tutor = new Tutor(contexto.tutorId, "Tutor Teste", contexto.cpf, null, null);
        contexto.pacienteId = PacienteId.gerar();
        contexto.tutor.adicionarPaciente(contexto.pacienteId);
        ResultadoBuscaId resultadoId = ResultadoBuscaId.gerar();
        contexto.resultado = new ResultadoBusca(resultadoId, contexto.cpf);
        contexto.resultado.associarTutor(contexto.tutor);
        contexto.excecaoCapturada = null;
    }

    // ── @E (configuração complementar de estado) ─────────────────────────────

    @And("o animal possui diagnostico infectante nos ultimos 40 dias")
    public void eAnimalComDiagnosticoInfectante() {
        when(contexto.diagnosticoRepositorio
                .findInfectantesUltimos(40, contexto.pacienteId))
                .thenReturn(List.of("Cinomose"));
    }

    @And("o animal nao possui diagnosticos infectantes recentes")
    public void eAnimalSemDiagnosticoInfectante() {
        when(contexto.diagnosticoRepositorio
                .findInfectantesUltimos(40, contexto.pacienteId))
                .thenReturn(List.of());
    }

    // ── @Quando ──────────────────────────────────────────────────────────────

    @When("o servico buscar o tutor pelo CPF {string}")
    public void quandoBuscarPorCpf(String cpfStr) {
        try {
            CPF cpf = CPF.de(cpfStr);
            contexto.resultado = contexto.servicoBusca.buscarPorCpf(cpf);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico cadastrar o tutor com CPF {string} e nome {string}")
    public void quandoCadastrarNovoTutor(String cpfStr, String nome) {
        try {
            CPF cpf = CPF.de(cpfStr);
            contexto.resultado = contexto.servicoBusca.cadastrarNovoTutor(cpf, nome, null, null);
            contexto.tutor = contexto.resultado.getTutor();
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico executar a varredura epidemiologica")
    public void quandoExecutarVarredura() {
        try {
            contexto.servicoVarredura.executarVarredura(contexto.tutor, contexto.resultado);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @When("o servico tentar buscar com CPF {string}")
    public void quandoTentarBuscarCpfInvalido(String cpfStr) {
        try {
            CPF cpf = CPF.de(cpfStr);
            contexto.resultado = contexto.servicoBusca.buscarPorCpf(cpf);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    // ── @Então ───────────────────────────────────────────────────────────────

    @Then("o resultado deve indicar que o tutor foi encontrado")
    public void entaoTutorEncontrado() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertNotNull(contexto.resultado);
        assertTrue(contexto.resultado.isTutorEncontrado());
    }

    @And("a lista de animais vinculados deve ser retornada")
    public void eListaAnimaisRetornada() {
        assertNotNull(contexto.resultado.getTutor().getPacientesVinculados());
    }

    @Then("o tutor deve ser salvo no repositorio")
    public void entaoTutorSalvo() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        verify(contexto.tutorRepositorio, times(1)).save(contexto.tutor);
    }

    @And("o resultado da busca deve ser atualizado com o novo tutor")
    public void eResultadoAtualizadoComTutor() {
        assertNotNull(contexto.resultado);
        assertTrue(contexto.resultado.isTutorEncontrado());
        verify(contexto.resultadoBuscaRepositorio, times(1)).save(contexto.resultado);
    }

    @Then("o alerta epidemiologico global deve ser verdadeiro")
    public void entaoAlertaVerdadeiro() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertTrue(contexto.resultado.isAlertaEpidemiologicoGlobal());
    }

    @Then("o alerta epidemiologico global deve ser falso")
    public void entaoAlertaFalso() {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertFalse(contexto.resultado.isAlertaEpidemiologicoGlobal());
    }

    @Then("deve ser lancada uma excecao de argumento invalido")
    public void entaoExcecaoArgumentoInvalido() {
        assertNotNull(contexto.excecaoCapturada);
        assertInstanceOf(IllegalArgumentException.class, contexto.excecaoCapturada);
    }
}
