package br.com.cesar.petCollar.infraestrutura.Farmacovigilancia;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ConsultarPrescricaoUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.CriarEFinalizarPrescricaoUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ListarCatalogoMedicamentosUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ListarTemplatesPrescricaoUseCase;
import br.com.cesar.petCollar.aplicacao.Farmacovigilancia.ValidarPrescricaoUseCase;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Frequencia;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.IMedicamentoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.InteracaoMedicamentosa;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ManejoAlimentar;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.Medicamento;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.MedicamentoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.catalogo.ViaAdministracao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.prescricao.IPrescricaoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.ITemplatePrescricaoRepositorio;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.TemplatePrescricao;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.template.TemplatePrescricaoId;
import br.com.cesar.petCollar.dominio.Farmacovigilancia.validacao.ValidadorPrescricao;

@Configuration
@EntityScan(basePackages = "br.com.cesar.petCollar.infraestrutura.Farmacovigilancia")
@EnableJpaRepositories(basePackages = "br.com.cesar.petCollar.infraestrutura.Farmacovigilancia")
public class FarmacovigilanciaConfig {

    @Bean
    public ValidadorPrescricao validadorPrescricao(IMedicamentoRepositorio medicamentos) {
        return new ValidadorPrescricao(medicamentos);
    }

    @Bean
    public ValidarPrescricaoUseCase validarPrescricaoUseCase(ValidadorPrescricao v) {
        return new ValidarPrescricaoUseCase(v);
    }

    @Bean
    public CriarEFinalizarPrescricaoUseCase criarEFinalizarPrescricaoUseCase(
            IMedicamentoRepositorio meds, IPrescricaoRepositorio prescs, ValidadorPrescricao v) {
        return new CriarEFinalizarPrescricaoUseCase(meds, prescs, v);
    }

    @Bean
    public ListarCatalogoMedicamentosUseCase listarCatalogoMedicamentosUseCase(IMedicamentoRepositorio r) {
        return new ListarCatalogoMedicamentosUseCase(r);
    }

    @Bean
    public ListarTemplatesPrescricaoUseCase listarTemplatesPrescricaoUseCase(ITemplatePrescricaoRepositorio r) {
        return new ListarTemplatesPrescricaoUseCase(r);
    }

    @Bean
    public ConsultarPrescricaoUseCase consultarPrescricaoUseCase(IPrescricaoRepositorio r) {
        return new ConsultarPrescricaoUseCase(r);
    }

    @Bean
    public CommandLineRunner seedFarmacovigilancia(IMedicamentoRepositorio repositorio,
                                                   ITemplatePrescricaoRepositorio templates) {
        return args -> {
            seedMedicamentos(repositorio);
            seedTemplates(repositorio, templates);
        };
    }

