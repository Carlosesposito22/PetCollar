package br.com.cesar.petCollar.dominio.RecepcaoTriagem.bdd;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import petcollar.dominio.recepcaotriagem.paciente.Especie;
import petcollar.dominio.recepcaotriagem.paciente.Paciente;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import petcollar.dominio.recepcaotriagem.triagem.CorDeRisco;
import petcollar.dominio.recepcaotriagem.triagem.PesoTotal;
import petcollar.dominio.recepcaotriagem.triagem.RespostaSintoma;
import petcollar.dominio.recepcaotriagem.triagem.Sintoma;
import petcollar.dominio.recepcaotriagem.triagem.StatusTriagem;
import petcollar.dominio.recepcaotriagem.triagem.Triagem;
import petcollar.dominio.recepcaotriagem.triagem.TriagemId;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PassosTriagem {

    private final ContextoCenario contexto;

    public PassosTriagem(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ── Cenários de ClassificacaoDeRiscoService ───────────────────────────────

    @Given("existe uma triagem em elaboracao para o paciente")
    public void dadaTriagemEmElaboracao() {
        contexto.triagemId = TriagemId.gerar();
        contexto.triagem = new Triagem(contexto.triagemId, PacienteId.gerar());
        when(contexto.repositorioTriagem.findById(contexto.triagemId))
                .thenReturn(contexto.triagem);
        contexto.excecaoCapturada = null;
    }

    @And("o sintoma {string} com peso {int} esta presente")
    public void eSintomaComPesoPresente(String codigo, int peso) {
        Sintoma sintoma = new Sintoma(codigo, codigo, peso);
        contexto.triagem.adicionarResposta(new RespostaSintoma(sintoma, true));
    }

    @And("a configuracao define limiteVermelho {int} e limiteAmarelo {int}")
    public void eConfiguracaoLimites(int limiteVermelho, int limiteAmarelo) {
        contexto.limiteVermelho = limiteVermelho;
        contexto.limiteAmarelo = limiteAmarelo;
    }

    @When("o servico calcular a cor de risco")
    public void quandoServicoCalculaCorDeRisco() {
        try {
            PesoTotal pesoTotal = contexto.servicoClassificacao.calcular(
                    contexto.triagem.getRespostas(),
                    contexto.limiteVermelho,
                    contexto.limiteAmarelo
            );
            contexto.corDeRiscoResultante = pesoTotal.getCorDeRisco();
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("a cor de risco resultante deve ser {string}")
    public void entaoCorDeRiscoResultante(String corEsperada) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(CorDeRisco.valueOf(corEsperada), contexto.corDeRiscoResultante);
    }

    // ── Cenário de FinalizacaoTriagemService ──────────────────────────────────

    @Given("existe uma triagem em elaboracao com cor de risco calculada")
    public void dadaTriagemComCorDeRiscoCalculada() {
        contexto.triagemId = TriagemId.gerar();
        contexto.triagem = new Triagem(contexto.triagemId, PacienteId.gerar());
        contexto.triagem.definirCorDeRisco(CorDeRisco.VERDE);
        when(contexto.repositorioTriagem.findById(contexto.triagemId))
                .thenReturn(contexto.triagem);
        contexto.excecaoCapturada = null;
    }

    @When("o servico finalizar a triagem")
    public void quandoServicoFinalizaTriagem() {
        try {
            contexto.servicoFinalizacao.finalizar(contexto.triagemId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o status da triagem deve ser {string}")
    public void entaoStatusDaTriagem(String statusEsperado) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        assertEquals(StatusTriagem.valueOf(statusEsperado), contexto.triagem.getStatus());
    }

    @And("a triagem deve estar bloqueada para alteracao")
    public void eTriagemBloqueadaParaAlteracao() {
        assertTrue(contexto.triagem.isBloqueadaParaAlteracao(),
                "A triagem deveria estar bloqueada para alteração");
    }

    // ── Cenário de TagueamentoAutomaticoService ───────────────────────────────

    @Given("existe um paciente da especie {string} com data de nascimento ha {int} anos")
    public void dadaPacienteComIdadeEmAnos(String especieStr, int anos) {
        contexto.pacienteId = PacienteId.gerar();
        LocalDate dataNascimento = LocalDate.now().minusYears(anos);
        Especie especie = Especie.valueOf(especieStr);
        contexto.paciente = new Paciente(contexto.pacienteId, "Rex", especie, "SRD", dataNascimento);
        when(contexto.repositorioPaciente.findById(contexto.pacienteId))
                .thenReturn(contexto.paciente);
        contexto.excecaoCapturada = null;
    }

    @When("o servico aplicar as tags automaticas")
    public void quandoServicoAplicaTagsAutomaticas() {
        try {
            contexto.servicoTagueamento.aplicarTagsAutomaticas(contexto.pacienteId);
        } catch (Exception e) {
            contexto.excecaoCapturada = e;
        }
    }

    @Then("o paciente deve ter a tag {string}")
    public void entaoPacienteTemTag(String codigoTag) {
        assertNull(contexto.excecaoCapturada, "Não deveria ter lançado exceção");
        boolean temTag = contexto.paciente.getTags().stream()
                .anyMatch(t -> t.getCodigo().equals(codigoTag));
        assertTrue(temTag, "O paciente deveria ter a tag " + codigoTag);
    }
}
