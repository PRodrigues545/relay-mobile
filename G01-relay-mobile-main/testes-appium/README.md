# Registo de Defeitos — Relay Mobile (testes Appium)

Este documento regista os defeitos encontrados pelos testes automáticos. Para cada um indica a causa e a correção proposta.
**Os defeitos são só identificados aqui e não foram corrigidos.**

| ID | Título | Requisito / Teste | Severidade | Estado |
|----|--------|-------------------|------------|--------|
| BUG-001 | Login com credenciais erradas devolve 500 e mostra erro genérico | RF01 / TC-LOG-02 | Média | Aberto |
| BUG-002 | Ecrã Publish não consegue sair: não fecha depois de publicar e a seta ← não funciona | RF03 / TC-PUB-01 #1, #2; TC-PUB-03 #1 | Alta | Aberto (causa confirmada: pilha de navegação) |

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

## BUG-002 — Ecrã Publish não consegue sair: não fecha depois de publicar e a seta ← não funciona

| Campo | Valor |
|-------|-------|
| **Data** | 2026-10-30 |
| **Requisito** | RF03 — Publicar recurso (CA03.3, CA03.4) |
| **Casos de teste** | TC-PUB-01 #1 (preço 12.50), TC-PUB-01 #2 (preço 0.01), TC-PUB-03 #1 (empréstimo) |
| **Técnica** | Análise de Valor Fronteira / Particionar por Equivalência |
| **Componente** | App Android: navegação da barra inferior (`presentation/MainScreen.kt`) + `presentation/create/CreateResourceScreen.kt` / `CreateResourceViewModel.kt` |
| **Severidade** | Alta: o recurso fica publicado, mas o utilizador não sabe. É provável que volte a carregar em Publish e crie **duplicados** |
| **Prioridade** | Alta |
| **Estado** | Aberto. Causa confirmada por testes manuais: a navegação da barra inferior (`MainScreen.kt`) constrói mal a pilha de navegação |

### Reprodutibilidade
- **Testes automáticos:** 3 em 3 execuções falharam. Cada sessão do Appium começa com a app limpa (`noReset: false`).
- **Testes manuais (2026-10-30):**
  1. 1.ª tentativa: o formulário não fechou e ficou com os dados preenchidos.
  2. 2.ª tentativa, depois de eliminar as publicações criadas: o formulário fechou corretamente.
  3. 3.ª tentativa: o formulário não fechou. Depois de eliminar as publicações, uma nova publicação **não fechou o formulário, mas limpou-o** (campos vazios). As tentativas seguintes voltaram a falhar.
  4. Tentativas seguintes: o defeito reproduz-se sempre. Não voltou a acontecer o formulário fechar nem limpar.
- **Comportamento enquanto o formulário está "preso":** a seta ← do topo **não faz nada** e o separador **Borrow não muda**. Os separadores da barra inferior (por exemplo, Profile) continuam a funcionar.
- **Reprodução sem publicar nada (2026-10-30):** abrir o separador Publish e, com o formulário **vazio**, tocar na seta ← do topo. **Não acontece nada.** O defeito não depende do servidor nem da publicação: o `popBackStack()` deste ecrã não tem para onde voltar. Depois de publicar, o fecho usa o mesmo `popBackStack()` e falha da mesma maneira.
- **Dependência do percurso (2026-10-30):** Home → Publish → Borrow → seta ←: o formulário **bloqueia** (seta, campos de texto, dropdowns e imagem deixam de funcionar; os dropdowns abrem, mas o valor escolhido não fica). Home → Profile → Home → Publish → seta ←: **funciona**, mas volta ao **Profile** e não à Home.
- **Ambientes:** emulador Android 9.0 (API 28) e telemóvel Android 15. O comportamento é igual nos dois, por isso não depende da versão do Android.
- Conclusão: o ecrã Publish fica **inativo**. Continua visível mas já não responde, o que aponta para um problema de navegação e não para os dados publicados.

### Passos para reproduzir
**Reprodução mínima (sem servidor):**
1. Iniciar sessão.
2. Tocar em **Publish** (barra inferior).
3. Sem preencher nada, tocar na seta ← do topo. Resultado: não acontece nada (esperado: voltar à Home).

**Reprodução pelo caso de teste:**
1. Iniciar sessão como `aluno2@ipca.pt`.
2. Tocar em **Publish** (barra inferior).
3. Preencher o título (`Livro de Cálculo <sufixo>`), escolher **Sell** e preço `12.50`. A categoria fica com a 1.ª por omissão.
4. Tocar em **Publish** (topo do formulário).

O mesmo acontece com o preço `0.01` e com **Borrow**, duração `1 semestre` e zonas `Campus IPCA`.

### Resultado esperado
- O recurso é criado no servidor.
- O formulário fecha (volta ao ecrã anterior), como em `LaunchedEffect(uiState.isSuccess) { navController.popBackStack() }`.