    private void seedMedicamentos(IMedicamentoRepositorio repositorio) {
        if (repositorio.contar() > 0) return;

            Map<String, MedicamentoId> ids = new HashMap<>();

            ids.put("omeprazol", criar(repositorio, "Omeprazol",
                    "2.0", "4.0", EnumSet.of(ViaAdministracao.ORAL),
                    Set.of("omeprazol", "benzimidazol"), ManejoAlimentar.JEJUM,
                    "Administrar 30min antes da refeição."));
            ids.put("metoclopramida", criar(repositorio, "Metoclopramida",
                    "0.5", "5.0", EnumSet.of(ViaAdministracao.ORAL, ViaAdministracao.SUBCUTANEA),
                    Set.of("metoclopramida"), ManejoAlimentar.INDIFERENTE,
                    "Evitar uso prolongado — risco de sintomas extrapiramidais."));
            ids.put("maropitant", criar(repositorio, "Maropitant",
                    "2.0", "10.0", EnumSet.of(ViaAdministracao.ORAL, ViaAdministracao.SUBCUTANEA),
                    Set.of("maropitant"), ManejoAlimentar.INDIFERENTE,
                    "1x ao dia por no máximo 5 dias consecutivos."));
            ids.put("amoxicilina_clavulanato", criar(repositorio, "Amoxicilina + Clavulanato",
                    "25.0", "50.0", EnumSet.of(ViaAdministracao.ORAL),
                    Set.of("amoxicilina", "clavulanato", "penicilina"), ManejoAlimentar.COM_ALIMENTO,
                    "Administrar com alimento para reduzir desconforto gástrico."));
            ids.put("cefalexina", criar(repositorio, "Cefalexina",
                    "30.0", "50.0", EnumSet.of(ViaAdministracao.ORAL),
                    Set.of("cefalexina", "cefalosporinas"), ManejoAlimentar.INDIFERENTE,
                    "Completar o ciclo mesmo após melhora dos sintomas."));
            ids.put("carprofeno", criar(repositorio, "Carprofeno",
                    "4.0", "25.0", EnumSet.of(ViaAdministracao.ORAL, ViaAdministracao.SUBCUTANEA),
                    Set.of("carprofeno", "aine"), ManejoAlimentar.COM_ALIMENTO,
                    "AINE — evitar em pacientes com insuficiência renal."));
            ids.put("dipirona", criar(repositorio, "Dipirona",
                    "25.0", "500.0", EnumSet.of(ViaAdministracao.ORAL, ViaAdministracao.INTRAVENOSA),
                    Set.of("dipirona", "metamizol"), ManejoAlimentar.INDIFERENTE,
                    "Monitorar sinais de hipotensão em uso IV."));
            ids.put("tramadol", criar(repositorio, "Tramadol",
                    "5.0", "50.0", EnumSet.of(ViaAdministracao.ORAL),
                    Set.of("tramadol"), ManejoAlimentar.INDIFERENTE,
                    "Pode causar sedação leve nos primeiros dias."));
            ids.put("furosemida", criar(repositorio, "Furosemida",
                    "4.0", "10.0", EnumSet.of(ViaAdministracao.ORAL, ViaAdministracao.INTRAVENOSA),
                    Set.of("furosemida"), ManejoAlimentar.INDIFERENTE,
                    "Diurético — manter hidratação e monitorar eletrólitos."));
            ids.put("enalapril", criar(repositorio, "Enalapril",
                    "0.5", "5.0", EnumSet.of(ViaAdministracao.ORAL),
                    Set.of("enalapril", "ieca"), ManejoAlimentar.INDIFERENTE,
                    "Verificar função renal antes do início do tratamento."));
            ids.put("prednisolona", criar(repositorio, "Prednisolona",
                    "2.0", "5.0", EnumSet.of(ViaAdministracao.ORAL),
                    Set.of("prednisolona", "corticoide"), ManejoAlimentar.COM_ALIMENTO,
                    "Reduzir dose gradualmente ao suspender — risco de insuficiência adrenal."));
            ids.put("sucralfato", criar(repositorio, "Sucralfato",
                    "1.0", "100.0", EnumSet.of(ViaAdministracao.ORAL),
                    Set.of("sucralfato"), ManejoAlimentar.JEJUM,
                    "Administrar pelo menos 2h antes de outros medicamentos orais."));

            repositorio.registrarInteracao(new InteracaoMedicamentosa(
                    ids.get("omeprazol"), ids.get("sucralfato"),
                    InteracaoMedicamentosa.Gravidade.MODERADA,
                    "Sucralfato reduz a absorção de omeprazol. Separar administração em pelo menos 2h."));
            repositorio.registrarInteracao(new InteracaoMedicamentosa(
                    ids.get("carprofeno"), ids.get("prednisolona"),
                    InteracaoMedicamentosa.Gravidade.GRAVE,
                    "Combinação de AINE + corticoide aumenta drasticamente o risco de úlcera GI e nefrotoxicidade."));
            repositorio.registrarInteracao(new InteracaoMedicamentosa(
                    ids.get("furosemida"), ids.get("enalapril"),
                    InteracaoMedicamentosa.Gravidade.MODERADA,
                    "Risco aumentado de hipotensão e insuficiência renal aguda — monitorar pressão e creatinina."));
            repositorio.registrarInteracao(new InteracaoMedicamentosa(
                    ids.get("metoclopramida"), ids.get("tramadol"),
                    InteracaoMedicamentosa.Gravidade.MODERADA,
                    "Ambos atuam no SNC — risco de sedação aumentada e síndrome serotoninérgica."));
            repositorio.registrarInteracao(new InteracaoMedicamentosa(
                    ids.get("amoxicilina_clavulanato"), ids.get("cefalexina"),
                    InteracaoMedicamentosa.Gravidade.GRAVE,
                    "Dois beta-lactâmicos concomitantes não trazem benefício clínico e potencializam toxicidade."));
            repositorio.registrarInteracao(new InteracaoMedicamentosa(
                    ids.get("carprofeno"), ids.get("furosemida"),
                    InteracaoMedicamentosa.Gravidade.MODERADA,
                    "AINE reduz a eficácia do diurético e aumenta risco de nefrotoxicidade."));
    }

