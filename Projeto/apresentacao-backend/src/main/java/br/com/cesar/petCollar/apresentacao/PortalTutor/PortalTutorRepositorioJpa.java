package br.com.cesar.petCollar.apresentacao.PortalTutor;

import br.com.cesar.petCollar.apresentacao.RecepcaoTriagem.TutorRecepcaoJpaRepository;
import br.com.cesar.petCollar.infraestrutura.SaudePreventiva.CicloVacinalJpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PortalTutorRepositorioJpa implements PortalTutorRepositorio {

    private final PacienteJpaRepository pacientes;
    private final CicloVacinalJpaRepository ciclosVacinais;
    private final TutorRecepcaoJpaRepository tutoresRecepcao;

    public PortalTutorRepositorioJpa(PacienteJpaRepository pacientes,
                                      CicloVacinalJpaRepository ciclosVacinais,
                                      TutorRecepcaoJpaRepository tutoresRecepcao) {
        this.pacientes       = pacientes;
        this.ciclosVacinais  = ciclosVacinais;
        this.tutoresRecepcao = tutoresRecepcao;
    }

    @Override
    public List<Paciente> listarPacientesDoTutor(String tutorId) {

        List<String> chaves = new ArrayList<>();
        chaves.add(tutorId);
        tutoresRecepcao.findByEmailIgnoreCase(tutorId).stream()
                .map(t -> t.getId())
                .filter(id -> !id.equalsIgnoreCase(tutorId))
                .forEach(chaves::add);

        LinkedHashMap<String, Paciente> porId = new LinkedHashMap<>();
        pacientes.findByTutorIdIn(chaves).stream()
                .map(PacienteJpa::toDomain)
                .forEach(p -> porId.putIfAbsent(p.id(), p));
        return porId.values().stream()
                .sorted((a, b) -> a.nome().compareToIgnoreCase(b.nome()))
                .toList();
    }

    @Override
    public Optional<Paciente> buscarPaciente(String id) {
        return pacientes.findById(id).map(PacienteJpa::toDomain);
    }

    @Override
    public void salvarPaciente(Paciente paciente) {
        pacientes.save(PacienteJpa.fromDomain(paciente));
    }

    @Override
    @Transactional
    public void removerPaciente(String id) {

        ciclosVacinais.deletarDosesPorPaciente(id);
        ciclosVacinais.deletarCiclosPorPaciente(id);
        if (pacientes.existsById(id)) {
            pacientes.deleteById(id);
        }
    }

    @Override
    public String novoId() {
        return UUID.randomUUID().toString();
    }
}
