package br.com.cesar.petCollar.dominio.RecepcaoTriagem.bdd;

import org.mockito.Mockito;

import petcollar.dominio.recepcaotriagem.paciente.IPacienteRepositorio;
import petcollar.dominio.recepcaotriagem.paciente.Paciente;
import petcollar.dominio.recepcaotriagem.paciente.TagueamentoAutomaticoService;
import petcollar.dominio.recepcaotriagem.prontuario.BuscaInteligenteTutorService;
import petcollar.dominio.recepcaotriagem.prontuario.CPF;
import petcollar.dominio.recepcaotriagem.prontuario.IDiagnosticoRepositorio;
import petcollar.dominio.recepcaotriagem.prontuario.IResultadoBuscaRepositorio;
import petcollar.dominio.recepcaotriagem.prontuario.ITutorRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import petcollar.dominio.recepcaotriagem.prontuario.ResultadoBusca;
import petcollar.dominio.recepcaotriagem.prontuario.Tutor;
import petcollar.dominio.recepcaotriagem.prontuario.TutorId;
import petcollar.dominio.recepcaotriagem.prontuario.VarreduraEpidemiologicaService;
import petcollar.dominio.recepcaotriagem.triagem.ClassificacaoDeRiscoService;
import petcollar.dominio.recepcaotriagem.triagem.CorDeRisco;
import petcollar.dominio.recepcaotriagem.triagem.FinalizacaoTriagemService;
import petcollar.dominio.recepcaotriagem.triagem.ITriagemRepositorio;
import petcollar.dominio.recepcaotriagem.triagem.Triagem;
import petcollar.dominio.recepcaotriagem.triagem.TriagemId;

public class ContextoCenario {

    public final ITutorRepositorio tutorRepositorio = Mockito.mock(ITutorRepositorio.class);

    public final IResultadoBuscaRepositorio resultadoBuscaRepositorio = Mockito.mock(IResultadoBuscaRepositorio.class);

    public final IDiagnosticoRepositorio diagnosticoRepositorio = Mockito.mock(IDiagnosticoRepositorio.class);

    public final BuscaInteligenteTutorService servicoBusca = new BuscaInteligenteTutorService(tutorRepositorio, resultadoBuscaRepositorio);

    public final VarreduraEpidemiologicaService servicoVarredura = new VarreduraEpidemiologicaService(diagnosticoRepositorio);

    public final ITriagemRepositorio repositorioTriagem = Mockito.mock(ITriagemRepositorio.class);

    public final IPacienteRepositorio repositorioPaciente = Mockito.mock(IPacienteRepositorio.class);

    public final ClassificacaoDeRiscoService servicoClassificacao = new ClassificacaoDeRiscoService();

    public final FinalizacaoTriagemService servicoFinalizacao = new FinalizacaoTriagemService(repositorioTriagem);

    public final TagueamentoAutomaticoService servicoTagueamento = new TagueamentoAutomaticoService(repositorioPaciente);

    public Tutor tutor;
    public TutorId tutorId;
    public CPF cpf;
    public PacienteId pacienteId;
    public ResultadoBusca resultado;
    public Exception excecaoCapturada;

    public Triagem triagem;
    public TriagemId triagemId;
    public Paciente paciente;
    public CorDeRisco corDeRiscoResultante;
    public int limiteVermelho;
    public int limiteAmarelo;
}
