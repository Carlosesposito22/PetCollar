package br.com.cesar.petCollar.apresentacao.PortalTutor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

/**
 * Armazenamento provisório em memória para Pacientes e Vacinas do Portal do Tutor.
 * Substituir por adapter de persistência quando os agregados de domínio existirem.
 */
@Repository
public class PortalTutorRepositorioEmMemoria implements PortalTutorRepositorio {

    private static final String TUTOR_DEMO = "tutor@petcollar.com";

    private final ConcurrentMap<String, Paciente> pacientes = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Vacina> vacinas = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Mensalidade> mensalidades = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Plano> planosPorTutor = new ConcurrentHashMap<>();

    public PortalTutorRepositorioEmMemoria() {
        LocalDate hoje = LocalDate.now();

        // ── Pacientes do tutor demo ──────────────────────────────────────────
        Paciente rex  = new Paciente(novoId(), TUTOR_DEMO, "Rex",  "Cão",  "Labrador", hoje.minusYears(3));
        Paciente miau = new Paciente(novoId(), TUTOR_DEMO, "Miau", "Gato", "Persa",    hoje.minusYears(2));
        Paciente bob  = new Paciente(novoId(), TUTOR_DEMO, "Bob",  "Cão",  "Beagle",   hoje.minusYears(5));
        salvarPaciente(rex);
        salvarPaciente(miau);
        salvarPaciente(bob);

        // ── Carteira do Rex (ciclo V10 em andamento + atrasada) ──────────────
        salvarVacina(new Vacina(novoId(), rex.id(), "V10", 1, 3, true,
                hoje.minusMonths(4), "Dr. Carlos Silva", "L12345"));
        salvarVacina(new Vacina(novoId(), rex.id(), "V10", 2, 3, true,
                hoje.minusMonths(3), "Dr. Carlos Silva", "L12346"));
        salvarVacina(new Vacina(novoId(), rex.id(), "Antirrábica", null, null, true,
                hoje.minusMonths(2), "Dra. Maria Santos", "R98765"));
        salvarVacina(new Vacina(novoId(), rex.id(), "V10", 3, 3, false,
                hoje.plusMonths(1), null, null)); // PENDENTE (futura)
        salvarVacina(new Vacina(novoId(), rex.id(), "Giardíase", null, null, false,
                hoje.minusDays(20), null, null));  // EM_ATRASO

        // ── Carteira do Miau (tem vacina em atraso → alerta no card) ─────────
        salvarVacina(new Vacina(novoId(), miau.id(), "Antirrábica", null, null, true,
                hoje.minusMonths(6), "Dra. Maria Santos", "R55501"));
        salvarVacina(new Vacina(novoId(), miau.id(), "Quádrupla Felina", 1, 2, false,
                hoje.minusDays(10), null, null));  // EM_ATRASO

        // ── Carteira do Bob (tudo em dia) ────────────────────────────────────
        salvarVacina(new Vacina(novoId(), bob.id(), "V10", 1, 1, true,
                hoje.minusMonths(8), "Dr. Carlos Silva", "L20011"));
        salvarVacina(new Vacina(novoId(), bob.id(), "Antirrábica", null, null, true,
                hoje.minusMonths(5), "Dra. Maria Santos", "R30022"));

        // ── Financeiro do tutor demo (Plano + Mensalidades) ──────────────────
        // Convenção: vencimento sempre dia 5; competência = mês anterior ao vencimento.
        // Seed relativo a HOJE para a demo mostrar o mesmo padrão independente da data.
        planosPorTutor.put(TUTOR_DEMO, Plano.BASICO_MENSAL);

        LocalDate dia5DesteMes = hoje.withDayOfMonth(5);
        LocalDate proximoVencimento = dia5DesteMes.isAfter(hoje)
                ? dia5DesteMes
                : dia5DesteMes.plusMonths(1);

        // Mensalidade PAGA (vencimento ~2 meses atrás, paga 2 dias antes)
        LocalDate vencPago = proximoVencimento.minusMonths(2);
        salvarMensalidade(new Mensalidade(
                novoId(), TUTOR_DEMO,
                YearMonth.from(vencPago).minusMonths(1),
                Plano.BASICO_MENSAL.valor(), null,
                vencPago, vencPago.minusDays(2)));

        // Mensalidade EM ATRASO (vencimento ~1 mês atrás, não paga)
        LocalDate vencAtraso = proximoVencimento.minusMonths(1);
        salvarMensalidade(new Mensalidade(
                novoId(), TUTOR_DEMO,
                YearMonth.from(vencAtraso).minusMonths(1),
                Plano.BASICO_MENSAL.valor(), null,
                vencAtraso, null));

        // Mensalidade PENDENTE (próximo vencimento, ainda não venceu)
        salvarMensalidade(new Mensalidade(
                novoId(), TUTOR_DEMO,
                YearMonth.from(proximoVencimento).minusMonths(1),
                Plano.BASICO_MENSAL.valor(), null,
                proximoVencimento, null));
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
        vacinas.values().removeIf(v -> v.pacienteId().equals(id));
    }

    @Override
    public List<Vacina> listarVacinasDoPaciente(String pacienteId) {
        return vacinas.values().stream()
                .filter(v -> v.pacienteId().equals(pacienteId))
                .sorted((a, b) -> {
                    if (a.data() == null) return 1;
                    if (b.data() == null) return -1;
                    return a.data().compareTo(b.data());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void salvarVacina(Vacina vacina) {
        vacinas.put(vacina.id(), vacina);
    }

    @Override
    public List<Mensalidade> listarMensalidadesDoTutor(String tutorId) {
        return mensalidades.values().stream()
                .filter(m -> m.tutorId().equalsIgnoreCase(tutorId))
                // mais recente primeiro
                .sorted(Comparator.comparing(Mensalidade::vencimento).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Mensalidade> buscarMensalidade(String id) {
        return Optional.ofNullable(mensalidades.get(id));
    }

    @Override
    public void salvarMensalidade(Mensalidade mensalidade) {
        mensalidades.put(mensalidade.id(), mensalidade);
    }

    @Override
    public Plano planoDoTutor(String tutorId) {
        return planosPorTutor.getOrDefault(tutorId, Plano.BASICO_MENSAL);
    }

    @Override
    public String novoId() {
        return UUID.randomUUID().toString();
    }
}
