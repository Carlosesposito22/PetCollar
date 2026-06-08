package br.com.cesar.petCollar.aplicacao.BeneficiosPlano;

import java.time.LocalDateTime;
import java.util.List;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutor;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.CalculoStatusBeneficioService;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioTutorRepositorio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

/**
 * Lista os benefícios liberados para o tutor logado, com o status recalculado
 * na hora (igual ao padrão de "valores recalculados na hora" usado em F-07) —
 * sem persistir, apenas refletindo o estado vigente para exibição.
 */
public class ConsultarBeneficiosTutorUseCase {

    private final IBeneficioTutorRepositorio beneficioTutorRepositorio;
    private final IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio;
    private final CalculoStatusBeneficioService calculoStatusBeneficioService;

    public ConsultarBeneficiosTutorUseCase(IBeneficioTutorRepositorio beneficioTutorRepositorio,
                                           IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio,
                                           CalculoStatusBeneficioService calculoStatusBeneficioService) {
        if (beneficioTutorRepositorio == null)
            throw new IllegalArgumentException("IBeneficioTutorRepositorio é obrigatório.");
        if (beneficioCatalogoRepositorio == null)
            throw new IllegalArgumentException("IBeneficioCatalogoRepositorio é obrigatório.");
        if (calculoStatusBeneficioService == null)
            throw new IllegalArgumentException("CalculoStatusBeneficioService é obrigatório.");
        this.beneficioTutorRepositorio = beneficioTutorRepositorio;
        this.beneficioCatalogoRepositorio = beneficioCatalogoRepositorio;
        this.calculoStatusBeneficioService = calculoStatusBeneficioService;
    }

    public List<Item> executar(TutorId tutorId) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");

        LocalDateTime agora = LocalDateTime.now();
        return beneficioTutorRepositorio.findByTutorId(tutorId).stream()
                .map(beneficioTutor -> {
                    calculoStatusBeneficioService.calcularStatus(beneficioTutor, agora);
                    BeneficioCatalogo catalogo = beneficioCatalogoRepositorio.findById(beneficioTutor.getBeneficioCatalogoId());
                    return new Item(beneficioTutor, catalogo);
                })
                .toList();
    }

    /** Saída do caso de uso: o agregado do tutor pareado ao seu item de catálogo (para nome/carência). */
    public record Item(BeneficioTutor beneficioTutor, BeneficioCatalogo catalogo) {}
}
