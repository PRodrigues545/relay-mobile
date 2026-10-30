const fs = require('fs');
const { remote } = require('webdriverio');

const APK = process.env.APK_PATH || ''; // ex.: C:\\caminho\\app-debug.apk (vazio = app já instalada)
const TIMEOUT = 15000;

const USER1 = { email: process.env.RELAY_EMAIL1 || 'aluno1@ipca.pt', password: process.env.RELAY_PASS1 || 'Teste1234' };
const USER2 = { email: process.env.RELAY_EMAIL2 || 'aluno2@ipca.pt', password: process.env.RELAY_PASS2 || 'Teste1234' };

// A app só tem 5 pontos de encontro fixos (não existe "Biblioteca IPCA"): usamos o do IPCA.
const PONTO_ENCONTRO = process.env.PONTO_ENCONTRO || 'IPCA';
const UNIVERSIDADE = process.env.UNIVERSIDADE || 'University of Porto'; // texto contido no nome da universidade

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

// Botões. Nesta app o texto de um botão Compose aparece como um TextView DENTRO do botão.
// Devolvemos o elemento pai (o próprio botão): é nele que estão o "enabled" e o clique.
// Usar o TextView daria sempre isEnabled() = true, mesmo com o botão desativado.
async function botao(driver, texto) {
    const b = driver.$(`android=new UiSelector().className("android.widget.Button").text("${texto}")`);
    if (await visivel(b, 1000)) return b;
    return botaoDoTexto(await esperarTexto(driver, texto));
}

