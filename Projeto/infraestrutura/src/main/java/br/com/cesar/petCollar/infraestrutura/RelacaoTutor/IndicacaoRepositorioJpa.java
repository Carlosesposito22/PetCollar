package br.com.cesar.petCollar.infraestrutura.RelacaoTutor;

import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.CPF;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IIndicacaoRepositorio;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.Indicacao;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.IndicacaoId;
import br.com.cesar.petCollar.dominio.RelacaoTutor.indicacao.StatusIndicacao;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class IndicacaoRepositorioJpa implements IIndicacaoRepositorio {

    private final IndicacaoJpaRepository jpa;

    public IndicacaoRepositorioJpa(IndicacaoJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public void salvar(Indicacao indicacao) {
        jpa.save(IndicacaoJpa.fromDomain(indicacao));
    }

    @Override
    public Optional<Indicacao> buscarPorId(IndicacaoId id) {
        return jpa.findById(id.getValor()).map(IndicacaoJpa::toDomain);
    }

    @Override
    public boolean existeConversaoPorCpf(CPF cpf) {
        return jpa.existsByCpfIndicadoAndStatus(cpf.getValor(), StatusIndicacao.CONVERTIDA);
    }

    @Override
    public List<Indicacao> listarPorTutorIndicador(TutorId tutorId) {
        return jpa.findByTutorIndicadorId(tutorId.getValor()).stream()
                  .map(IndicacaoJpa::toDomain)
                  .toList();
    }
}
