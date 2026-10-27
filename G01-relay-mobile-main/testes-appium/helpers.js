const fs = require('fs');
const { remote } = require('webdriverio');

const APK = process.env.APK_PATH || ''; // ex.: C:\\caminho\\app-debug.apk (vazio = app já instalada)
const TIMEOUT = 15000;

const USER1 = { email: process.env.RELAY_EMAIL1 || 'aluno1@ipca.pt', password: process.env.RELAY_PASS1 || 'Teste1234' };
const USER2 = { email: process.env.RELAY_EMAIL2 || 'aluno2@ipca.pt', password: process.env.RELAY_PASS2 || 'Teste1234' };

// A app só tem 5 pontos de encontro fixos (não existe "Biblioteca IPCA"): usamos o do IPCA.
const PONTO_ENCONTRO = process.env.PONTO_ENCONTRO || 'IPCA';
const UNIVERSIDADE = process.env.UNIVERSIDADE || 'IPCA'; // texto contido no nome da universidade

// ---------------------------------------------------------------- sessão
// Abre uma sessão nova (app limpa, deslogada)
async function criarDriver() {
    return remote({
        hostname: '127.0.0.1',
        port: 4723,
        path: '/',
        logLevel: 'error',
        capabilities: {
            platformName: 'Android',
            'appium:automationName': 'UiAutomator2',
            'appium:deviceName': process.env.DEVICE_NAME || 'Android Emulator',
            'appium:noReset': false,
            'appium:autoGrantPermissions': true,
            'appium:uiautomator2ServerLaunchTimeout': 60000,
            'appium:uiautomator2ServerInstallTimeout': 60000,
            'appium:disableWindowAnimation': true,
            ...(APK
                ? { 'appium:app': APK, 'appium:appWaitActivity': '*' }
                : { 'appium:appPackage': 'ipca.api.relay', 'appium:appActivity': '.MainActivity' })
        }
    });
}

// ---------------------------------------------------------------- elementos
// A app não tem ids: procuramos pelo texto visível
const porTexto = (driver, texto) => driver.$(`android=new UiSelector().text("${texto}")`);
const porTextoContem = (driver, texto) => driver.$(`android=new UiSelector().textContains("${texto}")`);
const porDesc = (driver, desc) => driver.$(`~${desc}`);

async function visivel(elemento, timeout = 3000) {
    try { await elemento.waitForDisplayed({ timeout }); return true; } catch { return false; }
}

async function desapareceu(elemento, timeout = 10000) {
    try { await elemento.waitForDisplayed({ timeout, reverse: true }); return true; } catch { return false; }
}

async function esperarTexto(driver, texto, timeout = TIMEOUT) {
    const el = porTexto(driver, texto);
    await el.waitForDisplayed({ timeout });
    return el;
}

async function tocarTexto(driver, texto) {
    await (await esperarTexto(driver, texto)).click();
}

// Botões (Compose "Button" aparece como android.widget.Button); se não for, usa o texto
async function botao(driver, texto) {
    const b = driver.$(`android=new UiSelector().className("android.widget.Button").text("${texto}")`);
    if (await visivel(b, 5000)) return b;
    return esperarTexto(driver, texto);
}

async function tocarBotao(driver, texto) {
    await (await botao(driver, texto)).click();
}

// Campos de texto do ecrã por ordem
async function campos(driver) {
    await driver.$('android.widget.EditText').waitForDisplayed({ timeout: TIMEOUT });
    return driver.$$('android.widget.EditText');
}

async function esconderTeclado(driver) {
    try { await driver.hideKeyboard(); } catch { /* não havia teclado */ }
}

async function scroll(driver, direction = 'down', vezes = 1) {
    const { width, height } = await driver.getWindowSize();
    for (let i = 0; i < vezes; i++) {
        await driver.execute('mobile: scrollGesture', {
            left: Math.round(width * 0.1), top: Math.round(height * 0.3),
            width: Math.round(width * 0.8), height: Math.round(height * 0.4),
            direction, percent: 0.9
        });
    }
}

