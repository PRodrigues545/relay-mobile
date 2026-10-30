// Cria, pela API, os recursos de que os testes de compra e empréstimo precisam.
// Não usa a interface: os dados de preparação não devem depender do ecrã de publicar,
// que é ele próprio testado em publicar.test.js.
// Os títulos levam um sufixo único em cada execução, porque uma compra/pedido "gasta" o recurso.
// Os títulos ficam em dados.json.
const assert = require('assert');
const h = require('./helpers');
const api = require('./api');

describe('Preparação dos dados de teste (via API)', function () {
    this.timeout(120000);
    const suf = String(Date.now()).slice(-5);
    const dados = {
        venda: [1, 2, 3, 4, 5, 6].map((i) => `Livro ${suf}-${i}`),
        emprestimo: [1, 2, 3].map((i) => `Calculadora ${suf}-${i}`),
        proprioVenda: `Livro Proprio ${suf}`,
        proprioEmprestimo: `Calculadora Propria ${suf}`
    };

    it('aluno2 publica os recursos de outro utilizador (venda e empréstimo)', async () => {
        for (const titulo of dados.venda) await api.criarRecurso(h.USER2, { tipo: 'SELL', titulo, preco: 12.5 });
        for (const titulo of dados.emprestimo) {
            await api.criarRecurso(h.USER2, { tipo: 'BORROW', titulo, duracao: '1 semestre', zonas: 'Campus IPCA' });
        }
    });

    it('aluno1 publica os seus próprios recursos', async () => {
        await api.criarRecurso(h.USER1, { tipo: 'SELL', titulo: dados.proprioVenda, preco: 5 });
        await api.criarRecurso(h.USER1, { tipo: 'BORROW', titulo: dados.proprioEmprestimo, duracao: '1 semestre', zonas: 'Campus IPCA' });
    });

    it('os recursos existem no servidor', async () => {
        const titulos = new Set((await api.todosRecursos(h.USER1)).map((r) => r.title));
        const todos = [...dados.venda, ...dados.emprestimo, dados.proprioVenda, dados.proprioEmprestimo];
        const faltam = todos.filter((t) => !titulos.has(t));
        assert.deepStrictEqual(faltam, [], `recursos em falta: ${faltam.join(', ')}`);
        h.guardarDados(dados);
    });
});
