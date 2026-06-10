package br.com.cesar.petCollar.dominio.RelacaoTutor.bdd;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IDescontoFaturaPort;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IEventoAuditoriaRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IIndicacaoRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ILinkIndicacaoRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IMotorGamificacaoPort;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IRegistroCliqueRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.Indicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.LinkIndicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.ProgramaIndicacaoService;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import org.mockito.Mockito;

public class ContextoCenario {

    public final ILinkIndicacaoRepositorio linkRepositorio =
        Mockito.mock(ILinkIndicacaoRepositorio.class);

    public final IRegistroCliqueRepositorio registroCliqueRepositorio =
        Mockito.mock(IRegistroCliqueRepositorio.class);

    public final IIndicacaoRepositorio indicacaoRepositorio =
        Mockito.mock(IIndicacaoRepositorio.class);

    public final IEventoAuditoriaRepositorio auditoriaRepositorio =
        Mockito.mock(IEventoAuditoriaRepositorio.class);

    public final IMotorGamificacaoPort motorGamificacao =
        Mockito.mock(IMotorGamificacaoPort.class);

    public final IDescontoFaturaPort descontoFatura =
        Mockito.mock(IDescontoFaturaPort.class);

    public final ProgramaIndicacaoService servico = new ProgramaIndicacaoService(
        linkRepositorio,
        registroCliqueRepositorio,
        indicacaoRepositorio,
        auditoriaRepositorio,
        motorGamificacao,
        descontoFatura
    );

    public TutorId tutorId;
    public TutorId tutorIdA;
    public TutorId tutorIdB;
    public boolean contaAtiva;
    public LinkIndicacao linkRetornado;
    public LinkIndicacao linkPreExistente;
    public Indicacao indicacaoCriada;
    public Exception excecaoCapturada;
}
