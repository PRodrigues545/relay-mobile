// RF02 - Criar conta | TC-REG-01 a TC-REG-04 (Teste_CriarConta.xlsx)
// Para poder repetir os testes, email e username novos levam um sufixo único.
const assert = require('assert');
const h = require('./helpers');

const suf = String(Date.now()).slice(-6);
const SUBTITULO_PASSO2 = 'Set up your profile (optional)';

describe('RF02 - Criar conta', function () {
    let driver;

    beforeEach(async function () {
        driver = await h.criarDriver();
        await h.esperarTexto(driver, 'Forgot password?');
        await h.tocarBotao(driver, 'Register');           // ecrã de login -> criar conta
        await h.esperarTexto(driver, 'Continue');
    });

    afterEach(async function () {
        if (this.currentTest.state === 'failed') await h.screenshot(driver, this.currentTest.title);
        await driver.deleteSession();
    });

    // Passo 1: a ordem dos campos na app é username, email, password, confirmação
    async function passo1({ email, password, confirmacao, username }) {
        const c = await h.campos(driver);
        if (username) await c[0].setValue(username);
        if (email) await c[1].setValue(email);
        if (password) await c[2].setValue(password);
        if (confirmacao) await c[3].setValue(confirmacao);
        await h.esconderTeclado(driver);
        await h.tocarBotao(driver, 'Continue');
    }

    async function textosNoEcra() {
        const s = new Set();
        for (const t of await driver.$$('android.widget.TextView')) {
            try { s.add(await t.getText()); } catch { /* ignorar */ }
        }
        return s;
    }

    // Espera que apareça um texto que não estava no ecrã antes de abrir o dropdown (= 1.ª opção)
    async function novaOpcao(antes, timeout) {
        const fim = Date.now() + timeout;
        while (Date.now() < fim) {
            for (const t of await driver.$$('android.widget.TextView')) {
                const txt = await t.getText().catch(() => '');
                if (txt && !antes.has(txt)) return t;
            }
            await driver.pause(400);
        }
        return null;
    }

    // Abre um dropdown do passo 2. Toca no placeholder; se a lista não abrir,
    // toca na seta à direita do campo (é a seta que tem o clique na app).
    async function abrirDropdown(placeholder, antes) {
        const campo = await h.scrollAteTexto(driver, placeholder);
        await campo.click();
        if (await novaOpcao(antes, 2500)) return;
        const { y, height } = await campo.getElementRect(await campo.elementId);
        const { width } = await driver.getWindowSize();
        const px = (await driver.getDisplayDensity()) / 160; // dp -> px
        await driver.execute('mobile: clickGesture', { x: Math.round(width - 40 * px), y: Math.round(y + height / 2) });
    }

    // Escolhe uma opção de um dropdown do passo 2. Sem `texto`, escolhe a 1.ª opção da lista.
    // O "antes" é lido ANTES de abrir a lista; lido depois, as opções já lá estariam e nunca seriam "novas".
    async function escolherDropdown(placeholder, texto) {
        await h.esconderTeclado(driver);
        const antes = await textosNoEcra();
        await abrirDropdown(placeholder, antes);
        if (texto) {
            const op = h.porTextoContem(driver, texto);
            await op.waitForDisplayed({ timeout: h.TIMEOUT });
            await op.click();
            return;
        }
        const op = await novaOpcao(antes, h.TIMEOUT);
        if (!op) throw new Error(`Sem opções no dropdown "${placeholder}"`);
        await op.click();
    }

    // TC-REG-01 | CA02.3 e CA02.5 | Use Case Testing
    it('TC-REG-01 #1 registo completo com sucesso', async function () {
        await passo1({ email: `novo1_${suf}@ipca.pt`, password: 'Teste1234', confirmacao: 'Teste1234', username: `ped${suf}a` });
        await h.esperarTexto(driver, SUBTITULO_PASSO2);
        const c = await h.campos(driver);
        await c[0].setValue('Aluno Teste');
        await h.esconderTeclado(driver);
        await escolherDropdown('Select your university', h.UNIVERSIDADE);
        // CA02.3: os cursos só aparecem depois de escolher a universidade
        await h.esperarTexto(driver, 'Select your course');
        await escolherDropdown('Select your course', null);
        await h.tocarBotaoComScroll(driver, 'Register'); // o botão pode estar abaixo do fim do ecrã
        await h.esperarTexto(driver, 'Publish', 20000); // Home com sessão iniciada
    });

    // TC-REG-02 | CA02.1 | Boundary Value Analysis
    describe('TC-REG-02 Validação do passo 1', function () {
        const linhas = [
            ['#1', 'pedro@', 'Teste1234', 'Teste1234', 'pedro01', 'Invalid email'],
            ['#2', 'novo@ipca.pt', 'Abcde12', 'Abcde12', 'pedro01', 'At least 8 characters'],
            ['#3', 'novo@ipca.pt', 'Abcdef12', 'Abcdef13', 'pedro01', 'Passwords do not match'],
            ['#4', 'novo@ipca.pt', 'Teste1234', 'Teste1234', 'ab', 'At least 3 characters'],
            ['#5', 'novo@ipca.pt', 'Teste1234', 'Teste1234', 'pedro 01', 'No spaces allowed'],
            ['#6', 'novo@ipca.pt', 'Abcdef12', 'Abcdef12', 'abc', null]
        ];
        linhas.forEach(([id, email, password, confirmacao, username, erro]) => {
            it(`${id} ${erro ? `mostra "${erro}"` : 'limites válidos: avança para o passo 2'}`, async function () {
                await passo1({ email, password, confirmacao, username });
                if (erro) {
                    assert.ok(await h.visivel(h.porTexto(driver, erro)), `devia mostrar "${erro}"`);
                    assert.ok(!(await h.visivel(h.porTexto(driver, SUBTITULO_PASSO2), 1500)), 'não devia avançar para o passo 2');
                } else {
                    assert.ok(await h.visivel(h.porTexto(driver, SUBTITULO_PASSO2), 10000), 'devia avançar para o passo 2');
                }
            });
        });
    });

    // TC-REG-03 | CA02.2 | State Transition Testing
    it('TC-REG-03 #1 passo 2 vazio + Skip cria a conta', async function () {
        await passo1({ email: `novo2_${suf}@ipca.pt`, password: 'Teste1234', confirmacao: 'Teste1234', username: `ped${suf}b` });
        await h.esperarTexto(driver, SUBTITULO_PASSO2);
        await h.tocarBotaoComScroll(driver, 'Skip');
        await h.esperarTexto(driver, 'Publish', 20000);
    });

    // TC-REG-04 | CA02.4 | Equivalence Partitioning
    it('TC-REG-04 #1 email já registado', async function () {
        await passo1({ email: 'aluno1@ipca.pt', password: 'Teste1234', confirmacao: 'Teste1234', username: `ped${suf}c` });
        await h.esperarTexto(driver, SUBTITULO_PASSO2);
        await h.tocarBotaoComScroll(driver, 'Skip');
        assert.ok(await h.visivel(h.porTexto(driver, 'Email already in use'), 15000), 'devia mostrar "Email already in use"');
        assert.ok(!(await h.visivel(h.porTexto(driver, 'Publish'), 1500)), 'a conta não devia ser criada');
    });
});