async function screenshot(driver, nome) {
    try {
        fs.mkdirSync('screenshots', { recursive: true });
        await driver.saveScreenshot(`screenshots/${nome.replace(/[^a-z0-9]+/gi, '_').slice(0, 80)}.png`);
    } catch { /* sessão já morta: ignorar */ }
}

// ---------------------------------------------------------------- dados partilhados entre ficheiros
const FICHEIRO_DADOS = 'dados.json';
const guardarDados = (d) => fs.writeFileSync(FICHEIRO_DADOS, JSON.stringify(d, null, 2));
function lerDados() {
    if (!fs.existsSync(FICHEIRO_DADOS)) {
        throw new Error('Falta o dados.json: corre primeiro "npm run preparar" (cria os recursos de teste).');
    }
    return JSON.parse(fs.readFileSync(FICHEIRO_DADOS, 'utf8'));
}

// ---------------------------------------------------------------- datas
const iso = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
const hojeMais = (dias) => { const d = new Date(); d.setDate(d.getDate() + dias); return d; };

// ---------------------------------------------------------------- login
async function fazerLogin(driver, email, password) {
    await esperarTexto(driver, 'Forgot password?');
    const c = await campos(driver);
    if (email) await c[0].setValue(email);
    if (password) await c[1].setValue(password);
    await tocarBotao(driver, 'Login');
}

// Login e espera pela Home (a barra inferior só existe com sessão iniciada)
async function iniciarSessao(driver, user) {
    await fazerLogin(driver, user.email, user.password);
    if (!(await visivel(porTexto(driver, 'Publish'), TIMEOUT))) {
        throw new Error(`Login de ${user.email} falhou. Textos no ecrã: ${await textosVisiveis(driver)}`);
    }
}

// Lista os textos visíveis (ajuda a perceber que erro a app está a mostrar)
async function textosVisiveis(driver) {
    const out = [];
    for (const t of await driver.$$('android.widget.TextView')) {
        try { const x = await t.getText(); if (x) out.push(x); } catch { /* ignorar */ }
    }
    return JSON.stringify(out);
}

// ---------------------------------------------------------------- recursos
async function irParaHome(driver) {
    await tocarTexto(driver, 'Home');
    await esperarTexto(driver, 'View all', 10000).catch(() => {});
}

// Pesquisa o recurso pelo título na Home e abre o detalhe
async function abrirRecurso(driver, titulo) {
    if (!(await visivel(driver.$('android.widget.EditText'), 1500))) await irParaHome(driver);
    const c = await campos(driver);
    await c[0].setValue(titulo);
    await esconderTeclado(driver);
    await tocarTexto(driver, titulo);
    await esperarTexto(driver, 'Details');
}

async function abrirFormularioPublicar(driver) {
    await tocarTexto(driver, 'Publish');      // barra inferior
    await tocarTexto(driver, 'Publish Item'); // menu que aparece
    await esperarTexto(driver, 'Title:');
    await driver.pause(1500);                 // dar tempo às categorias
}

async function escolherCategoria(driver, nome) {
    const c = await campos(driver);
    await c[2].click(); // campo da categoria
    const item = porTexto(driver, nome);
    if (await visivel(item, 3000)) await item.click(); else await driver.back();
}

// r = { tipo: 'SELL'|'BORROW', titulo, categoria?, preco?, duracao?, zonas? }
async function preencherFormulario(driver, r) {
    if (r.tipo === 'BORROW') await tocarTexto(driver, 'Borrow');
    if (r.titulo) await (await campos(driver))[0].setValue(r.titulo);
    if (r.categoria) await escolherCategoria(driver, r.categoria);
    await esconderTeclado(driver);
    if (r.tipo === 'BORROW') {
        if (r.duracao) await preencherAposRotulo(driver, 'Maximum Duration:', r.duracao);
        if (r.zonas) await preencherAposRotulo(driver, 'Zones:', r.zonas);
    } else if (r.preco !== undefined && r.preco !== '') {
        await preencherAposRotulo(driver, 'Price:', String(r.preco));
    }
}

