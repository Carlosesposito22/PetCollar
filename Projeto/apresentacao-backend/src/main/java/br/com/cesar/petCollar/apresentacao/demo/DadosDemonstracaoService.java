package br.com.cesar.petCollar.apresentacao.demo;

import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.StatusConta;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioAutenticavel;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioJpa;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioJpaRepository;
import br.com.cesar.petCollar.apresentacao.PortalTutor.Paciente;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpa;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpaRepository;
import br.com.cesar.petCollar.apresentacao.PortalTutor.Vacina;
import br.com.cesar.petCollar.apresentacao.PortalTutor.VacinaJpa;
import br.com.cesar.petCollar.apresentacao.PortalTutor.VacinaJpaRepository;
import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.PlanosPadrao;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Competencia;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.EspecialidadeJpa;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.EspecialidadeJpaRepository;
import br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento.CobrancaJpa;
import br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento.CobrancaJpaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gerencia o Modo Demonstração: ativa (semeia) e desativa (limpa) dados de
 * exemplo usados para demonstrar os fluxos do sistema. Ativado pelo atalho
 * Shift+D no frontend.
 *
 * <p>Dados operacionais (admin, plano, especialidades, ConfiguracaoProtocolo)
 * são mantidos intactos — este serviço só toca dados de demonstração.
 */
@Service
public class DadosDemonstracaoService {

    private static final Logger log = LoggerFactory.getLogger(DadosDemonstracaoService.class);

    private static final String ID_TUTOR_DEMO    = "tutor@petcollar.com";
    private static final String ID_SUSPENSO_DEMO = "suspenso@petcollar.com";
    private static final String ID_RECEPCAO_DEMO = "100001";
    private static final String ID_MEDICO_DEMO   = "200001";
    private static final String SENHA_DEMO       = "petcollar123";

    private final UsuarioJpaRepository usuarios;
    private final PacienteJpaRepository pacientes;
    private final VacinaJpaRepository vacinas;
    private final EspecialidadeJpaRepository especialidades;
    private final CobrancaJpaRepository cobrancas;
    private final PasswordEncoder encoder;

    public DadosDemonstracaoService(
            UsuarioJpaRepository usuarios,
            PacienteJpaRepository pacientes,
            VacinaJpaRepository vacinas,
            EspecialidadeJpaRepository especialidades,
            CobrancaJpaRepository cobrancas,
            PasswordEncoder encoder) {
        this.usuarios       = usuarios;
        this.pacientes      = pacientes;
        this.vacinas        = vacinas;
        this.especialidades = especialidades;
        this.cobrancas      = cobrancas;
        this.encoder        = encoder;
    }

    public boolean estaAtivo() {
        return usuarios.existsById(ID_TUTOR_DEMO);
    }

    @Transactional
    public void ativar() {
        if (estaAtivo()) {
            log.info("[DEMO] Modo demonstração já está ativo — nenhuma ação tomada.");
            return;
        }
        log.info("[DEMO] Ativando modo demonstração…");

        String senha = encoder.encode(SENHA_DEMO);
        seedUsuarios(senha);
        vincularMedicoAEspecialidades(ID_MEDICO_DEMO);
        seedPacientesEVacinas();
        seedCobrancas();

        log.info("[DEMO] Modo demonstração ativado. Login: {} / senha: {}", ID_TUTOR_DEMO, SENHA_DEMO);
    }

    @Transactional
    public void desativar() {
        if (!estaAtivo()) {
            log.info("[DEMO] Modo demonstração não está ativo — nenhuma ação tomada.");
            return;
        }
        log.info("[DEMO] Desativando modo demonstração…");

        // Cobranças do tutor demo
        cobrancas.deleteAll(cobrancas.findByTutorIdOrderByVencimentoDesc(ID_TUTOR_DEMO));

        // Vacinas e pacientes (todos são demo)
        vacinas.deleteAll();
        pacientes.deleteAll();

        // Especialidades: preservadas (seed operacional) — apenas desvincula o médico demo
        desvincularMedicoDeEspecialidades(ID_MEDICO_DEMO);

        // Usuários demo (admin é seed operacional e NÃO é removido)
        for (String id : List.of(ID_TUTOR_DEMO, ID_SUSPENSO_DEMO,
                                  ID_RECEPCAO_DEMO, ID_MEDICO_DEMO)) {
            usuarios.deleteById(id);
        }

        log.info("[DEMO] Modo demonstração desativado. Dados removidos.");
    }

    // ── Seed helpers ──────────────────────────────────────────────────────────

    private void seedUsuarios(String senha) {
        usuarios.save(UsuarioJpa.fromDomain(new UsuarioAutenticavel(
                ID_TUTOR_DEMO, "Tutor Demo",
                Perfil.TUTOR, senha, StatusConta.ATIVA,
                "123.456.789-00", "(11) 99999-0001",
                "Rua das Flores, 100 — São Paulo/SP", ID_TUTOR_DEMO)));

        usuarios.save(UsuarioJpa.fromDomain(new UsuarioAutenticavel(
                ID_SUSPENSO_DEMO, "Tutor Suspenso",
                Perfil.TUTOR, senha, StatusConta.SUSPENSA,
                "987.654.321-00", "(11) 99999-0002",
                "Av. Paulista, 1000 — São Paulo/SP", ID_SUSPENSO_DEMO)));

        usuarios.save(UsuarioJpa.fromDomain(new UsuarioAutenticavel(
                ID_RECEPCAO_DEMO, "Recepcionista Demo",
                Perfil.RECEPCIONISTA, senha, StatusConta.ATIVA)));

        usuarios.save(UsuarioJpa.fromDomain(new UsuarioAutenticavel(
                ID_MEDICO_DEMO, "Médico Demo",
                Perfil.MEDICO_VETERINARIO, senha, StatusConta.ATIVA)));
    }