    private void seedTemplates(IMedicamentoRepositorio medicamentos,
                               ITemplatePrescricaoRepositorio templates) {
        if (templates.contar() > 0) return;

        Map<String, MedicamentoId> porNome = new HashMap<>();
        for (Medicamento m : medicamentos.listarTodos())
            porNome.put(m.getNome(), m.getId());

        templates.salvar(new TemplatePrescricao(
                TemplatePrescricaoId.gerar(),
                "Gastroproteção Básica",
                "Omeprazol + Sucralfato (esofagite/gastrite leve a moderada).",
                List.of(
                        new TemplatePrescricao.ItemTemplate(
                                porNome.get("Omeprazol"), new BigDecimal("0.5"), 7,
                                Frequencia.UMA_VEZ_DIA, ViaAdministracao.ORAL),
                        new TemplatePrescricao.ItemTemplate(
                                porNome.get("Sucralfato"), new BigDecimal("0.5"), 7,
                                Frequencia.DUAS_VEZES_DIA, ViaAdministracao.ORAL))));

        templates.salvar(new TemplatePrescricao(
                TemplatePrescricaoId.gerar(),
                "Antiemético Padrão",
                "Metoclopramida + Maropitant (vômitos persistentes).",
                List.of(
                        new TemplatePrescricao.ItemTemplate(
                                porNome.get("Metoclopramida"), new BigDecimal("0.3"), 5,
                                Frequencia.DUAS_VEZES_DIA, ViaAdministracao.ORAL),
                        new TemplatePrescricao.ItemTemplate(
                                porNome.get("Maropitant"), new BigDecimal("1.0"), 5,
                                Frequencia.UMA_VEZ_DIA, ViaAdministracao.SUBCUTANEA))));

        templates.salvar(new TemplatePrescricao(
                TemplatePrescricaoId.gerar(),
                "Antibiótico Amplo Espectro",
                "Amoxicilina + Clavulanato — primeira escolha para infecções de pele e tecidos moles.",
                List.of(
                        new TemplatePrescricao.ItemTemplate(
                                porNome.get("Amoxicilina + Clavulanato"), new BigDecimal("12.5"), 10,
                                Frequencia.DUAS_VEZES_DIA, ViaAdministracao.ORAL))));
    }

    private static MedicamentoId criar(IMedicamentoRepositorio repo, String nome,
                                        String doseMax, String concentracao,
                                        Set<ViaAdministracao> vias,
                                        Set<String> componentes, ManejoAlimentar manejo,
                                        String nota) {
        Medicamento m = new Medicamento(MedicamentoId.gerar(), nome,
                new BigDecimal(doseMax), new BigDecimal(concentracao),
                vias, componentes, manejo, nota);
        repo.salvar(m);
        return m.getId();
    }
}
