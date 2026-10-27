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

    // Escolhe uma opção de um dropdown do passo 2. Sem `texto`, escolhe a 1.ª opção da lista.
    async function escolherDropdown(placeholder, texto) {
        await h.tocarTexto(driver, placeholder);
        if (texto) {
            const op = h.porTextoContem(driver, texto);
            await op.waitForDisplayed({ timeout: h.TIMEOUT });
            await op.click();
            return;
        }
        const antes = new Set();
        for (const t of await driver.$$('android.widget.TextView')) antes.add(await t.getText());
        await driver.pause(800);
        for (const t of await driver.$$('android.widget.TextView')) {
            const txt = await t.getText();
            if (txt && !antes.has(txt)) { await t.click(); return; }
        }
        throw new Error(`Sem opções no dropdown "${placeholder}"`);
    }

    // TC-REG-01 | CA02.3 e CA02.5 | Casos de Uso
    it('TC-REG-01 #1 registo completo com sucesso', async function () {
        await passo1({ email: `novo1_${suf}@ipca.pt`, password: 'Teste1234', confirmacao: 'Teste1234', username: `ped${suf}a` });
        await h.esperarTexto(driver, SUBTITULO_PASSO2);
        const c = await h.campos(driver);
        await c[0].setValue('Aluno Teste');
        await h.esconderTeclado(driver);
        await escolherDropdown('Select your university', h.UNIVERSIDADE);
        await escolherDropdown('Select your course', null);
        await h.tocarBotao(driver, 'Register');
        await h.esperarTexto(driver, 'Publish', 20000); // Home com sessão iniciada
    });

    // TC-REG-02 | CA02.1 | Análise de Valor Fronteira
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

    // TC-REG-03 | CA02.2 | Transição de Estados
    it('TC-REG-03 #1 passo 2 vazio + Skip cria a conta', async function () {
        await passo1({ email: `novo2_${suf}@ipca.pt`, password: 'Teste1234', confirmacao: 'Teste1234', username: `ped${suf}b` });
        await h.esperarTexto(driver, SUBTITULO_PASSO2);
        await h.tocarBotao(driver, 'Skip');
        await h.esperarTexto(driver, 'Publish', 20000);
    });

    // TC-REG-04 | CA02.4 | (sem técnica)
    it('TC-REG-04 #1 email já registado', async function () {
        await passo1({ email: 'aluno1@ipca.pt', password: 'Teste1234', confirmacao: 'Teste1234', username: `ped${suf}c` });
        await h.esperarTexto(driver, SUBTITULO_PASSO2);
        await h.tocarBotao(driver, 'Skip');
        assert.ok(await h.visivel(h.porTexto(driver, 'Email already in use'), 15000), 'devia mostrar "Email already in use"');
        assert.ok(!(await h.visivel(h.porTexto(driver, 'Publish'), 1500)), 'a conta não devia ser criada');
    });
});
