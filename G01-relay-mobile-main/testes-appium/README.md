# Testes Appium – Relay Mobile (20 testes)

```
testes-appium/
├── 00-preparar-dados.test.js  (cria os recursos de teste; corre primeiro)
├── login.test.js        TC-LOG-01..04
├── registo.test.js      TC-REG-01..04
├── publicar.test.js     TC-PUB-01..03
├── comprar.test.js      TC-COM-01..05
├── emprestimo.test.js   TC-EMP-01..04
├── helpers.js           (ligação ao Appium e funções comuns)
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
npm run preparar     # 1x antes de comprar/emprestimo (cria recursos e grava dados.json)
npm test             # todos os testes
npm run test:login   # ou :registo :publicar :comprar :emprestimo
```
`APK_PATH` (caminho do .apk) instala a app; sem isso ela tem de estar já instalada.
Outras contas: `RELAY_EMAIL1/RELAY_PASS1/RELAY_EMAIL2/RELAY_PASS2`.
Falhas deixam uma captura em `screenshots/`.

## Notas de adaptação (a app ≠ Excel)
- Pontos de encontro: a app só tem 5 pontos fixos; "Biblioteca IPCA" não existe, usa-se o que contém "IPCA" (`PONTO_ENCONTRO`).
- Botões da app: "Buy now"/"Buy", "Ask for loan"/"Submit Request" (= "Confirmar" do Excel).
- Compras e pedidos "gastam" o recurso: por isso `npm run preparar` cria recursos novos (títulos com sufixo) em cada execução.
- TC-PUB-02 #2/#3 (sem categoria) cortam a rede depois do login para as categorias não carregarem.
- TC-PUB-01 #3 (preço 0), TC-EMP-03 #1 (início ontem), TC-COM-05 #1 e TC-EMP-04 #2 (recurso próprio) podem **falhar de propósito** se a app não validar: é um defeito encontrado, não um erro do teste.
- Ecrãs com mais risco (nunca foram corridos num dispositivo): Stripe PaymentSheet (COM-01 #3, COM-03), seletor de datas e o dropdown de curso (REG-01).
