// RF04 - Comprar | TC-COM-01 a TC-COM-05 (Teste_Comprar.xlsx)
// O aluno1 compra recursos do aluno2 (criados por 00-preparar-dados.test.js, ficheiro dados.json).
const assert = require('assert');
const h = require('./helpers');
const api = require('./api');

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
        if (c[3]) await c[3].setValue('47500');
        await h.esconderTeclado(driver);
        const pagar = driver.$('//android.widget.TextView[starts-with(@text, "Pay €")]');
        await pagar.waitForDisplayed({ timeout: h.TIMEOUT });
        await pagar.click();
    }

    // TC-COM-01 | CA04.1 e CA04.3 | Use Case Testing
    describe('TC-COM-01 Compra com numerário, MBWay e transferência', function () {
        [['#1', 0, 'Cash Payment'], ['#2', 1, 'MBWAY']].forEach(([id, i, metodo]) => {
            it(`${id} ${metodo}: compra criada sem abrir o Stripe`, async function () {
                await abrirCompra(D.venda[i]);
                await h.escolherPontoEncontro(driver);
                await h.escolherPagamento(driver, metodo);
                assert.ok(await (await h.botao(driver, 'Buy')).isEnabled(), 'o botão Buy devia estar ativo');
                await h.tocarBotao(driver, 'Buy');
                assert.ok(await h.visivel(h.porTexto(driver, 'You are the buyer'), 40000), 'devia abrir o detalhe da compra');
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

    // TC-COM-02 | CA04.2 | Decision Table Testing
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

    // TC-COM-03 | CA04.4 | Error Guessing
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

    // TC-COM-04 | CA04.5 | State Transition Testing
    // A contagem é feita pela API: o histórico da app só mostra as compras que cabem no ecrã.
    it('TC-COM-04 #1 duplo toque em Buy cria uma única compra', async function () {
        const antes = await api.contarCompras(h.USER1);
        await abrirCompra(D.venda[5]);
        await h.escolherPontoEncontro(driver);
        await h.escolherPagamento(driver, 'Cash Payment');
        await h.duploToque(driver, await h.botao(driver, 'Buy'));
        assert.ok(await h.desapareceu(h.porTexto(driver, 'Total to pay'), 20000), 'devia sair do ecrã de compra');
        await driver.pause(2000); // dar tempo a um eventual 2.º pedido chegar ao servidor
        const depois = await api.contarCompras(h.USER1);
        assert.strictEqual(depois - antes, 1, 'devia existir exatamente 1 compra nova');
    });

    // TC-COM-05 | CA04.6 | Equivalence Partitioning
    describe('TC-COM-05 Recurso próprio ou só para empréstimo', function () {
        it('#1 recurso próprio: compra não permitida', async function () {
            await h.abrirRecurso(driver, D.proprioVenda);
            // primeiro confirmar que o ecrã carregou (barra do dono com "Edit"); sem isto, o teste
            // passava mesmo que o detalhe ainda não tivesse aparecido
            assert.ok(await h.visivel(h.porTexto(driver, 'Edit'), h.TIMEOUT), 'devia mostrar a barra do dono ("Edit")');
            assert.ok(!(await h.visivel(h.porTexto(driver, 'Buy now'), 1000)), 'não devia existir "Buy now"');
        });
        it('#2 recurso de outro só para empréstimo: compra não disponível', async function () {
            await h.abrirRecurso(driver, D.emprestimo[1]);
            assert.ok(await h.visivel(h.porTexto(driver, 'Ask for loan'), h.TIMEOUT), 'devia oferecer "Ask for loan"');
            assert.ok(!(await h.visivel(h.porTexto(driver, 'Buy now'), 1000)), 'não devia existir "Buy now"');
        });
    });
});
