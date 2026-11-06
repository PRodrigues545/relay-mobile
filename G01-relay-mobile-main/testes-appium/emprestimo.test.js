// RF05 - Pedir emprestado | TC-EMP-01 a TC-EMP-04 (Teste_PedirEmprestado.xlsx)
const assert = require('assert');
const h = require('./helpers');
const api = require('./api');

let D; // carregado em before(): só falha se faltar o dados.json

describe('RF05 - Pedir emprestado', function () {
    this.timeout(180000);
    let driver;

    before(() => { D = h.lerDados(); });

    beforeEach(async function () {
        driver = await h.criarDriver();
        await h.esperarTexto(driver, 'Forgot password?');
        await h.iniciarSessao(driver, h.USER1);
    });

    afterEach(async function () {
        if (this.currentTest.state === 'failed') await h.screenshot(driver, this.currentTest.title);
        await driver.deleteSession();
    });

    async function abrirPedido(titulo) {
        await h.abrirRecurso(driver, titulo);
        await h.tocarBotao(driver, 'Ask for loan');
        await h.esperarTexto(driver, 'Borrow Item');
    }

    const submeter = () => h.botao(driver, 'Submit Request');
    const hoje = () => h.iso(new Date());

    // TC-EMP-01 | CA05.1, CA05.4 e CA05.5 | Use Case Testing
    it('TC-EMP-01 #1 pedido de empréstimo válido', async function () {
        await abrirPedido(D.emprestimo[0]);
        assert.ok(await h.visivel(h.porTextoContem(driver, '1 semestre')), 'devia mostrar a duração do dono');
        assert.ok(await h.visivel(h.porTexto(driver, 'Campus IPCA')), 'devia mostrar as zonas do dono');
        assert.ok(await h.visivel(h.porTexto(driver, hoje())), 'data de início = hoje');
        assert.ok(await h.visivel(h.porTexto(driver, 'Select')), 'data de fim vazia');
        await h.escolherData(driver, 'Select', 7);
        await h.escolherPontoEncontro(driver);
        await (await submeter()).click();
        assert.ok(await h.desapareceu(h.porTexto(driver, 'Borrow Item'), 20000), 'pedido criado: devia sair do ecrã');
    });

    // TC-EMP-02 | CA05.2 | Boundary Value Analysis
    // No seletor da data de fim, a app desativa os dias iguais ou anteriores ao início (hoje).
    // Para #2 e #3 o teste confirma isso e depois que o botão fica desativado (fim por escolher).
    describe('TC-EMP-02 Confirmar: datas e ponto de encontro', function () {
        // [id, dias até ao fim (null = não escolher), com ponto?, botão ativo?]
        const linhas = [
            ['#1', 1, true, true],
            ['#2', 0, true, false],
            ['#3', -1, true, false],
            ['#4', null, true, false],
            ['#5', 7, false, false]
        ];
        linhas.forEach(([id, fim, ponto, ativo]) => {
            it(`${id} fim=${fim === null ? 'vazia' : 'hoje' + (fim >= 0 ? '+' : '') + fim} ponto=${ponto ? 'sim' : 'não'} -> ${ativo ? 'ativo' : 'desativado'}`, async function () {
                await abrirPedido(D.emprestimo[1]);
                if (fim !== null && fim <= 0) {
                    assert.strictEqual(await h.diaSelecionavel(driver, 'Select', fim), false,
                        `o dia hoje${fim ? fim : ''} não devia poder ser escolhido como fim`);
                } else if (fim !== null) {
                    await h.escolherData(driver, 'Select', fim);
                }
                if (ponto) await h.escolherPontoEncontro(driver);
                assert.strictEqual(await (await submeter()).isEnabled(), ativo);
            });
        });
    });

    // TC-EMP-03 | CA05.3 | Boundary Value Analysis
    describe('TC-EMP-03 Data de início não anterior a hoje', function () {
        it('#1 início = hoje - 1 dia: rejeitado ou botão desativado', async function () {
            await abrirPedido(D.emprestimo[1]);
            await h.escolherData(driver, hoje(), -1);
            await h.escolherData(driver, 'Select', 7);
            await h.escolherPontoEncontro(driver);
            const ontemAceite = await h.visivel(h.porTexto(driver, h.iso(h.hojeMais(-1))), 2000);
            const desativado = !(await (await submeter()).isEnabled());
            assert.ok(!ontemAceite || desativado, 'a data de ontem foi aceite e o botão está ativo');
        });
        it('#2 início = hoje: datas aceites e botão ativo', async function () {
            await abrirPedido(D.emprestimo[1]);
            await h.escolherData(driver, 'Select', 7);
            await h.escolherPontoEncontro(driver);
            assert.ok(await (await submeter()).isEnabled(), 'o botão devia estar ativo');
        });
    });

    // TC-EMP-04 | CA05.6 | Error Guessing
    // A contagem é feita pela API: o histórico da app só mostra os pedidos que cabem no ecrã.
    describe('TC-EMP-04 Pedido único e recurso próprio', function () {
        it('#1 duplo toque em Submit Request cria um só pedido', async function () {
            const antes = await api.contarEmprestimos(h.USER1);
            await abrirPedido(D.emprestimo[2]);
            await h.escolherData(driver, 'Select', 7);
            await h.escolherPontoEncontro(driver);
            await h.duploToque(driver, await submeter());
            assert.ok(await h.desapareceu(h.porTexto(driver, 'Borrow Item'), 20000), 'devia sair do ecrã de pedido');
            await driver.pause(2000); // dar tempo a um eventual 2.º pedido chegar ao servidor
            const depois = await api.contarEmprestimos(h.USER1);
            assert.strictEqual(depois - antes, 1, 'devia existir exatamente 1 pedido novo');
        });
        it('#2 recurso próprio: pedido não permitido', async function () {
            await h.abrirRecurso(driver, D.proprioEmprestimo);
            assert.ok(await h.visivel(h.porTexto(driver, 'Edit'), h.TIMEOUT), 'devia mostrar a barra do dono ("Edit")');
            assert.ok(!(await h.visivel(h.porTexto(driver, 'Ask for loan'), 1000)), 'não devia existir "Ask for loan"');
        });
    });
});
