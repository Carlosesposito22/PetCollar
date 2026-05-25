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
import { ProtectedRoute } from "./auth/ProtectedRoute";

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
      <Route
        path="/app"
        element={
          <ProtectedRoute perfil={["TUTOR", "RECEPCIONISTA", "MEDICO_VETERINARIO"]}>
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
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
