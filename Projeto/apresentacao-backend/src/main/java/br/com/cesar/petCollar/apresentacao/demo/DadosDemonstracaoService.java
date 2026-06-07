package br.com.cesar.petCollar.apresentacao.demo;

import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.Perfil;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.StatusConta;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioAutenticavel;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioJpa;
import br.com.cesar.petCollar.apresentacao.IdentidadeAcesso.UsuarioJpaRepository;
import br.com.cesar.petCollar.apresentacao.PortalTutor.Paciente;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpa;
import br.com.cesar.petCollar.apresentacao.PortalTutor.PacienteJpaRepository;
import br.com.cesar.petCollar.aplicacao.AssinaturaFaturamento.PlanosPadrao;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Cobranca;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.CobrancaId;
import br.com.cesar.petCollar.dominio.AssinaturaFaturamento.cobranca.Competencia;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.CicloVacinal;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.TipoProtocolo;
import br.com.cesar.petCollar.dominio.SaudePreventiva.vacinal.VacinaId;
import br.com.cesar.petCollar.dominio.compartilhado.MedicoId;
import br.com.cesar.petCollar.dominio.compartilhado.PacienteId;
import br.com.cesar.petCollar.dominio.compartilhado.TutorId;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.EspecialidadeJpa;
import br.com.cesar.petCollar.infraestrutura.AgendamentoClinico.EspecialidadeJpaRepository;
import br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento.CobrancaJpa;
import br.com.cesar.petCollar.infraestrutura.AssinaturaFaturamento.CobrancaJpaRepository;
import br.com.cesar.petCollar.infraestrutura.SaudePreventiva.CicloVacinalJpa;
import br.com.cesar.petCollar.infraestrutura.SaudePreventiva.CicloVacinalJpaRepository;

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
    private final CicloVacinalJpaRepository ciclosVacinais;
    private final EspecialidadeJpaRepository especialidades;
    private final CobrancaJpaRepository cobrancas;
    private final PasswordEncoder encoder;

    public DadosDemonstracaoService(
            UsuarioJpaRepository usuarios,
            PacienteJpaRepository pacientes,
            CicloVacinalJpaRepository ciclosVacinais,
            EspecialidadeJpaRepository especialidades,
            CobrancaJpaRepository cobrancas,
            PasswordEncoder encoder) {
        this.usuarios       = usuarios;
        this.pacientes      = pacientes;
        this.ciclosVacinais = ciclosVacinais;
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
        seedPacientesECiclosVacinais();
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
        cobrancas.deleteAll(cobrancas.findByTutorIdOrderByVencimentoDesc(ID_TUTOR_DEMO));
        ciclosVacinais.deleteAll();
        pacientes.deleteAll();
        desvincularMedicoDeEspecialidades(ID_MEDICO_DEMO);
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

    private void desvincularMedicoDeEspecialidades(String medicoId) {
        MedicoId medico = MedicoId.de(medicoId);
        especialidades.findAll().forEach(esp -> {
            List<MedicoId> lista = new ArrayList<>(esp.medicos());
            if (lista.remove(medico)) {
                especialidades.save(EspecialidadeJpa.fromDomain(esp.toDomain(), lista));
            }
        });
    }

    private void seedPacientesECiclosVacinais() {
        LocalDate hoje = LocalDate.now();

        Paciente rex  = novoPaciente(ID_TUTOR_DEMO, "Rex",  "Cão",  "Labrador", hoje.minusYears(3));
        Paciente miau = novoPaciente(ID_TUTOR_DEMO, "Miau", "Gato", "Persa",    hoje.minusYears(2));
        Paciente bob  = novoPaciente(ID_TUTOR_DEMO, "Bob",  "Cão",  "Beagle",   hoje.minusYears(5));

        // Rex — ciclo V10 em andamento (2 aplicadas + 1 pendente) + Antirrábica + Giardíase em atraso
        CicloVacinal v10Rex = criarCiclo(rex.id(), "V10", 3, TipoProtocolo.FILHOTE, null);
        v10Rex.adicionarPrimeiraDose(hoje.minusMonths(4));
        v10Rex.getDoses().get(0).aplicar(hoje.minusMonths(4), "Dr. Carlos Silva", "L12345");
        v10Rex.adicionarProximaDose(hoje.minusMonths(3));
        v10Rex.getDoses().get(1).aplicar(hoje.minusMonths(3), "Dr. Carlos Silva", "L12346");
        v10Rex.adicionarProximaDose(hoje.plusMonths(1));
        salvarCiclo(v10Rex);

        CicloVacinal antirrabicaRex = criarCiclo(rex.id(), "Antirrábica", 1, TipoProtocolo.REFORCO_ANUAL, null);
        antirrabicaRex.adicionarPrimeiraDose(hoje.minusMonths(2));
        antirrabicaRex.getDoses().get(0).aplicar(hoje.minusMonths(2), "Dra. Maria Santos", "R98765");
        salvarCiclo(antirrabicaRex);

        CicloVacinal giardiaseRex = criarCiclo(rex.id(), "Giardíase", 1, TipoProtocolo.REFORCO_ANUAL, null);
        giardiaseRex.adicionarPrimeiraDose(hoje.minusDays(20));
        salvarCiclo(giardiaseRex);

        // Miau — Antirrábica aplicada + Quádrupla Felina em atraso
        CicloVacinal antirrabicaMiau = criarCiclo(miau.id(), "Antirrábica", 1, TipoProtocolo.REFORCO_ANUAL, null);
        antirrabicaMiau.adicionarPrimeiraDose(hoje.minusMonths(6));
        antirrabicaMiau.getDoses().get(0).aplicar(hoje.minusMonths(6), "Dra. Maria Santos", "R55501");
        salvarCiclo(antirrabicaMiau);

        CicloVacinal quadruplaFelina = criarCiclo(miau.id(), "Quádrupla Felina", 2, TipoProtocolo.FILHOTE, null);
        quadruplaFelina.adicionarPrimeiraDose(hoje.minusDays(10));
        salvarCiclo(quadruplaFelina);

        // Bob — tudo em dia
        CicloVacinal v10Bob = criarCiclo(bob.id(), "V10", 1, TipoProtocolo.REFORCO_ANUAL, null);
        v10Bob.adicionarPrimeiraDose(hoje.minusMonths(8));
        v10Bob.getDoses().get(0).aplicar(hoje.minusMonths(8), "Dr. Carlos Silva", "L20011");
        salvarCiclo(v10Bob);

        CicloVacinal antirrabicaBob = criarCiclo(bob.id(), "Antirrábica", 1, TipoProtocolo.REFORCO_ANUAL, null);
        antirrabicaBob.adicionarPrimeiraDose(hoje.minusMonths(5));
        antirrabicaBob.getDoses().get(0).aplicar(hoje.minusMonths(5), "Dra. Maria Santos", "R30022");
        salvarCiclo(antirrabicaBob);
    }

    private void seedCobrancas() {
        TutorId tutorDemo = TutorId.de(ID_TUTOR_DEMO);
        BigDecimal valor  = new BigDecimal(PlanosPadrao.VALOR_PLANO_BASICO_MENSAL);
        LocalDate hoje    = LocalDate.now();
        LocalDate dia5    = hoje.withDayOfMonth(5);
        LocalDate proxVenc = dia5.isAfter(hoje) ? dia5 : dia5.plusMonths(1);

        LocalDate vencPago = proxVenc.minusMonths(2);
        cobrancas.save(CobrancaJpa.fromDomain(new Cobranca(
                CobrancaId.gerar(), tutorDemo,
                PlanosPadrao.ID_PLANO_BASICO_MENSAL,
                Competencia.de(YearMonth.from(vencPago).minusMonths(1)),
                valor, null, vencPago,
                vencPago.minusDays(2), BigDecimal.ZERO)));

        LocalDate vencAtraso = proxVenc.minusMonths(1);
        cobrancas.save(CobrancaJpa.fromDomain(new Cobranca(
                CobrancaId.gerar(), tutorDemo,
                PlanosPadrao.ID_PLANO_BASICO_MENSAL,
                Competencia.de(YearMonth.from(vencAtraso).minusMonths(1)),
                valor, null, vencAtraso)));

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

    private CicloVacinal criarCiclo(String pacienteId, String nome, int totalDoses,
                                     TipoProtocolo protocolo, Integer intervaloDias) {
        return new CicloVacinal(VacinaId.gerar(), PacienteId.de(pacienteId),
            nome, totalDoses, protocolo, intervaloDias);
    }

    private void salvarCiclo(CicloVacinal ciclo) {
        ciclosVacinais.save(CicloVacinalJpa.fromDomain(ciclo));
    }
}
