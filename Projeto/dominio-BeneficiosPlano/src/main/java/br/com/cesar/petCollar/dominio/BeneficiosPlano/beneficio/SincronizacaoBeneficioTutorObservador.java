package br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio;

import java.time.LocalDateTime;
import java.util.List;

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
