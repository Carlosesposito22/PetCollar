package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.IPrescricaoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.Prescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.PrescricaoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.StatusPrescricao;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;

@Repository
public class PrescricaoRepositorioJpa implements IPrescricaoRepositorio {

    private final PrescricaoJpaRepository jpa;

    public PrescricaoRepositorioJpa(PrescricaoJpaRepository jpa) { this.jpa = jpa; }

    @Override public void salvar(Prescricao p) { jpa.save(PrescricaoJpa.fromDomain(p)); }

    @Override public Optional<Prescricao> buscarPorId(PrescricaoId id) {
        return jpa.findById(id.getValor()).map(PrescricaoJpa::toDomain);
    }

    @Override public Optional<Prescricao> buscarVigenteDoPaciente(PacienteId pacienteId) {
        return jpa.findFirstByPacienteIdAndStatusOrderByAtualizadoEmDesc(
                pacienteId.getValor(), StatusPrescricao.FINALIZADA.name())
                .map(PrescricaoJpa::toDomain);
    }

    @Override public List<Prescricao> listarHistoricoDoPaciente(PacienteId pacienteId) {
        return jpa.findByPacienteIdAndStatusInOrderByAtualizadoEmDesc(
                pacienteId.getValor(),
                List.of(StatusPrescricao.FINALIZADA.name(), StatusPrescricao.SUBSTITUIDA.name()))
                .stream().map(PrescricaoJpa::toDomain).toList();
    }

    @Override public List<Prescricao> listarAtivasDoTutor(TutorId tutorId) {
        return jpa.findByTutorIdAndStatusOrderByAtualizadoEmDesc(
                tutorId.getValor(), StatusPrescricao.FINALIZADA.name())
                .stream().map(PrescricaoJpa::toDomain).toList();
    }
}
