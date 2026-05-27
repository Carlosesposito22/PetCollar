import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { BrandWordmark } from "./Brand";

type Props = {
  children: ReactNode;
  showBack?: boolean;
  highlight?: ReactNode;
};

export function AuthLayout({ children, showBack = false, highlight }: Props) {
  return (
    <div className="min-h-screen lg:grid lg:grid-cols-2">
      <aside className="relative hidden overflow-hidden bg-gradient-to-br from-brand-700 via-brand-600 to-brand-500 text-white lg:flex lg:flex-col lg:justify-between lg:p-12">
        <div className="absolute -right-24 -top-24 h-80 w-80 rounded-full bg-white/10 blur-2xl" />
        <div className="absolute -bottom-32 -left-20 h-96 w-96 rounded-full bg-black/10 blur-3xl" />

        <div className="relative">
          <BrandWordmark tone="light" />
        </div>

        <div className="relative max-w-md">
          <h1 className="text-3xl font-bold leading-tight">
            Fortalecendo o vínculo entre pessoas e animais.
          </h1>
          <p className="mt-4 text-white/90">
            Atendimento clínico mais seguro, do balcão à prescrição: priorize casos
            críticos, monitore SLAs e bloqueie dosagens inseguras — em um único fluxo.
          </p>

          {highlight ?? (
            <ul className="mt-8 space-y-3 text-sm text-white/95">
              <Bullet>Triagem por escore com cor de risco</Bullet>
              <Bullet>Fila inteligente por gravidade e SLA</Bullet>
              <Bullet>Cálculo de NEM e dosagem segura</Bullet>
            </ul>
          )}
        </div>

        <div className="relative text-xs text-white/70">
          © {new Date().getFullYear()} petCollar — Viva a experiência Pet Collar
        </div>
      </aside>

      <main className="flex min-h-screen flex-col">
        <header className="flex items-center justify-between px-6 pt-6 lg:hidden">
          <BrandWordmark />
        </header>

        <div className="flex flex-1 items-center justify-center px-6 py-10">
          <div className="w-full max-w-md">
            {showBack && (
              <Link
                to="/"
                className="mb-6 inline-flex items-center gap-1 text-sm font-medium text-ink-500 hover:text-ink-800"
              >
                <ArrowLeft /> Voltar
              </Link>
            )}
            {children}
          </div>
        </div>
      </main>
    </div>
  );
}

function Bullet({ children }: { children: ReactNode }) {
  return (
    <li className="flex items-start gap-2">
      <span className="mt-1 inline-flex h-4 w-4 flex-none items-center justify-center rounded-full bg-white/20">
        <svg viewBox="0 0 12 12" className="h-2.5 w-2.5" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M2 6.5L5 9.5L10 3.5" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </span>
      <span>{children}</span>
    </li>
  );
}

function ArrowLeft() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 12H5" />
      <path d="M12 19l-7-7 7-7" />
    </svg>
  );
}
