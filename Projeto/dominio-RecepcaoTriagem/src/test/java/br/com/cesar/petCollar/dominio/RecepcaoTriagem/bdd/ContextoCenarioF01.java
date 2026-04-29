package br.com.cesar.petCollar.dominio.RecepcaoTriagem.bdd;

import org.mockito.Mockito;
import petcollar.dominio.recepcaotriagem.prontuario.BuscaInteligenteTutorService;
import petcollar.dominio.recepcaotriagem.prontuario.CPF;
import petcollar.dominio.recepcaotriagem.prontuario.IDiagnosticoRepositorio;
import petcollar.dominio.recepcaotriagem.prontuario.IResultadoBuscaRepositorio;
import petcollar.dominio.recepcaotriagem.prontuario.ITutorRepositorio;
import petcollar.dominio.recepcaotriagem.prontuario.PacienteId;
import petcollar.dominio.recepcaotriagem.prontuario.ResultadoBusca;
import petcollar.dominio.recepcaotriagem.prontuario.Tutor;
import petcollar.dominio.recepcaotriagem.prontuario.TutorId;
import petcollar.dominio.recepcaotriagem.prontuario.VarreduraEpidemiologicaService;

public class ContextoCenarioF01 {

    // ── Mocks de repositório ────────────────────────────────────────────────
    public final ITutorRepositorio tutorRepositorio =
            Mockito.mock(ITutorRepositorio.class);

    public final IResultadoBuscaRepositorio resultadoBuscaRepositorio =
            Mockito.mock(IResultadoBuscaRepositorio.class);

    public final IDiagnosticoRepositorio diagnosticoRepositorio =
            Mockito.mock(IDiagnosticoRepositorio.class);

    // ── Serviços de domínio (recebem os mocks via construtor) ───────────────
    public final BuscaInteligenteTutorService servicoBusca =
            new BuscaInteligenteTutorService(tutorRepositorio, resultadoBuscaRepositorio);

    public final VarreduraEpidemiologicaService servicoVarredura =
            new VarreduraEpidemiologicaService(diagnosticoRepositorio);

    // ── Estado do cenário (preenchido pelos passos) ─────────────────────────
    public Tutor tutor;
    public TutorId tutorId;
    public CPF cpf;
    public PacienteId pacienteId;
    public ResultadoBusca resultado;
    public Exception excecaoCapturada;
}
