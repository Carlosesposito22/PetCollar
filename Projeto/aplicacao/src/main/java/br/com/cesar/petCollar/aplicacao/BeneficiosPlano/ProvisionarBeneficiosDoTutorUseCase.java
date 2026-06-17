package br.com.cesar.petCollar.aplicacao.BeneficiosPlano;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutor;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutorId;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioTutorRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.PlanoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class ProvisionarBeneficiosDoTutorUseCase {

    private final IBeneficioCatalogoRepositorio catalogoRepositorio;
    private final IBeneficioTutorRepositorio beneficioTutorRepositorio;

    public ProvisionarBeneficiosDoTutorUseCase(IBeneficioCatalogoRepositorio catalogoRepositorio,
                                               IBeneficioTutorRepositorio beneficioTutorRepositorio) {
        if (catalogoRepositorio == null)
            throw new IllegalArgumentException("IBeneficioCatalogoRepositorio é obrigatório.");
        if (beneficioTutorRepositorio == null)
            throw new IllegalArgumentException("IBeneficioTutorRepositorio é obrigatório.");
        this.catalogoRepositorio = catalogoRepositorio;
        this.beneficioTutorRepositorio = beneficioTutorRepositorio;
    }

    public void executar(TutorId tutorId, PlanoId planoId) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");
        if (planoId == null) throw new IllegalArgumentException("PlanoId é obrigatório.");

        Set<String> jaProvisionados = beneficioTutorRepositorio.findByTutorId(tutorId).stream()
                .map(bt -> bt.getBeneficioCatalogoId().getValor())
                .collect(Collectors.toSet());

        LocalDateTime agora = LocalDateTime.now();
        List<BeneficioCatalogo> catalogos = catalogoRepositorio.findByPlanoId(planoId);

        for (BeneficioCatalogo catalogo : catalogos) {
            if (!catalogo.isAtivo()) continue;
            if (jaProvisionados.contains(catalogo.getId().getValor())) continue;

            LocalDateTime dataLiberacao = agora.plusDays(catalogo.getCarenciaDias());
            BeneficioTutor beneficioTutor = new BeneficioTutor(
                    BeneficioTutorId.gerar(),
                    tutorId,
                    planoId,
                    catalogo.getId(),
                    dataLiberacao,
                    catalogo.getPeriodoRenovacao(),
                    catalogo.getLimiteUsosPorPeriodo());
            beneficioTutorRepositorio.save(beneficioTutor);
        }
    }
}
