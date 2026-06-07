import { Navigate, Route, Routes } from "react-router-dom";
import { ProfileSelect } from "./pages/ProfileSelect";
import { TutorLogin } from "./pages/TutorLogin";
import { RecepcionistaLogin } from "./pages/RecepcionistaLogin";
import { MedicoLogin } from "./pages/MedicoLogin";
import { AdminLogin } from "./pages/AdminLogin";
import { ContratarPlano } from "./pages/ContratarPlano";
import { PagamentoPendente } from "./pages/PagamentoPendente";
import { AdminPanel } from "./pages/AdminPanel";
import { Dashboard } from "./pages/Dashboard";
import { TutorLayout } from "./pages/tutor/TutorLayout";
import { TutorInicio } from "./pages/tutor/TutorInicio";
import { TutorVacinacao } from "./pages/tutor/TutorVacinacao";
import { TutorBeneficios } from "./pages/tutor/TutorBeneficios";
import { TutorConquistas } from "./pages/tutor/TutorConquistas";
import { TutorFinanceiro } from "./pages/tutor/TutorFinanceiro";
import { TutorFinanceiroPagamento } from "./pages/tutor/TutorFinanceiroPagamento";
import { EmBreve } from "./pages/tutor/EmBreve";
import { AgendamentoHub } from "./pages/tutor/agendamento/AgendamentoHub";
import { NovaConsultaPage } from "./pages/tutor/agendamento/NovaConsultaPage";
import { AgendamentoRetornoPage } from "./pages/tutor/agendamento/AgendamentoRetornoPage";
import { ProtectedRoute } from "./auth/ProtectedRoute";
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
        <Route path="financeiro" element={<TutorFinanceiro />} />
        <Route path="financeiro/pagamento/:id" element={<TutorFinanceiroPagamento />} />
        <Route path="beneficios" element={<TutorBeneficios />} />
        <Route path="agendamentos" element={<AgendamentoHub />} />
        <Route path="agendamentos/nova-consulta" element={<NovaConsultaPage />} />
        <Route path="agendamentos/retorno" element={<AgendamentoRetornoPage />} />
        <Route path="conquistas" element={<TutorConquistas />} />
        <Route path="indicacoes" element={<EmBreve titulo="Indicações" />} />
        {/* F-03 — acompanhamento do protocolo pelo tutor (RN 15) */}
        <Route path="protocolos/:atendimentoId" element={<AcompanhamentoProtocoloPage />} />
      </Route>

      {/* Área de funcionários (recepcionista / médico) — placeholder por enquanto */}
      <Route
        path="/staff"
        element={
          <ProtectedRoute perfil={["RECEPCIONISTA", "MEDICO_VETERINARIO"]}>
            <Dashboard />
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin"
        element={
          <ProtectedRoute perfil="ADMIN_CLINICA">
            <AdminPanel />
          </ProtectedRoute>
        }
      />

      {/* F-03 — Protocolo de inacessibilidade: recepção (operacional) */}
      <Route
        path="/recepcao/protocolos"
        element={
          <ProtectedRoute perfil="RECEPCIONISTA">
            <PainelProtocolosAtivosPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/recepcao/protocolos/:protocoloId"
        element={
          <ProtectedRoute perfil="RECEPCIONISTA">
            <DetalheProtocoloPage />
          </ProtectedRoute>
        }
      />

      {/* F-03 — Protocolo de inacessibilidade: administração (configuração) */}
      <Route
        path="/admin/protocolos/configuracao"
        element={
          <ProtectedRoute perfil="ADMIN_CLINICA">
            <ConfiguracaoProtocoloPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/protocolos/configuracao/historico"
        element={
          <ProtectedRoute perfil="ADMIN_CLINICA">
            <HistoricoConfiguracaoPage />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
