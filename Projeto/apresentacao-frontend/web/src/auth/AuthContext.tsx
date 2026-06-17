import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";

export type Perfil = "TUTOR" | "RECEPCIONISTA" | "MEDICO_VETERINARIO" | "ADMIN_CLINICA";

export type AuthUser = {
  identificador: string;
  perfil: Perfil;
  nome?: string;
};

export type AuthSession = {
  token: string;
  user: AuthUser;
  expiraEm?: number;
};

export class LoginError extends Error {
  code: string;
  contaId?: string;
  constructor(message: string, code: string, contaId?: string) {
    super(message);
    this.code = code;
    this.contaId = contaId;
  }
}

type AuthState = {
  session: AuthSession | null;
  login: (perfil: Perfil, identificador: string, senha: string) => Promise<void>;
  logout: () => void;
  apiFetch: (input: string, init?: RequestInit) => Promise<Response>;
};

const STORAGE_KEY = "petcollar.session";
const AuthCtx = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AuthSession;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    if (session) localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    else localStorage.removeItem(STORAGE_KEY);
  }, [session]);

  const login = useCallback<AuthState["login"]>(async (perfil, identificador, senha) => {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ perfil, identificador, senha }),
    });

    if (!res.ok) {
      const body = await res.json().catch(
        () => ({} as { mensagem?: string; status?: string; contaId?: string })
      );
      const status = res.status;
      const code = body?.status ?? `HTTP_${status}`;
      const mensagem =
        body?.mensagem ??
        (status === 401
          ? "Credenciais inválidas."
          : status === 402
            ? "Acesso será liberado somente após confirmação do pagamento."
            : status === 423
              ? "Conta suspensa. Entre em contato com o suporte."
              : "Falha ao autenticar. Tente novamente.");
      throw new LoginError(mensagem, code, body?.contaId);
    }

    const data = (await res.json()) as AuthSession;
    setSession(data);
  }, []);

  const logout = useCallback(() => setSession(null), []);

  const apiFetch = useCallback<AuthState["apiFetch"]>(async (input, init = {}) => {
    const headers = new Headers(init.headers);
    if (session?.token) headers.set("Authorization", `Bearer ${session.token}`);
    if (init.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
    const res = await fetch(input, { ...init, headers });
    // Token expirado ou inválido: desloga para forçar novo login.
    if (res.status === 401 && session?.token) {
      setSession(null);
    }
    return res;
  }, [session]);

  const value = useMemo(
    () => ({ session, login, logout, apiFetch }),
    [session, login, logout, apiFetch]
  );
  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthCtx);
  if (!ctx) throw new Error("useAuth deve ser usado dentro de <AuthProvider>");
  return ctx;
}
