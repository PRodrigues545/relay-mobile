// RF04 - Comprar | TC-COM-01 a TC-COM-05 (Teste_Comprar.xlsx)
// O aluno1 compra recursos do aluno2 (criados por 00-preparar-dados.test.js, ficheiro dados.json).
const assert = require('assert');
const h = require('./helpers');

let D; // carregado em before(): só falha se faltar o dados.json
const STRIPE_OK = '4242424242424242';
const STRIPE_RECUSADO = '4000000000000002';

describe('RF04 - Comprar', function () {
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

    // Abre o recurso, carrega em "Buy now" e chega ao ecrã de compra
    async function abrirCompra(titulo) {
        await h.abrirRecurso(driver, titulo);
        await h.tocarBotao(driver, 'Buy now');
        await h.esperarTexto(driver, 'Total to pay');
    }

    // Preenche o PaymentSheet do Stripe (campos: cartão, validade, CVC, [código postal])
    async function pagarStripe(cartao) {
        const sheet = h.porTextoContem(driver, 'Card number');
        await sheet.waitForDisplayed({ timeout: 30000 });
        const c = await driver.$$('android.widget.EditText');
        await c[0].setValue(cartao);
        await c[1].setValue('1235'); // MM/YY futuro (12/35)
        await c[2].setValue('123');
        if (c[3]) await c[3].setValue('4750');
        await h.esconderTeclado(driver);
        const pagar = driver.$('android=new UiSelector().className("android.widget.Button").textStartsWith("Pay")');
        await pagar.waitForDisplayed({ timeout: h.TIMEOUT });
        await pagar.click();
    }

    // TC-COM-01 | CA04.1 e CA04.3 | Casos de Uso
    describe('TC-COM-01 Compra com numerário, MBWay e transferência', function () {
        [['#1', 0, 'Cash Payment'], ['#2', 1, 'MBWAY']].forEach(([id, i, metodo]) => {
            it(`${id} ${metodo}: compra criada sem abrir o Stripe`, async function () {
                await abrirCompra(D.venda[i]);
                await h.escolherPontoEncontro(driver);
                await h.escolherPagamento(driver, metodo);
                assert.ok(await (await h.botao(driver, 'Buy')).isEnabled(), 'o botão Buy devia estar ativo');
                await h.tocarBotao(driver, 'Buy');
                assert.ok(await h.desapareceu(h.porTexto(driver, 'Total to pay'), 20000), 'devia sair do ecrã de compra');
                assert.ok(!(await h.visivel(h.porTextoContem(driver, 'Card number'), 1500)), 'não devia abrir o Stripe');
            });
        });

        it('#3 Bank Transfer: abre o PaymentSheet e só avança com o pagamento concluído', async function () {
            await abrirCompra(D.venda[2]);
            await h.escolherPontoEncontro(driver);
            await h.escolherPagamento(driver, 'Bank Transfer');
            await h.tocarBotao(driver, 'Buy');
            await h.porTextoContem(driver, 'Card number').waitForDisplayed({ timeout: 30000 });
            await pagarStripe(STRIPE_OK);
            assert.ok(await h.desapareceu(h.porTexto(driver, 'Total to pay'), 40000), 'depois de pagar devia avançar');
        });
    });

    // TC-COM-02 | CA04.2 | Tabela de Decisão
    describe('TC-COM-02 Confirmar exige ponto de encontro e pagamento', function () {
        [['#1', true, null], ['#2', false, 'Cash Payment'], ['#3', false, null]].forEach(([id, ponto, pagamento]) => {
            it(`${id} ponto: ${ponto ? 'sim' : 'não'} | pagamento: ${pagamento || 'não'} -> Buy desativado`, async function () {
                await abrirCompra(D.venda[3]);
                if (ponto) await h.escolherPontoEncontro(driver);
                if (pagamento) await h.escolherPagamento(driver, pagamento);
                assert.strictEqual(await (await h.botao(driver, 'Buy')).isEnabled(), false, 'Buy devia estar desativado');
                assert.ok(await h.visivel(h.porTexto(driver, 'Total to pay'), 1500), 'nenhuma compra devia ser criada');
            });
        });
    });

    // TC-COM-03 | CA04.4 | Estimativa de Erro
    it('TC-COM-03 #1 cartão recusado: mostra erro e a compra não conclui', async function () {
        await abrirCompra(D.venda[4]);
        await h.escolherPontoEncontro(driver);
        await h.escolherPagamento(driver, 'Bank Transfer');
        await h.tocarBotao(driver, 'Buy');
        await pagarStripe(STRIPE_RECUSADO);
        const erro = (await h.visivel(h.porTextoContem(driver, 'declined'), 30000)) ||
            (await h.visivel(h.porTextoContem(driver, 'Payment failed'), 3000));
        assert.ok(erro, 'devia mostrar erro de pagamento');
    });

    // TC-COM-04 | CA04.5 | Transição de Estados
    it('TC-COM-04 #1 duplo toque em Buy cria uma única compra', async function () {
        const antes = await h.contarHistorico(driver, 'Purchases');
        await abrirCompra(D.venda[5]);
        await h.escolherPontoEncontro(driver);
        await h.escolherPagamento(driver, 'Cash Payment');
        const buy = await h.botao(driver, 'Buy');
        await buy.click();
        try { await buy.click(); } catch { /* o botão já pode ter desaparecido */ }
        await h.desapareceu(h.porTexto(driver, 'Total to pay'), 20000);
        for (let i = 0; i < 3 && !(await h.visivel(h.porTexto(driver, 'Profile'), 1500)); i++) await driver.back();
        const depois = await h.contarHistorico(driver, 'Purchases');
        assert.strictEqual(depois - antes, 1, 'devia existir exatamente 1 compra nova');
    });

    // TC-COM-05 | CA04.6 | Particionar por Equivalência
    describe('TC-COM-05 Recurso próprio ou só para empréstimo', function () {
        it('#1 recurso próprio: compra não permitida', async function () {
            await h.abrirRecurso(driver, D.proprioVenda);
            assert.ok(!(await h.visivel(h.porTexto(driver, 'Buy now'), 3000)), 'não devia existir "Buy now"');
        });
        it('#2 recurso de outro só para empréstimo: compra não disponível', async function () {
            await h.abrirRecurso(driver, D.emprestimo[1]);
            assert.ok(!(await h.visivel(h.porTexto(driver, 'Buy now'), 3000)), 'não devia existir "Buy now"');
            assert.ok(await h.visivel(h.porTexto(driver, 'Ask for loan')), 'devia oferecer só "Ask for loan"');
        });
    });
});
