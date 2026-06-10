package br.com.cesar.petCollar.dominio.AssinaturaFaturamento.plano;

import java.util.List;

import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.ICobrancaRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.dominio.compartilhado.eventos.PublicadorDeEventosDoTutor;

/**
 * Observador concreto: quando o admin altera um {@link Plano}, percorre todos
 * os tutores com cobranças pendentes vinculadas àquele plano e dispara o evento
 * {@code "PLANO_ALTERADO"} via {@link PublicadorDeEventosDoTutor} — sistemas
 * downstream (notificações, gamificação) são informados sem que o publicador
 * conheça seus assinantes (padrão Observer, CLAUDE.md §8).
 */
public class NotificacaoAlteracaoPlanoObservador implements IObservadorDeAlteracaoPlano {

    private final ICobrancaRepositorio cobrancaRepositorio;
    private final PublicadorDeEventosDoTutor publicadorDeEventosDoTutor;

    public NotificacaoAlteracaoPlanoObservador(ICobrancaRepositorio cobrancaRepositorio,
                                               PublicadorDeEventosDoTutor publicadorDeEventosDoTutor) {
        if (cobrancaRepositorio == null)
            throw new IllegalArgumentException("ICobrancaRepositorio não pode ser nulo.");
        if (publicadorDeEventosDoTutor == null)
            throw new IllegalArgumentException("PublicadorDeEventosDoTutor não pode ser nulo.");
        this.cobrancaRepositorio = cobrancaRepositorio;
        this.publicadorDeEventosDoTutor = publicadorDeEventosDoTutor;
    }

    @Override
    public void aoAlterarPlano(Plano plano) {
        PlanoId planoId = plano.getId();
        List<TutorId> afetados = cobrancaRepositorio.listarTutoresComCobrancaPendente(planoId);
        for (TutorId tutorId : afetados) {
            publicadorDeEventosDoTutor.publicar(tutorId, "PLANO_ALTERADO");
        }
    }
}
