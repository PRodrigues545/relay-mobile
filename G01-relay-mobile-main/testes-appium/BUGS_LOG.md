# Registo de Defeitos — Relay Mobile (testes Appium)

Este documento regista os defeitos encontrados pelos testes automáticos. Para cada um indica a causa e a correção proposta.
**Os defeitos são só identificados aqui e não foram corrigidos.**

| ID | Título | Requisito / Teste | Severidade | Estado |
|----|--------|-------------------|------------|--------|
| BUG-001 | Login com credenciais erradas devolve 500 e mostra erro genérico | RF01 / TC-LOG-02 | Média | Aberto |

---

## BUG-001 — Login com credenciais erradas devolve 500 e mostra erro genérico

| Campo | Valor |
|-------|-------|
| **Data** | 2026-10-30 |
| **Requisito** | RF01 — Fazer login (CA01.2) |
| **Caso de teste** | TC-LOG-02 Password incorreta (#1 e #2) |
| **Técnica** | Particionamento por Equivalência |
| **Componente** | Backend — `user-service` (autenticação) |
| **Severidade** | Média: não deixa entrar com credenciais erradas, mas não diz ao utilizador porquê e o servidor regista a falha como erro interno |
| **Prioridade** | Alta (correção pequena e localizada) |
| **Estado** | Aberto |

### Passos para reproduzir
1. Abrir a app no ecrã de Login.
2. Introduzir um email registado (`aluno1@ipca.pt`) e uma password errada (`errada123`).
3. Tocar em **Login**.

### Resultado esperado
- A app continua no ecrã de login.
- Aparece a mensagem **"Invalid email or password"**.
- O servidor responde **HTTP 401** (como documentado em `@apiError (401)` no `authController.js`).

### Resultado obtido
- A app continua no ecrã de login. ✔
- Aparece a mensagem **"Something went wrong"**. ✘
- O servidor responde **HTTP 500 "Internal Server Error"**. ✘

Evidência:
- Saída do Mocha: `AssertionError: devia mostrar erro de credenciais` (`login.test.js:39`), nos dois cenários.
- Dumps do ecrã: `_1_aluno1_ipca_pt_errada123_mostra_erro_e_fica_no_login.xml` e `_2_aluno2_ipca_pt_abc_mostra_erro_e_fica_no_login.xml` mostram um `TextView` com `text="Something went wrong"`.
- O mesmo acontece com um email que não existe (segundo ramo do mesmo código).

### Análise da causa

O erro passa por três camadas:

**1. `user-service/src/services/authService.js` (linhas 59–66)** lança estas mensagens:
```js
throw new Error('There is no user with this email');
throw new Error('Invalid password');
```

**2. `user-service/src/controllers/authController.js` (linhas 88–95)** só reconhece outra mensagem:
```js
if (error.message === 'Invalid username or password') {
    return res.status(400).json({ message: error.message });
}
console.error('Login error:', error);
return res.status(500).json({ message: 'Internal Server Error' });
```
As mensagens não coincidem. Por isso o erro cai sempre no ramo genérico e o servidor responde **500**.
Mesmo que coincidissem, o código devolvido seria **400**, quando o contrato documentado é **401**.

**3. App Android: `data/repository/AuthRepositoryImpl.kt` + `presentation/auth/login/LoginViewModel.kt`.** Esta camada está correta:
```kotlin
401  -> AuthException.InvalidCredentials   // → "Invalid email or password"
else -> AuthException.ServerError(code)     // → "Something went wrong"
```
A app só mostra a mensagem de credenciais quando recebe 401. Como recebe 500, mostra o erro genérico.

**Conclusão:** o defeito está só no backend. A app não precisa de alterações.

### Correção proposta (não aplicada)

`user-service/src/services/authService.js`: usar a mesma mensagem nos dois casos.
```js
if (!user) {
    throw new Error('Invalid email or password');
}
const isMatch = await bcrypt.compare(password, user.password);
if (!isMatch) {
    throw new Error('Invalid email or password');
}
```

`user-service/src/controllers/authController.js`: reconhecer essa mensagem e devolver 401.
```js
if (error.message === 'Invalid email or password') {
    return res.status(401).json({ message: error.message });
}
```

Nota de segurança: a mensagem atual "There is no user with this email" revela que emails estão registados (*user enumeration*). Com uma mensagem única para os dois casos, o login deixa de revelar se a conta existe.

### Verificação após correção
1. Pedido direto à API:
   ```powershell
   curl.exe -i -X POST http://<host>:<porta>/api/auth/login -H "Content-Type: application/json" -d '{\"email\":\"aluno1@ipca.pt\",\"password\":\"errada123\"}'
   ```
   Esperado: `HTTP/1.1 401` com `{"message":"Invalid email or password"}`.
2. `npm run test:login`: o TC-LOG-02 #1 e #2 devem passar, e o TC-LOG-01, TC-LOG-03 e TC-LOG-04 devem continuar a passar.

---

## Modelo para novos defeitos

```markdown
## BUG-XXX — <título curto>

| Campo | Valor |
|-------|-------|
| **Data** | AAAA-MM-DD |
| **Requisito** | RFxx — ... (CAxx.x) |
| **Caso de teste** | TC-XXX-xx |
| **Técnica** | ... |
| **Componente** | App / Backend — <serviço> |
| **Severidade** | Crítica / Alta / Média / Baixa |
| **Prioridade** | Alta / Média / Baixa |
| **Estado** | Aberto |

### Passos para reproduzir
### Resultado esperado
### Resultado obtido
### Análise da causa
### Correção proposta (não aplicada)
### Verificação após correção
```