package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Armazenamento em memória para Pacientes do Portal do Tutor.
 * Ciclos vacinais agora persistem via {@code CicloVacinalRepositorioJpa}
 * no módulo de infraestrutura.
 *
 * <p>Nota: {@code @Repository} removido — substituído por
 * {@link PortalTutorRepositorioJpa} (JPA).
 */
public class PortalTutorRepositorioEmMemoria implements PortalTutorRepositorio {

    private static final String TUTOR_DEMO = "tutor@petcollar.com";

    private final ConcurrentMap<String, Paciente> pacientes = new ConcurrentHashMap<>();

    public PortalTutorRepositorioEmMemoria() {
        LocalDate hoje = LocalDate.now();
        salvarPaciente(new Paciente(novoId(), TUTOR_DEMO, "Rex",  "Cão",  "Labrador", hoje.minusYears(3)));
        salvarPaciente(new Paciente(novoId(), TUTOR_DEMO, "Miau", "Gato", "Persa",    hoje.minusYears(2)));
        salvarPaciente(new Paciente(novoId(), TUTOR_DEMO, "Bob",  "Cão",  "Beagle",   hoje.minusYears(5)));
    }

    @Override
    public List<Paciente> listarPacientesDoTutor(String tutorId) {
        return pacientes.values().stream()
                .filter(p -> p.tutorId().equalsIgnoreCase(tutorId))
                .sorted((a, b) -> a.nome().compareToIgnoreCase(b.nome()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Paciente> buscarPaciente(String id) {
        return Optional.ofNullable(pacientes.get(id));
    }

    @Override
    public void salvarPaciente(Paciente paciente) {
        pacientes.put(paciente.id(), paciente);
    }

    @Override
    public void removerPaciente(String id) {
        pacientes.remove(id);
    }

    @Override
    public String novoId() {
        return UUID.randomUUID().toString();
    }
}
