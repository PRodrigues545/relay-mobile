# Testes Appium – Relay Mobile (21 testes)

```
testes-appium/
├── 00-preparar-dados.test.js  (cria os recursos de teste pela API; corre primeiro)
├── limpar.js            (lista/apaga os recursos criados pelos testes)
├── login.test.js        TC-LOG-01..04
├── registo.test.js      TC-REG-01..04
├── publicar.test.js     TC-PUB-01..04
├── comprar.test.js      TC-COM-01..05
├── emprestimo.test.js   TC-EMP-01..04
├── helpers.js           (ligação ao Appium e funções comuns)
├── api.js               (verificações diretas na API: contagem de compras e pedidos)
└── package.json
```

## Preparação (uma vez)
```
npm install
npx appium driver install uiautomator2
```
Requisitos: Node 20.19+, Android Studio com emulador (API 28+), `ANDROID_HOME` definido,
backend ativo, contas `aluno1@ipca.pt` e `aluno2@ipca.pt` (password `Teste1234`) criadas,
e Stripe em modo de teste (cartões 4242… e 4000 0000 0000 0002).
Para o emulador ter o mesmo dia que o PC (testes de datas), deixa a data/hora automática.

## Executar (2 terminais)
1. Terminal 1: `npx appium` (deixar a correr)
2. Terminal 2, com o emulador ligado (`adb devices`):
```
npm run preparar     # 1x antes de comprar/emprestimo (cria recursos PELA API e grava dados.json)
npm test             # todos os testes
npm run test:login   # ou :registo :publicar :comprar :emprestimo
```
`APK_PATH` (caminho do .apk) instala a app; sem isso ela tem de estar já instalada.
Outras contas: `RELAY_EMAIL1/RELAY_PASS1/RELAY_EMAIL2/RELAY_PASS2`.
`RELAY_API` muda o endereço da API usado em `api.js` (por omissão, o mesmo URL ngrok da app; o túnel tem de estar ativo).
O emulador tem de ter uma imagem com Google APIs/Play Services (o ecrã do ponto de encontro usa Google Maps).
Falhas deixam uma captura em `screenshots/`.

## Limpar os dados de teste
```
npm run limpar          # lista os recursos criados pelos testes (não apaga nada)
npm run limpar:apagar   # apaga-os (pela API: DELETE resources/delete/{id})
```
Recursos com compras ou pedidos associados podem não poder ser apagados; ficam listados.

## Notas de adaptação (a app ≠ Excel)
- Pontos de encontro: a app só tem 5 pontos fixos; "Biblioteca IPCA" não existe, usa-se o que contém "IPCA" (`PONTO_ENCONTRO`).
- Botões da app: "Buy now"/"Buy", "Ask for loan"/"Submit Request" (= "Confirmar" do Excel).
- Compras e pedidos "gastam" o recurso: por isso `npm run preparar` cria recursos novos (títulos com sufixo) em cada execução.
- TC-PUB-02 #2/#3 (sem categoria) cortam a rede depois do login para as categorias não carregarem. A app valida a categoria antes do título, por isso o #3 (sem título e sem categoria) mostra só "Please select a category".
- TC-PUB-01 #4 ("-5"): o campo de preço só aceita dígitos e "."; o teste confirma que o "-" é recusado.
- TC-EMP-02 #2/#3: o seletor da data de fim desativa os dias iguais ou anteriores ao início; o teste confirma que esses dias não se podem escolher.
- TC-COM-04 e TC-EMP-04 fazem um toque duplo real (`mobile: doubleClickGesture`) e contam as compras/pedidos pela API, porque o histórico da app só mostra os itens que cabem no ecrã.
- TC-PUB-01 #3 (preço 0) e #5 (preço vazio) podem **falhar de propósito**: a app não valida o preço, por isso o resultado depende do backend. Se falharem, é um defeito encontrado, não um erro do teste.
- TC-EMP-03 #1 (início ontem) **falha de propósito**: o seletor da data de início aceita dias passados e a app só compara fim > início.
- TC-PUB-04 (tipo "Ambos") **falha de propósito**: o formulário só tem "Sell" e "Borrow", por isso o requisito CA03.5 não está implementado.
- Ecrãs com mais risco (nunca foram corridos num dispositivo): Stripe PaymentSheet (COM-01 #3, COM-03), seletor de datas e o dropdown de curso (REG-01).
