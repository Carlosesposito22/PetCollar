package br.com.cesar.petCollar.dominio.AgendamentoClinico.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.Consulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.ConsultaId;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.consulta.MotivoConsulta;
import br.com.cesar.petCollar.dominio.AgendamentoClinico.especialidade.EspecialidadeId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class PassosAgendamentoInicial {

    private final ContextoCenario contexto;

    public PassosAgendamentoInicial(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("uma especialidade {string} com {int} médicos cadastrados")
    public void especialidadeComMedicos(String nome, int quantidade) {
        contexto.especialidadeId = EspecialidadeId.gerar();
        List<MedicoId> medicos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            medicos.add(MedicoId.gerar());
        }
        when(contexto.especialidadeRepositorio.listarMedicosDaEspecialidade(contexto.especialidadeId))
            .thenReturn(medicos);
    }

    @Quando("o tutor lista os médicos da especialidade")
    public void listaMedicosDaEspecialidade() {
        contexto.medicosRetornados =
            contexto.especialidadeRepositorio.listarMedicosDaEspecialidade(contexto.especialidadeId);
    }

    @Entao("a lista de médicos retornada deve ter {int} itens")
    public void listaDeMedicosComTamanho(int esperado) {
        assertEquals(esperado, contexto.medicosRetornados.size());
    }

    @Quando("o tutor tenta criar uma consulta inicial com motivo {string}")
    public void criaConsultaInicialComMotivo(String motivo) {
        try {
            MotivoConsulta motivoConsulta = MotivoConsulta.de(motivo);
            new Consulta(ConsultaId.gerar(), contexto.pacienteId, contexto.tutorId,
                contexto.medicoId, EspecialidadeId.gerar(), motivoConsulta,
                contexto.horarioUtilLivre());
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }

    @Quando("o tutor agenda a consulta inicial")
    public void agendaConsultaInicial() {
        contexto.consulta = new Consulta(ConsultaId.gerar(), contexto.pacienteId, contexto.tutorId,
            contexto.medicoId, EspecialidadeId.gerar(),
            MotivoConsulta.de("Consulta inicial de rotina"), contexto.horario);
        try {
            contexto.inicialService.agendar(contexto.consulta);
        } catch (Exception e) {
            contexto.excecao = e;
        }
    }
}