    /** Adiciona {@code medicoId} à lista de médicos de todas as especialidades. */
    private void vincularMedicoAEspecialidades(String medicoId) {
        MedicoId medico = MedicoId.de(medicoId);
        especialidades.findAll().forEach(esp -> {
            List<MedicoId> lista = new ArrayList<>(esp.medicos());
            if (!lista.contains(medico)) {
                lista.add(medico);
                especialidades.save(EspecialidadeJpa.fromDomain(esp.toDomain(), lista));
            }
        });
    }

    /** Remove {@code medicoId} da lista de médicos de todas as especialidades. */
    private void desvincularMedicoDeEspecialidades(String medicoId) {
        MedicoId medico = MedicoId.de(medicoId);
        especialidades.findAll().forEach(esp -> {
            List<MedicoId> lista = new ArrayList<>(esp.medicos());
            if (lista.remove(medico)) {
                especialidades.save(EspecialidadeJpa.fromDomain(esp.toDomain(), lista));
            }
        });
    }

    private void seedPacientesEVacinas() {
        LocalDate hoje = LocalDate.now();

        Paciente rex  = novoPaciente(ID_TUTOR_DEMO, "Rex",  "Cão",  "Labrador", hoje.minusYears(3));
        Paciente miau = novoPaciente(ID_TUTOR_DEMO, "Miau", "Gato", "Persa",    hoje.minusYears(2));
        Paciente bob  = novoPaciente(ID_TUTOR_DEMO, "Bob",  "Cão",  "Beagle",   hoje.minusYears(5));

        // Rex — ciclo V10 em andamento + vacina em atraso
        salvarVacina(rex.id(), "V10",          1, 3, true,  hoje.minusMonths(4), "Dr. Carlos Silva",  "L12345");
        salvarVacina(rex.id(), "V10",          2, 3, true,  hoje.minusMonths(3), "Dr. Carlos Silva",  "L12346");
        salvarVacina(rex.id(), "Antirrábica",  null, null, true,  hoje.minusMonths(2), "Dra. Maria Santos", "R98765");
        salvarVacina(rex.id(), "V10",          3, 3, false, hoje.plusMonths(1),  null, null);  // PENDENTE
        salvarVacina(rex.id(), "Giardíase",    null, null, false, hoje.minusDays(20), null, null); // EM_ATRASO

        // Miau — vacina em atraso
        salvarVacina(miau.id(), "Antirrábica",      null, null, true,  hoje.minusMonths(6), "Dra. Maria Santos", "R55501");
        salvarVacina(miau.id(), "Quádrupla Felina",  1, 2, false, hoje.minusDays(10), null, null); // EM_ATRASO

        // Bob — tudo em dia
        salvarVacina(bob.id(), "V10",          1, 1, true, hoje.minusMonths(8), "Dr. Carlos Silva",  "L20011");
        salvarVacina(bob.id(), "Antirrábica",  null, null, true, hoje.minusMonths(5), "Dra. Maria Santos", "R30022");
    }

    private void seedCobrancas() {
        TutorId tutorDemo = TutorId.de(ID_TUTOR_DEMO);
        BigDecimal valor  = new BigDecimal(PlanosPadrao.VALOR_PLANO_BASICO_MENSAL);
        LocalDate hoje    = LocalDate.now();
        LocalDate dia5    = hoje.withDayOfMonth(5);
        LocalDate proxVenc = dia5.isAfter(hoje) ? dia5 : dia5.plusMonths(1);

        // PAGA (~2 meses atrás)
        LocalDate vencPago = proxVenc.minusMonths(2);
        cobrancas.save(CobrancaJpa.fromDomain(new Cobranca(
                CobrancaId.gerar(), tutorDemo,
                PlanosPadrao.ID_PLANO_BASICO_MENSAL,
                Competencia.de(YearMonth.from(vencPago).minusMonths(1)),
                valor, null, vencPago,
                vencPago.minusDays(2), BigDecimal.ZERO)));

        // EM_ATRASO (~1 mês atrás, não paga)
        LocalDate vencAtraso = proxVenc.minusMonths(1);
        cobrancas.save(CobrancaJpa.fromDomain(new Cobranca(
                CobrancaId.gerar(), tutorDemo,
                PlanosPadrao.ID_PLANO_BASICO_MENSAL,
                Competencia.de(YearMonth.from(vencAtraso).minusMonths(1)),
                valor, null, vencAtraso)));

        // PENDENTE (próximo vencimento)
        cobrancas.save(CobrancaJpa.fromDomain(new Cobranca(
                CobrancaId.gerar(), tutorDemo,
                PlanosPadrao.ID_PLANO_BASICO_MENSAL,
                Competencia.de(YearMonth.from(proxVenc).minusMonths(1)),
                valor, null, proxVenc)));
    }

    private Paciente novoPaciente(String tutorId, String nome, String especie,
                                  String raca, LocalDate nascimento) {
        String id = UUID.randomUUID().toString();
        Paciente p = new Paciente(id, tutorId, nome, especie, raca, nascimento);
        pacientes.save(PacienteJpa.fromDomain(p));
        return p;
    }

    private void salvarVacina(String pacienteId, String ciclo, Integer doseNum,
                               Integer totalDoses, boolean aplicada,
                               LocalDate data, String medico, String lote) {
        vacinas.save(VacinaJpa.fromDomain(new Vacina(
                UUID.randomUUID().toString(), pacienteId, ciclo,
                doseNum, totalDoses, aplicada, data, medico, lote)));
    }
}