### Resultado obtido
- O recurso **é criado** no servidor. O teste confirma-o pela API (`api.procurarRecurso`). ✔
- O formulário **continua aberto** após 15 s, com os dados preenchidos. ✘
- **Não aparece nenhuma mensagem de erro**: no dump não há texto vermelho entre "Select Image" e "Title:", onde a app mostra `uiState.error`. ✘
- O botão Publish voltou ao estado normal (texto "Publish", sem spinner). O pedido já terminou.

Evidência:
- Mocha: `o recurso foi criado mas o formulário não fechou: defeito na app (formulário ficou aberto, recurso CRIADO no servidor)` (`publicar.test.js:39`), nos 3 casos.
- Dumps: `_1_pre_o_12_50_publicado.xml`, `_2_pre_o_0_01_publicado_limite_inferior_v_lido_.xml` e `TC_PUB_03_1_empr_stimo_sem_pre_o_com_dura_o_e_zonas.xml` mostram "Publish Item" com o título preenchido e sem erro.
- Os casos em que a app **rejeita** antes de chamar o servidor (TC-PUB-01 #3–#5, TC-PUB-02) passam. O problema só aparece quando o pedido chega ao servidor e tem sucesso.

### Análise da causa

**Causa confirmada (testes manuais de 2026-10-30).** A pilha de navegação depende dos separadores visitados antes, e a Home é removida dela:

| Sequência | Pilha resultante (simplificada) | Seta ← no Publish |
|-----------|---------------------------------|-------------------|
| Home → Publish | `[CreateResource]`: a Home foi removida | **Bloqueia** (não há ecrã por baixo; fica o "ecrã fantasma") |
| Home → Profile → Home → Publish | `[Profile, CreateResource]` | Funciona, mas **volta ao Profile** em vez da Home |

Porquê, no `MainScreen.kt`: `popUpTo(bottomNavController.graph.startDestinationId)` aponta para o **grafo** `HomeGraph`, não para o ecrã Home.
- Quando o `HomeGraph` está na pilha, o `popUpTo` remove tudo o que está por cima dele, **incluindo o ecrã Home**. O grafo, sem ecrã, é depois descartado pela navegação.
- Quando o `HomeGraph` já não está na pilha, o `popUpTo` **não encontra nada e não remove nada**. Os separadores anteriores (por exemplo, Profile) ficam acumulados.
- Resultado: a Home nunca fica na base da pilha. Às vezes não há nada por baixo do Publish, e outras vezes está lá o separador errado.

O comportamento do formulário bloqueado também bate com isto. Os dropdowns abrem porque usam estado local do ecrã (`remember`). Os campos não guardam o texto porque dependem do ViewModel, e o ecrã já não está ativo (`collectAsStateWithLifecycle` parou).

**0. O ecrã "preso" já não está ativo na navegação ("ecrã fantasma").**
- O separador **Borrow** chama `viewModel.onTypeChange("BORROW")`. O ecrã lê o estado com `collectAsStateWithLifecycle()`, que **só atualiza enquanto o ecrã está ativo** (lifecycle STARTED). Se o Borrow não muda, o ecrã já não está ativo.
- A seta ← chama `navController.popBackStack()`. Se não faz nada, a entrada do ecrã Publish **já não está na pilha de navegação**, ou não há nada para onde voltar.
- A barra inferior continua a funcionar porque pertence ao `MainScreen`, que continua ativo.
- Conclusão: depois de publicar, o `isSuccess` passou a `true` e o `popBackStack()` **correu e removeu o ecrã Publish da pilha**. Por baixo, porém, não havia nenhum ecrã para mostrar. O `NavHost` continuou a mostrar a última imagem do formulário, que já não responde a nada.

**1. O formulário limpo prova que a publicação teve sucesso na app e que houve navegação.**
Em `CreateResourceViewModel.kt`, nada volta a pôr os campos vazios. Não há nenhum reset depois do sucesso. Os campos só estão vazios quando o ViewModel é **novo** (`MutableStateFlow(CreateResourceUiState())`, linha 16). Um ViewModel novo só aparece com uma **nova entrada de navegação** do ecrã Publish.
Logo, na 3.ª tentativa manual: `isSuccess` passou a `true`, o `popBackStack()` correu, e o utilizador **voltou a cair noutro ecrã Publish** em vez de voltar à Home.
Isto afasta a hipótese de uma exceção depois da resposta do servidor. Há mais um argumento contra essa hipótese: a Home usa o mesmo `ResourceDTO`, o mesmo `toDomain()` e a mesma cache Room, e funciona.

**2. Causa mais provável: navegação da barra inferior** (`presentation/MainScreen.kt`, linhas 47–54):
```kotlin
bottomNavController.navigate(route) {
    popUpTo(bottomNavController.graph.startDestinationId) {   // ← HomeGraph (grafo aninhado)
        inclusive = false
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```
- Cada separador é um **grafo aninhado** (`HomeGraph`, `CreateGraph`, `ProfileGraph`…). Por isso, `graph.startDestinationId` é o id do grafo `HomeGraph` e não do ecrã Home. O padrão documentado pela Google para grafos aninhados é `popUpTo(navController.graph.findStartDestination().id)`. Com o id do grafo, a pilha de navegação pode ficar num estado inconsistente.
- `saveState = true` + `restoreState = true` guardam e **restauram** a pilha de cada separador, incluindo os ViewModels. Ao andar entre separadores (por exemplo, Profile para eliminar publicações e depois Publish outra vez), podem ser restauradas entradas antigas do ecrã Publish. O `popBackStack()` revela então essas entradas em vez da Home.
- Isto explica a intermitência. O resultado depende dos separadores visitados antes, o que bate com as diferenças entre as tentativas manuais.
- Ligação ao ponto 0: o `popUpTo(<id do grafo HomeGraph>)` com `inclusive = false` remove da pilha o **ecrã Home** e deixa só a entrada do **grafo** `HomeGraph`, que não tem ecrã próprio. Quando o Publish sai da pilha, o topo passa a ser esse grafo vazio. Não há ecrã para mostrar, e fica o "ecrã fantasma".

**3. Defeitos de código que agravam o problema:**
- `isSuccess` **nunca volta a `false`**. Se um ViewModel com `isSuccess = true` for restaurado (ponto 2), o `LaunchedEffect` volta a disparar `popBackStack()` assim que o ecrã aparece.
- **Falha silenciosa** no `catch` do `submit()`: `error = e.localizedMessage` pode ser `null`, e então nenhuma mensagem aparece. Não há `Log`. Não é a causa deste caso, mas esconde qualquer outra falha.
- **Não há mensagem de sucesso.** O único sinal de que a publicação correu bem é o ecrã fechar. Quando a navegação falha, o utilizador fica sem nenhuma indicação.

### Diagnóstico (feito)
Os passos abaixo foram usados para confirmar a causa. Ficam como registo.

1. **Seta de voltar (feito, 2026-10-30):** com o formulário preso, a seta ← não faz nada e o separador Borrow não muda. Confirma que o ecrã está inativo (ponto 0).
2. **Separador destacado na barra inferior.** Com o formulário preso, ver qual dos ícones está destacado (círculo roxo). A previsão é que esteja destacado o **Home** e não o Publish. A seguir, tocar em **Home**: a previsão é que **não aconteça nada**, porque a app acha que já está no Home. Tocar em **Profile** deve funcionar.
   Se isto se confirmar, fica provado que, para a app, o ecrã atual é o grafo `HomeGraph` vazio (ponto 2).
3. **Sequência controlada:** limpar os dados (`adb shell pm clear ipca.api.relay`), fazer login e publicar **diretamente a partir da Home**. Repetir mas passando antes por **Profile** e depois **Publish**. Anotar em que sequência falha.
4. **Logcat** (confirma que o servidor respondeu 201 e que não houve exceção):
   ```powershell
   adb logcat -c
   # reproduzir o defeito
   adb logcat -d | Select-String -Pattern "okhttp|AndroidRuntime|Exception"
   ```

### Correção proposta (não aplicada)
0. **Navegação (causa provável).** Em `MainScreen.kt`, usar o padrão documentado para grafos aninhados:
   ```kotlin
   import androidx.navigation.NavGraph.Companion.findStartDestination
   ...
   popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
   ```
   Depois de publicar, navegar explicitamente para a Home (ou para o detalhe do recurso) em vez de depender do `popBackStack()`. Repor `isSuccess = false` logo a seguir, para não ficar estado antigo no ViewModel.
1. **Nunca falhar em silêncio.** No `catch`, mostrar sempre uma mensagem e registar a exceção:
   ```kotlin
   } catch (e: Exception) {
       Log.e("CreateResourceVM", "submit failed", e)
       _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Could not publish the item") }
   }
   ```
2. **Separar o sucesso no servidor dos passos opcionais.** Quando o `api.createResource` devolve 2xx, o recurso está publicado. O upload da imagem, a conversão e a cache não devem impedir que `isSuccess = true`. Coloca-os num `try/catch` próprio dentro do `addResource`, ou corre-os depois de marcar o sucesso.
3. Mostrar uma confirmação de sucesso (por exemplo, um Snackbar "Item published"), para o utilizador não depender só de o ecrã fechar.

Nota: o `OkHttpClient` tem `retryOnConnectionFailure(true)`. Com o túnel ngrok, um POST pode ser repetido automaticamente se a ligação cair, o que também pode gerar duplicados. Convém ter isto em conta na análise.

### Verificação após correção
1. `npm run test:publicar`: TC-PUB-01 #1, #2 e TC-PUB-03 #1 devem passar (o formulário fecha e o recurso fica criado).
2. Os testes que já passavam (TC-PUB-01 #3–#5, TC-PUB-02 #1–#3) devem continuar a passar.
3. Confirmar na API que **só existe um** recurso com cada título (sem duplicados).

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