import { Navigate, Route, Routes } from "react-router-dom";
import { ProfileSelect } from "./pages/ProfileSelect";
import { TutorLogin } from "./pages/TutorLogin";
import { RecepcionistaLogin } from "./pages/RecepcionistaLogin";
import { MedicoLogin } from "./pages/MedicoLogin";
import { AdminLogin } from "./pages/AdminLogin";
import { ContratarPlano } from "./pages/ContratarPlano";
import { PagamentoPendente } from "./pages/PagamentoPendente";
import { AdminPanel } from "./pages/AdminPanel";
import { AdminCatalogoRacoes } from "./pages/admin/AdminCatalogoRacoes";
import { Dashboard } from "./pages/Dashboard";
import { TutorLayout } from "./pages/tutor/TutorLayout";
import { RecepcionistaLayout } from "./pages/recepcionista/RecepcionistaLayout";
import { AdminLayout } from "./pages/admin/AdminLayout";
import { MedicoLayout } from "./pages/medico/MedicoLayout";
import { MedicoPainel } from "./pages/medico/MedicoPainel";
import { MedicoProntuario } from "./pages/medico/MedicoProntuario";
import { MedicoRelatorio } from "./pages/medico/MedicoRelatorio";
import { MedicoVacinacao } from "./pages/medico/MedicoVacinacao";
import { MedicoGestaoNutricional } from "./pages/medico/MedicoGestaoNutricional";
import { MedicoFarmacovigilancia } from "./pages/medico/MedicoFarmacovigilancia";
import { TutorInicio } from "./pages/tutor/TutorInicio";
import { TutorNutricao } from "./pages/tutor/TutorNutricao";
import { TutorMedicamentos } from "./pages/tutor/TutorMedicamentos";
import { TutorVacinacao } from "./pages/tutor/TutorVacinacao";
import { TutorBeneficios } from "./pages/tutor/TutorBeneficios";
import { TutorConquistas } from "./pages/tutor/TutorConquistas";
import { TutorFinanceiro } from "./pages/tutor/TutorFinanceiro";
import { TutorFinanceiroPagamento } from "./pages/tutor/TutorFinanceiroPagamento";
import { TutorIndicacoes } from "./pages/tutor/TutorIndicacoes";
import { LandingIndicacao } from "./pages/LandingIndicacao";
import { AgendamentoHub } from "./pages/tutor/agendamento/AgendamentoHub";
import { NovaConsultaPage } from "./pages/tutor/agendamento/NovaConsultaPage";
import { AgendamentoRetornoPage } from "./pages/tutor/agendamento/AgendamentoRetornoPage";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { BuscaTutorPage } from "./features/recepcao-triagem/pages/BuscaTutorPage";
import { FilaEsperaPage } from "./features/recepcao-triagem/pages/FilaEsperaPage";
import { AcompanhamentoProtocoloPage } from "./features/protocolo-inacessibilidade/pages/tutor/AcompanhamentoProtocoloPage";
import { PainelProtocolosAtivosPage } from "./features/protocolo-inacessibilidade/pages/recepcionista/PainelProtocolosAtivosPage";
import { DetalheProtocoloPage } from "./features/protocolo-inacessibilidade/pages/recepcionista/DetalheProtocoloPage";
import { ConfiguracaoProtocoloPage } from "./features/protocolo-inacessibilidade/pages/administrador/ConfiguracaoProtocoloPage";
import { HistoricoConfiguracaoPage } from "./features/protocolo-inacessibilidade/pages/administrador/HistoricoConfiguracaoPage";

export function App() {
  return (
    <Routes>
      <Route path="/" element={<ProfileSelect />} />
      <Route path="/login/tutor" element={<TutorLogin />} />
      <Route path="/login/recepcionista" element={<RecepcionistaLogin />} />
      <Route path="/login/medico" element={<MedicoLogin />} />
      <Route path="/login/admin" element={<AdminLogin />} />
      <Route path="/contratar-plano" element={<ContratarPlano />} />
      <Route path="/indicacao/:codigo" element={<LandingIndicacao />} />
      <Route path="/pagamento-pendente" element={<PagamentoPendente />} />

      {/* Área do Tutor */}
      <Route
        path="/app"
        element={
          <ProtectedRoute perfil="TUTOR">
            <TutorLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<TutorInicio />} />
        <Route path="vacinacao" element={<TutorVacinacao />} />
        <Route path="nutricao" element={<TutorNutricao />} />
        <Route path="medicamentos" element={<TutorMedicamentos />} />
        <Route path="financeiro" element={<TutorFinanceiro />} />
        <Route path="financeiro/pagamento/:id" element={<TutorFinanceiroPagamento />} />
        <Route path="beneficios" element={<TutorBeneficios />} />
        <Route path="agendamentos" element={<AgendamentoHub />} />
        <Route path="agendamentos/nova-consulta" element={<NovaConsultaPage />} />
        <Route path="agendamentos/retorno" element={<AgendamentoRetornoPage />} />
        <Route path="conquistas" element={<TutorConquistas />} />
        <Route path="indicacoes" element={<TutorIndicacoes />} />
        {/* F-03 — acompanhamento do protocolo pelo tutor (RN 15) */}
        <Route path="protocolos/:atendimentoId" element={<AcompanhamentoProtocoloPage />} />
      </Route>

      {/* Área da Recepcionista */}
      <Route path="/staff" element={<Navigate to="/recepcao" replace />} />
      <Route
        path="/recepcao"
        element={
          <ProtectedRoute perfil="RECEPCIONISTA">
            <RecepcionistaLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="busca-tutor" element={<BuscaTutorPage />} />
        <Route path="fila-espera" element={<FilaEsperaPage />} />
        {/* F-03 — Protocolo de inacessibilidade: recepção (operacional) */}
        <Route path="protocolos" element={<PainelProtocolosAtivosPage />} />
        <Route path="protocolos/:protocoloId" element={<DetalheProtocoloPage />} />
      </Route>

      {/* Área do Médico Veterinário */}
      <Route
        path="/medico"
        element={
          <ProtectedRoute perfil="MEDICO_VETERINARIO">
            <MedicoLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<MedicoPainel />} />
        <Route path="prontuario/:pacienteId" element={<MedicoProntuario />} />
        <Route path="prontuario/:pacienteId/relatorio" element={<MedicoRelatorio />} />
        <Route path="prontuario/:pacienteId/vacinacao" element={<MedicoVacinacao />} />
        <Route path="prontuario/:pacienteId/nutricao" element={<MedicoGestaoNutricional />} />
        <Route path="prontuario/:pacienteId/farmacovigilancia" element={<MedicoFarmacovigilancia />} />
      </Route>

      {/* Área do Admin */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute perfil="ADMIN_CLINICA">
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<AdminPanel />} />
        {/* F-03 — Protocolo de inacessibilidade: administração (configuração) */}
        <Route path="protocolos/configuracao" element={<ConfiguracaoProtocoloPage />} />
        <Route path="protocolos/configuracao/historico" element={<HistoricoConfiguracaoPage />} />
        {/* F-11 — Admin: CRUD do catálogo de rações */}
        <Route path="catalogo-racoes" element={<AdminCatalogoRacoes />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