// Faz scroll até ao rótulo, escreve no campo que vem a seguir e confirma o valor lido de volta
async function preencherAposRotulo(driver, rotulo, valor) {
    const campo = driver.$(`//android.widget.TextView[@text="${rotulo}"]/following::android.widget.EditText[1]`);
    // o rótulo pode aparecer logo acima da barra inferior, com o campo ainda escondido: continuar a descer
    for (let i = 0; i < 6 && !(await visivel(campo, 800)); i++) await scroll(driver, 'down', 1);
    await campo.waitForDisplayed({ timeout: 5000 });
    await campo.setValue(valor);
    const lido = await campo.getText();
    if (!String(lido).includes(valor)) {
        throw new Error(`O campo "${rotulo}" ficou com "${lido}" em vez de "${valor}"`);
    }
}

async function tocarPublicar(driver) {
    await esconderTeclado(driver);
    await scroll(driver, 'up', 3);
    const b = await botao(driver, 'Publish');
    // o botão fica desativado enquanto as categorias estão a carregar
    for (let i = 0; i < 15 && !(await b.isEnabled()); i++) await driver.pause(1000);
    await b.click();
}

async function publicarRecurso(driver, r) {
    await abrirFormularioPublicar(driver);
    await preencherFormulario(driver, r);
    await tocarPublicar(driver);
    // sucesso = o formulário fecha
    const fechou = await desapareceu(porTexto(driver, 'Title:'), 20000);
    if (!fechou) throw new Error(`Publicar "${r.titulo}" não fechou o formulário. Textos no ecrã: ${await textosVisiveis(driver)}`);
}

// ---------------------------------------------------------------- ponto de encontro / pagamento / datas
async function escolherPontoEncontro(driver, nome = PONTO_ENCONTRO) {
    await tocarTexto(driver, 'Select a pickup point');
    await tocarTexto(driver, 'List');
    const ponto = porTextoContem(driver, nome);
    await ponto.waitForDisplayed({ timeout: TIMEOUT });
    await ponto.click();
    await tocarBotao(driver, 'Select this meeting place');
}

async function escolherPagamento(driver, metodo) { // 'Cash Payment' | 'MBWAY' | 'Bank Transfer'
    await tocarTexto(driver, 'Choose a payment method');
    await tocarTexto(driver, metodo);
    await tocarBotao(driver, 'Confirm');
}

// Abre o seletor de datas ao tocar em `textoCampo` e escolhe hoje + dias
async function escolherData(driver, textoCampo, dias) {
    const alvo = hojeMais(dias);
    const hoje = new Date();
    await tocarTexto(driver, textoCampo);
    let meses = (alvo.getFullYear() - hoje.getFullYear()) * 12 + (alvo.getMonth() - hoje.getMonth());
    while (meses > 0) { await porDesc(driver, 'Change to next month').click(); meses--; }
    while (meses < 0) { await porDesc(driver, 'Change to previous month').click(); meses++; }
    const dia = porTexto(driver, String(alvo.getDate()));
    await dia.waitForDisplayed({ timeout: TIMEOUT });
    await dia.click();
    await tocarBotao(driver, 'OK');
}

// ---------------------------------------------------------------- histórico
// Conta as transações listadas em Perfil > History (filtro: 'Purchases' | 'Borrowings')
async function contarHistorico(driver, filtro) {
    await tocarTexto(driver, 'Profile');
    await tocarTexto(driver, 'History');
    await esperarTexto(driver, 'All');
    if (filtro) await tocarTexto(driver, filtro);
    await driver.pause(2000);
    const n = (await driver.$$('android=new UiSelector().textStartsWith("Resource #")')).length;
    for (let i = 0; i < 3 && !(await visivel(porTexto(driver, 'Publish'), 1500)); i++) await driver.back();
    return n;
}

module.exports = {
    criarDriver, porTexto, porTextoContem, porDesc, visivel, desapareceu, esperarTexto, tocarTexto,
    botao, tocarBotao, campos, esconderTeclado, scroll, screenshot, guardarDados, lerDados,
    iso, hojeMais, fazerLogin, iniciarSessao, irParaHome, abrirRecurso, abrirFormularioPublicar,
    preencherFormulario, tocarPublicar, publicarRecurso, escolherPontoEncontro, escolherPagamento,
    escolherData, contarHistorico, textosVisiveis, USER1, USER2, TIMEOUT, PONTO_ENCONTRO, UNIVERSIDADE
};