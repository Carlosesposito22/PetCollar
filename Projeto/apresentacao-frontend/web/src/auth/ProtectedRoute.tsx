import { Navigate } from "react-router-dom";
import { useAuth, type Perfil } from "./AuthContext";
import type { ReactNode } from "react";

type Props = {
  children: ReactNode;
  perfil?: Perfil | Perfil[];
};

export function ProtectedRoute({ children, perfil }: Props) {
  const { session } = useAuth();
  if (!session) return <Navigate to="/" replace />;

  if (perfil) {
    const permitidos = Array.isArray(perfil) ? perfil : [perfil];
    if (!permitidos.includes(session.user.perfil)) {
      return <Navigate to="/" replace />;
    }
  }
  return <>{children}</>;
}
