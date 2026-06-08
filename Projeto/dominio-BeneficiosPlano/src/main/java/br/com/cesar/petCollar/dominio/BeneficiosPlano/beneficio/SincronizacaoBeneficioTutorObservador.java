package br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Observador concreto: quando o admin altera as configurações de um
 * {@link BeneficioCatalogo}, percorre todos os {@link BeneficioTutor} ativos
 * vinculados àquele catálogo e chama
 * {@link BeneficioTutor#sincronizarLimitePorConfiguracao} — garantindo que
 * tutores que estejam no meio de um período não ultrapassem o novo teto de usos
 * caso o administrador tenha reduzido a cota. Adapta o service de repositório
 * ao papel de "observador" sem que o publicador precise conhecer a infra de
 * persistência (padrão Observer, CLAUDE.md §8).
 */
public class SincronizacaoBeneficioTutorObservador implements IObservadorDeAlteracaoBeneficio {

    private final IBeneficioTutorRepositorio beneficioTutorRepositorio;

    public SincronizacaoBeneficioTutorObservador(IBeneficioTutorRepositorio beneficioTutorRepositorio) {
        if (beneficioTutorRepositorio == null)
            throw new IllegalArgumentException("IBeneficioTutorRepositorio não pode ser nulo.");
        this.beneficioTutorRepositorio = beneficioTutorRepositorio;
    }

    @Override
    public void aoAlterarConfiguracao(BeneficioCatalogo catalogo) {
        List<BeneficioTutor> vinculados =
                beneficioTutorRepositorio.findByBeneficioCatalogoId(catalogo.getId());
        LocalDateTime agora = LocalDateTime.now();
        int novoLimite = catalogo.getLimiteUsosPorPeriodo();
        for (BeneficioTutor bt : vinculados) {
            bt.sincronizarLimitePorConfiguracao(novoLimite, agora);
            beneficioTutorRepositorio.save(bt);
        }
    }
}
