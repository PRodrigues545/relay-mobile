// RF03 - Publicar recurso | TC-PUB-01 a TC-PUB-03 (Teste_PublicarRecurso.xlsx)
// Os recursos são publicados pelo aluno2 (assim o aluno1 nunca é "dono" deles).
const assert = require('assert');
const h = require('./helpers');

describe('RF03 - Publicar recurso', function () {
    this.timeout(180000);
    let driver;

    beforeEach(async function () {
        driver = await h.criarDriver();
        await h.esperarTexto(driver, 'Forgot password?');
        await h.iniciarSessao(driver, h.USER2);
    });

    afterEach(async function () {
        try { await driver.execute('mobile: setConnectivity', { wifi: true, data: true }); } catch { /* ignorar */ }
        if (this.currentTest.state === 'failed') await h.screenshot(driver, this.currentTest.title);
        await driver.deleteSession();
    });

    const formularioFechou = () => h.desapareceu(h.porTexto(driver, 'Title:'), 15000);

    // TC-PUB-01 | CA03.3 | Análise de Valor Fronteira
    describe('TC-PUB-01 Venda: preço obrigatório e superior a 0', function () {
        const linhas = [
            ['#1', '12.50', true, 'publicado'],
            ['#2', '0.01', true, 'publicado (limite inferior válido)'],
            ['#3', '0', false, 'preço rejeitado: tem de ser superior a 0'],
            ['#4', '', false, 'preço rejeitado: é obrigatório']
        ];
        linhas.forEach(([id, preco, publica, descricao]) => {
            it(`${id} preço "${preco}" -> ${descricao}`, async function () {
                await h.abrirFormularioPublicar(driver);
                await h.preencherFormulario(driver, { tipo: 'SELL', titulo: 'Livro de Cálculo', preco });
                await h.tocarPublicar(driver);
                const fechou = await formularioFechou();
                assert.strictEqual(fechou, publica, publica ? 'devia publicar e fechar o formulário' : 'não devia publicar (o formulário devia ficar aberto)');
            });
        });
    });

    // TC-PUB-02 | CA03.1 e CA03.2 | Tabela de Decisão
    // A app escolhe a 1.ª categoria por omissão. Para ficar sem categoria, cortamos a rede
    // depois do login (as categorias não carregam) - ver nota do Excel.
    describe('TC-PUB-02 Título e categoria obrigatórios', function () {
        it('#1 título vazio com categoria -> "Title cannot be empty"', async function () {
            await h.abrirFormularioPublicar(driver);
            await h.preencherFormulario(driver, { tipo: 'SELL', preco: '5' });
            await h.tocarPublicar(driver);
            assert.ok(await h.visivel(h.porTexto(driver, 'Title cannot be empty'), 8000));
            assert.ok(await h.visivel(h.porTexto(driver, 'Title:'), 2000), 'nada devia ser publicado');
        });

        [['#2', 'Livro de Cálculo'], ['#3', '']].forEach(([id, titulo]) => {
            it(`${id} sem categoria (${titulo ? 'com título' : 'sem título'}) -> mensagem de campo obrigatório`, async function () {
                await driver.execute('mobile: setConnectivity', { wifi: false, data: false });
                await h.abrirFormularioPublicar(driver);
                await h.preencherFormulario(driver, { tipo: 'SELL', titulo, preco: '5' });
                await h.tocarPublicar(driver);
                const erro = (await h.visivel(h.porTexto(driver, 'Please select a category'), 8000)) ||
                    (await h.visivel(h.porTexto(driver, 'Title cannot be empty'), 1500));
                assert.ok(erro, 'devia mostrar mensagem de campo obrigatório');
                assert.ok(await h.visivel(h.porTexto(driver, 'Title:'), 2000), 'nada devia ser publicado');
            });
        });
    });

    // TC-PUB-03 | CA03.4 | Particionar por Equivalência
    it('TC-PUB-03 #1 empréstimo sem preço, com duração e zonas', async function () {
        await h.abrirFormularioPublicar(driver);
        await h.tocarTexto(driver, 'Borrow');
        assert.ok(await h.visivel(h.porTexto(driver, 'Maximum Duration:')), 'devia mostrar a duração');
        assert.ok(await h.visivel(h.porTexto(driver, 'Zones:')), 'devia mostrar as zonas');
        assert.ok(!(await h.visivel(h.porTexto(driver, 'Price:'), 1500)), 'não devia mostrar o preço');
        await h.preencherFormulario(driver, {
            tipo: 'BORROW', titulo: 'Calculadora científica', duracao: '1 semestre', zonas: 'Campus IPCA'
        });
        await h.tocarPublicar(driver);
        assert.ok(await formularioFechou(), 'devia publicar sem preço');
    });
});