async function botaoDoTexto(textView) {
    const pai = await textView.$('..');
    return (await pai.isExisting()) ? pai : textView;
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

// Faz scroll no ecrã até o texto aparecer (ecrãs com scroll, ex.: passo 2 do registo)
async function scrollAteTexto(driver, texto) {
    try {
        await driver.$(`android=new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text("${texto}"))`);
    } catch { /* o ecrã não tem scroll ou o texto já está visível */ }
    return esperarTexto(driver, texto);
}

async function tocarBotaoComScroll(driver, texto) {
    await esconderTeclado(driver);
    await scrollAteTexto(driver, texto);
    await tocarBotao(driver, texto);
}

// Toque duplo real: dois toques seguidos no mesmo ponto (um .click() a seguir ao outro
// pode falhar em silêncio se o botão mudar depois do 1.º toque, e o teste passaria sem testar nada)
async function duploToque(driver, elemento) {
    await driver.execute('mobile: doubleClickGesture', { elementId: elemento.elementId });
}

// Em caso de falha guarda a captura (.png) e a árvore de elementos do ecrã (.xml).
// O .xml mostra todos os elementos, mesmo os que não são texto, com o estado (enabled, etc.).
async function screenshot(driver, nome) {
    const base = `screenshots/${nome.replace(/[^a-z0-9]+/gi, '_').slice(0, 80)}`;
    try {
        fs.mkdirSync('screenshots', { recursive: true });
        await driver.saveScreenshot(`${base}.png`);
        fs.writeFileSync(`${base}.xml`, await driver.getPageSource());
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

// Escreve num campo de texto Compose e confirma que o valor ficou lá.
// O setValue às vezes não "pega" (o campo fica vazio); nesse caso escreve tecla a tecla,
// como faria o teclado, que é o caminho mais fiável em Compose.
async function escreverCampo(driver, campo, valor) {
    const ficou = async () => { await driver.pause(400); return String(await campo.getText()).includes(valor); };
    await campo.setValue(valor);
    if (await ficou()) return;
    await campo.click();
    await campo.clearValue();
    await driver.keys(valor.split(''));
    if (await ficou()) return;
    try { await driver.execute('mobile: type', { text: valor }); } catch { /* versão do driver sem "mobile: type" */ }
    await ficou();
}

// Faz scroll até ao rótulo, escreve no campo que vem a seguir e confirma o valor lido de volta
async function preencherAposRotulo(driver, rotulo, valor) {
    const campo = driver.$(`//android.widget.TextView[@text="${rotulo}"]/following::android.widget.EditText[1]`);
    // o rótulo pode aparecer logo acima da barra inferior, com o campo ainda escondido: continuar a descer
    for (let i = 0; i < 6 && !(await visivel(campo, 800)); i++) await scroll(driver, 'down', 1);
    await campo.waitForDisplayed({ timeout: 5000 });
    await escreverCampo(driver, campo, valor);
    const lido = await campo.getText();
    if (!String(lido).includes(valor)) {
        throw new Error(`O campo "${rotulo}" ficou com "${lido}" em vez de "${valor}"`);
    }
}

// Texto "Publish" do cabeçalho do formulário: o 1.º "Publish" depois do título "Publish Item"
// (o outro "Publish" é o da barra inferior). Enquanto o pedido decorre, o texto é trocado por um indicador.
const textoPublicarFormulario = (driver) =>
    driver.$('//android.widget.TextView[@text="Publish Item"]/following::android.widget.TextView[@text="Publish"][1]');

async function tocarPublicar(driver) {
    await esconderTeclado(driver);
    await scroll(driver, 'up', 3);
    // Na app, o botão só fica ativo depois de as categorias carregarem; enquanto carregam,
    // o campo da categoria mostra "Loading…". Esperar que isso desapareça.
    if (!(await desapareceu(driver.$('android=new UiSelector().textStartsWith("Loading")'), 20000))) {
        throw new Error('As categorias não carregaram ao fim de 20 s (o botão "Publish" fica desativado): backend lento ou em baixo?');
    }
    const t = textoPublicarFormulario(driver);
    if (!(await visivel(t, 5000))) {
        throw new Error(`Não encontrei o botão "Publish" do formulário. Textos no ecrã: ${await textosVisiveis(driver)}`);
    }
    await (await botaoDoTexto(t)).click();
}

// Erro mostrado pela app no formulário (fica entre a caixa da imagem e o rótulo "Title:")
async function erroDoFormulario(driver) {
    const el = driver.$('//android.widget.TextView[@text="Select Image"]/following::android.widget.TextView[1]');
    try {
        const t = await el.getText();
        return t && t !== 'Title:' ? t : null;
    } catch { return null; }
}

async function publicarRecurso(driver, r) {
    await abrirFormularioPublicar(driver);
    await preencherFormulario(driver, r);
    await tocarPublicar(driver);
    // sucesso = o formulário fecha; enquanto o pedido está a decorrer o botão mostra um indicador de carregamento
    const fim = Date.now() + 30000;
    while (Date.now() < fim) {
        if (!(await visivel(porTexto(driver, 'Title:'), 500))) return;
        const erro = await erroDoFormulario(driver);
        if (erro) throw new Error(`Publicar "${r.titulo}": a app mostrou o erro "${erro}"`);
        await driver.pause(1000);
    }
    const aCarregar = !(await visivel(textoPublicarFormulario(driver), 500));
    // Perguntar à API se o recurso foi criado, para distinguir "não publicou" de "publicou mas o ecrã não fechou"
    let noServidor = 'não foi possível confirmar na API';
    try {
        noServidor = (await require('./api').procurarRecurso(USER1, r.titulo))
            ? 'O RECURSO FOI CRIADO no servidor: a app publicou mas o formulário não fechou'
            : 'o recurso NÃO existe no servidor';
    } catch (e) { noServidor += ` (${e.message})`; }
    throw new Error(`Publicar "${r.titulo}" não fechou o formulário ao fim de 30 s` +
        (aCarregar ? ' (o pedido ainda estava a decorrer)' : ' (sem erro visível)') +
        `; ${noServidor}. Textos no ecrã: ${await textosVisiveis(driver)}`);
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

// Abre o seletor de datas ao tocar em `textoCampo` e devolve o elemento do dia hoje + dias
async function abrirDia(driver, textoCampo, dias) {
    const alvo = hojeMais(dias);
    const hoje = new Date();
    await tocarTexto(driver, textoCampo);
    let meses = (alvo.getFullYear() - hoje.getFullYear()) * 12 + (alvo.getMonth() - hoje.getMonth());
    while (meses > 0) { await porDesc(driver, 'Change to next month').click(); meses--; }
    while (meses < 0) { await porDesc(driver, 'Change to previous month').click(); meses++; }
    const dia = porTexto(driver, String(alvo.getDate()));
    await dia.waitForDisplayed({ timeout: TIMEOUT });
    return botaoDoTexto(dia); // o "enabled" do dia está no elemento pai, tal como nos botões
}

// Escolhe hoje + dias e confirma com OK
async function escolherData(driver, textoCampo, dias) {
    await (await abrirDia(driver, textoCampo, dias)).click();
    await tocarBotao(driver, 'OK');
}

// Diz se o dia hoje + dias pode ser escolhido no seletor; fecha o seletor sem escolher nada.
// (No seletor da data de fim, a app desativa os dias iguais ou anteriores ao início.)
async function diaSelecionavel(driver, textoCampo, dias) {
    const ativo = await (await abrirDia(driver, textoCampo, dias)).isEnabled();
    await tocarTexto(driver, 'Cancel');
    return ativo;
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
    escolherData, diaSelecionavel, contarHistorico, textosVisiveis, scrollAteTexto, tocarBotaoComScroll,
    duploToque, USER1, USER2, TIMEOUT, PONTO_ENCONTRO, UNIVERSIDADE
};