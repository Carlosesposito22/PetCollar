package br.com.cesar.petCollar.aplicacao.BeneficiosPlano;

import java.time.LocalDateTime;
import java.util.Optional;

import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioCatalogo;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.BeneficioTutor;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioCatalogoRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.IBeneficioTutorRepositorio;
import br.com.cesar.petCollar.dominio.BeneficiosPlano.beneficio.StatusBeneficio;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

public class ConsumirBeneficioUseCase {

    private final IBeneficioTutorRepositorio beneficioTutorRepositorio;
    private final IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio;

    public ConsumirBeneficioUseCase(IBeneficioTutorRepositorio beneficioTutorRepositorio,
                                    IBeneficioCatalogoRepositorio beneficioCatalogoRepositorio) {
        if (beneficioTutorRepositorio == null)
            throw new IllegalArgumentException("IBeneficioTutorRepositorio é obrigatório.");
        if (beneficioCatalogoRepositorio == null)
            throw new IllegalArgumentException("IBeneficioCatalogoRepositorio é obrigatório.");
        this.beneficioTutorRepositorio = beneficioTutorRepositorio;
        this.beneficioCatalogoRepositorio = beneficioCatalogoRepositorio;
    }

    public void consumir(TutorId tutorId, Categoria categoria, String mensagemIndisponivel) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");
        if (categoria == null) throw new IllegalArgumentException("Categoria é obrigatória.");

        Optional<BeneficioTutor> encontrado = localizar(tutorId, categoria);
        if (encontrado.isEmpty()) return;

        BeneficioTutor beneficio = encontrado.get();
        LocalDateTime agora = LocalDateTime.now();
        beneficio.reiniciarPeriodoSeNecessario(agora);
        beneficio.recalcularStatus(agora);
        if (beneficio.getStatus() != StatusBeneficio.DISPONIVEL) {
            throw new IllegalStateException(mensagemIndisponivel);
        }
        beneficio.debitarUso(agora);
        beneficioTutorRepositorio.save(beneficio);
    }

    public void devolver(TutorId tutorId, Categoria categoria) {
        if (tutorId == null) throw new IllegalArgumentException("TutorId é obrigatório.");
        if (categoria == null) throw new IllegalArgumentException("Categoria é obrigatória.");

        localizar(tutorId, categoria).ifPresent(beneficio -> {
            beneficio.devolverUso(LocalDateTime.now());
            beneficioTutorRepositorio.save(beneficio);
        });
    }

    private Optional<BeneficioTutor> localizar(TutorId tutorId, Categoria categoria) {
        return beneficioTutorRepositorio.findByTutorId(tutorId).stream()
                .filter(bt -> {
                    BeneficioCatalogo catalogo =
                            beneficioCatalogoRepositorio.findById(bt.getBeneficioCatalogoId());
                    return catalogo != null && categoria.corresponde(catalogo.getNome());
                })
                .findFirst();
    }

    public enum Categoria {
        CONSULTA("consulta"),
        VACINACAO("vacina");

        private final String prefixo;

        Categoria(String prefixo) { this.prefixo = prefixo; }

        public boolean corresponde(String nomeBeneficio) {
            return nomeBeneficio != null
                    && nomeBeneficio.trim().toLowerCase().startsWith(prefixo);
        }
    }
}
