# petCollar — Frontend (apresentacao-frontend)

SPA em React + Vite + TypeScript + Tailwind para a apresentação do sistema petCollar.

## Como rodar

```bash
cd Projeto/apresentacao-frontend/web
npm install
npm run dev
```

A SPA roda em http://localhost:5173 e proxya `/api/**` para o backend em http://localhost:8080.

## Estrutura

```
src/
  auth/         AuthContext (JWT) e ProtectedRoute
  components/   AuthLayout, Brand, PasswordInput
  pages/        ProfileSelect, TutorLogin, MatriculaLogin, RecepcionistaLogin, MedicoLogin, Dashboard
```

## Fluxos seguindo o protótipo

1. `/` — seleção de perfil (Tutor / Recepcionista / Médico Veterinário)
2. `/login/tutor` — e-mail + senha, com aviso de "conta suspensa" (HTTP 423 do backend)
3. `/login/recepcionista` — matrícula (6 dígitos) + senha
4. `/login/medico` — matrícula (6 dígitos) + senha
5. `/app` — placeholder pós-login com token JWT decodificado na tela
