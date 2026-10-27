// Cria os recursos de que os testes de compra e empréstimo precisam.
// Corre PRIMEIRO (nome 00-...). Os títulos levam um sufixo único para cada execução,
// porque uma compra/pedido "gasta" o recurso. Os títulos ficam em dados.json.
const h = require('./helpers');

describe('Preparação dos dados de teste', function () {
    this.timeout(900000);
    let driver;
    const suf = String(Date.now()).slice(-5);
    const dados = {
        venda: [1, 2, 3, 4, 5, 6].map((i) => `Livro ${suf}-${i}`),
        emprestimo: [1, 2, 3].map((i) => `Calculadora ${suf}-${i}`),
        proprioVenda: `Livro Proprio ${suf}`,
        proprioEmprestimo: `Calculadora Propria ${suf}`
    };

    beforeEach(async () => { driver = await h.criarDriver(); await h.esperarTexto(driver, 'Forgot password?'); });
    afterEach(async function () {
        if (this.currentTest.state === 'failed') await h.screenshot(driver, this.currentTest.title);
        await driver.deleteSession();
    });

    it('aluno2 publica os recursos de outro utilizador (venda e empréstimo)', async () => {
        await h.iniciarSessao(driver, h.USER2);
        for (const titulo of dados.venda) await h.publicarRecurso(driver, { tipo: 'SELL', titulo, preco: '12.50' });
        for (const titulo of dados.emprestimo) {
            await h.publicarRecurso(driver, { tipo: 'BORROW', titulo, duracao: '1 semestre', zonas: 'Campus IPCA' });
        }
    });

    it('aluno1 publica os seus próprios recursos', async () => {
        await h.iniciarSessao(driver, h.USER1);
        await h.publicarRecurso(driver, { tipo: 'SELL', titulo: dados.proprioVenda, preco: '5' });
        await h.publicarRecurso(driver, { tipo: 'BORROW', titulo: dados.proprioEmprestimo, duracao: '1 semestre', zonas: 'Campus IPCA' });
        h.guardarDados(dados);
    });
});
