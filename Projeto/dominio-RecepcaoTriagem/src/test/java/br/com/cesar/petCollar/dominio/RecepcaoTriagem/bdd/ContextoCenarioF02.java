package br.com.cesar.petCollar.dominio.RecepcaoTriagem.bdd;

import org.mockito.Mockito;
import petcollar.dominio.recepcaotriagem.paciente.IPacienteRepositorio;
import petcollar.dominio.recepcaotriagem.paciente.Paciente;
import petcollar.dominio.recepcaotriagem.paciente.PacienteId;
import petcollar.dominio.recepcaotriagem.paciente.TagueamentoAutomaticoService;
import petcollar.dominio.recepcaotriagem.triagem.ClassificacaoDeRiscoService;
import petcollar.dominio.recepcaotriagem.triagem.CorDeRisco;
import petcollar.dominio.recepcaotriagem.triagem.FinalizacaoTriagemService;
import petcollar.dominio.recepcaotriagem.triagem.ITriagemRepositorio;
import petcollar.dominio.recepcaotriagem.triagem.Triagem;
import petcollar.dominio.recepcaotriagem.triagem.TriagemId;

public class ContextoCenarioF02 {

    // ── Mocks de repositório ─────────────────────────────────────────────────
    public final ITriagemRepositorio repositorioTriagem =
            Mockito.mock(ITriagemRepositorio.class);

    public final IPacienteRepositorio repositorioPaciente =
            Mockito.mock(IPacienteRepositorio.class);

    // ── Serviços de domínio ──────────────────────────────────────────────────
    public final ClassificacaoDeRiscoService servicoClassificacao =
            new ClassificacaoDeRiscoService();

    public final FinalizacaoTriagemService servicoFinalizacao =
            new FinalizacaoTriagemService(repositorioTriagem);

    public final TagueamentoAutomaticoService servicoTagueamento =
            new TagueamentoAutomaticoService(repositorioPaciente);

    // ── Estado do cenário ────────────────────────────────────────────────────
    public Triagem triagem;
    public TriagemId triagemId;
    public Paciente paciente;
    public PacienteId pacienteId;
    public CorDeRisco corDeRiscoResultante;
    public int limiteVermelho;
    public int limiteAmarelo;
    public Exception excecaoCapturada;
}
