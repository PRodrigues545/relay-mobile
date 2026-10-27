// RF01 - Fazer login | TC-LOG-01 a TC-LOG-04 (Teste_Login.xlsx)
const assert = require('assert');
const h = require('./helpers');

const E_EMAIL_OBRIG = 'Email is required';
const E_EMAIL_INV = 'Invalid email';
const E_PASS_OBRIG = 'Password is required';
const E_CREDENCIAIS = 'Invalid email or password';
const TODOS = [E_EMAIL_OBRIG, E_EMAIL_INV, E_PASS_OBRIG];

describe('RF01 - Fazer login', function () {
    let driver;

    beforeEach(async function () {
        driver = await h.criarDriver();
        await h.esperarTexto(driver, 'Forgot password?');
    });

    afterEach(async function () {
        if (this.currentTest.state === 'failed') await h.screenshot(driver, this.currentTest.title);
        await driver.deleteSession();
    });

    // TC-LOG-01 | CA01.1 | Casos de Uso
    describe('TC-LOG-01 Login com credenciais válidas', function () {
        [['#1', h.USER1], ['#2', h.USER2]].forEach(([id, u]) => {
            it(`${id} ${u.email} acede à Home`, async function () {
                await h.fazerLogin(driver, u.email, u.password);
                await h.esperarTexto(driver, 'Publish'); // barra inferior só existe com sessão iniciada
            });
        });
    });

    // TC-LOG-02 | CA01.2 | Particionar por Equivalência
    describe('TC-LOG-02 Password incorreta', function () {
        [['#1', 'aluno1@ipca.pt', 'errada123'], ['#2', 'aluno2@ipca.pt', 'abc']].forEach(([id, email, pass]) => {
            it(`${id} ${email} / ${pass} mostra erro e fica no login`, async function () {
                await h.fazerLogin(driver, email, pass);
                assert.ok(await h.visivel(h.porTexto(driver, E_CREDENCIAIS), 10000), 'devia mostrar erro de credenciais');
                assert.ok(await h.visivel(h.porTexto(driver, 'Forgot password?')), 'devia continuar no login');
            });
        });
    });

    // TC-LOG-03 | CA01.3 e CA01.4 | Tabela de Decisão
    describe('TC-LOG-03 Combinações de email e password', function () {
        const linhas = [
            ['#1', '', '', [E_EMAIL_OBRIG, E_PASS_OBRIG]],
            ['#2', 'aluno.ipca.pt', '', [E_EMAIL_INV, E_PASS_OBRIG]],
            ['#3', 'aluno1@ipca.pt', '', [E_PASS_OBRIG]],
            ['#4', '', 'Teste1234', [E_EMAIL_OBRIG]],
            ['#5', 'aluno.ipca.pt', 'Teste1234', [E_EMAIL_INV]]
        ];
        linhas.forEach(([id, email, pass, esperados]) => {
            it(`${id} aparecem só os erros: ${esperados.join(' + ')}`, async function () {
                await h.fazerLogin(driver, email, pass);
                for (const msg of esperados) {
                    assert.ok(await h.visivel(h.porTexto(driver, msg)), `devia mostrar "${msg}"`);
                }
                for (const msg of TODOS.filter((m) => !esperados.includes(m))) {
                    assert.ok(!(await h.visivel(h.porTexto(driver, msg), 1000)), `não devia mostrar "${msg}"`);
                }
                assert.ok(!(await h.visivel(h.porTexto(driver, E_CREDENCIAIS), 1000)), 'não devia haver chamada ao servidor');
            });
        });
    });

    // TC-LOG-04 | CA01.5 | Transição de Estados
    describe('TC-LOG-04 Mostrar/ocultar password', function () {
        it('o ícone alterna entre password oculta e visível', async function () {
            const c = await h.campos(driver);
            await c[1].setValue('Teste1234');
            assert.ok(await h.visivel(h.porDesc(driver, 'Show password')), 'password começa oculta');
            await h.porDesc(driver, 'Show password').click();
            assert.ok(await h.visivel(h.porDesc(driver, 'Hide password')), 'password fica visível');
            await h.porDesc(driver, 'Hide password').click();
            assert.ok(await h.visivel(h.porDesc(driver, 'Show password')), 'password volta a ficar oculta');
        });
    });
});
