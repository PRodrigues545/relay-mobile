// RF03 - Publicar recurso | TC-PUB-01 a TC-PUB-04 (Teste_PublicarRecurso.xlsx)
// Os recursos são publicados pelo aluno2 (assim o aluno1 nunca é "dono" deles).
const assert = require('assert');
const h = require('./helpers');
const api = require('./api');

// Sufixo único por execução: cada recurso publicado tem um título próprio,
// para se poder confirmar na API se foi (ou não) criado no servidor.
const SUF = String(Date.now()).slice(-5);
const tituloUnico = (base, id) => `${base} ${SUF}-${id.replace('#', '')}`;

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

    // Resultado completo de uma publicação: o que a interface fez E o que ficou no servidor
    async function resultadoPublicacao(titulo) {
        const fechou = await formularioFechou();
        const criado = !!(await api.procurarRecurso(h.USER2, titulo));
        return { fechou, criado, txt: `formulário ${fechou ? 'fechou' : 'ficou aberto'}, recurso ${criado ? 'CRIADO' : 'não criado'} no servidor` };
    }

    function confirmarPublicado({ fechou, criado, txt }) {
        assert.ok(criado, `devia criar o recurso no servidor (${txt})`);
        assert.ok(fechou, `o recurso foi criado mas o formulário não fechou: defeito na app (${txt})`);
    }

    function confirmarRejeitado({ fechou, criado, txt }) {
        assert.ok(!criado, `o servidor aceitou um valor que devia ser rejeitado (${txt})`);
        assert.ok(!fechou, `o formulário devia ficar aberto com uma mensagem de erro (${txt})`);
    }

    // TC-PUB-01 | CA03.3 | Boundary Value Analysis
    describe('TC-PUB-01 Venda: preço obrigatório e superior a 0', function () {
        // [id, preço, publica?, descrição]; publica = null -> o "-" tem de ser recusado pelo próprio campo
        const linhas = [
            ['#1', '12.50', true, 'publicado'],
            ['#2', '0.01', true, 'publicado (limite inferior válido)'],
            ['#3', '0', false, 'preço rejeitado: tem de ser superior a 0'],
            ['#4', '-5', null, 'rejeitado pelo campo (não aceita o sinal "-")'],
            ['#5', '', false, 'preço rejeitado: é obrigatório']
        ];

        linhas.forEach(([id, preco, publica, descricao]) => {
            it(`${id} preço "${preco}" -> ${descricao}`, async function () {
                await h.abrirFormularioPublicar(driver);
                if (publica === null) {
                    // #4: o campo de preço só aceita dígitos e ".", por isso "-5" nem chega a ser escrito.
                    // O resultado esperado do Excel ("preço rejeitado") cumpre-se no próprio campo.
                    await h.preencherFormulario(driver, { tipo: 'SELL', titulo: 'Livro de Cálculo' });
                    const campo = driver.$('//android.widget.TextView[@text="Price:"]/following::android.widget.EditText[1]');
                    for (let i = 0; i < 6 && !(await h.visivel(campo, 800)); i++) await h.scroll(driver, 'down', 1);
                    await campo.setValue(preco);
                    const lido = String(await campo.getText());
                    assert.ok(!lido.includes('-'), `o campo devia recusar o "-", mas ficou com "${lido}"`);
                    return;
                }
                const titulo = tituloUnico('Livro de Cálculo', id);
                await h.preencherFormulario(driver, { tipo: 'SELL', titulo, preco });
                await h.tocarPublicar(driver);
                const r = await resultadoPublicacao(titulo);
                if (publica) confirmarPublicado(r); else confirmarRejeitado(r);
            });
        });
    });

    // TC-PUB-02 | CA03.1 e CA03.2 | Decision Table Testing
    // A app valida a categoria ANTES do título e mostra um erro de cada vez:
    // sem categoria (#2 e #3) aparece sempre "Please select a category".
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
            it(`${id} sem categoria (${titulo ? 'com título' : 'sem título'}) -> "Please select a category"`, async function () {
                await driver.execute('mobile: setConnectivity', { wifi: false, data: false });
                await h.abrirFormularioPublicar(driver);
                await h.preencherFormulario(driver, { tipo: 'SELL', titulo, preco: '5' });
                await h.tocarPublicar(driver);
                assert.ok(await h.visivel(h.porTexto(driver, 'Please select a category'), 8000), 'devia mostrar "Please select a category"');
                assert.ok(await h.visivel(h.porTexto(driver, 'Title:'), 2000), 'nada devia ser publicado');
            });
        });
    });

    // TC-PUB-03 | CA03.4 | Equivalence Partitioning
    it('TC-PUB-03 #1 empréstimo sem preço, com duração e zonas', async function () {
        await h.abrirFormularioPublicar(driver);
        await h.tocarTexto(driver, 'Borrow');
        // os campos de duração e zonas ficam abaixo do fim do ecrã: fazer scroll até eles
        await h.scrollAteTexto(driver, 'Zones:');
        assert.ok(await h.visivel(h.porTexto(driver, 'Maximum Duration:')), 'devia mostrar a duração');
        assert.ok(await h.visivel(h.porTexto(driver, 'Zones:')), 'devia mostrar as zonas');
        assert.ok(!(await h.visivel(h.porTexto(driver, 'Price:'), 1500)), 'não devia mostrar o preço');
        await h.scroll(driver, 'up', 3); // voltar ao topo para preencher o título
        const titulo = tituloUnico('Calculadora científica', '#1');
        await h.preencherFormulario(driver, { tipo: 'BORROW', titulo, duracao: '1 semestre', zonas: 'Campus IPCA' });
        await h.tocarPublicar(driver);
        confirmarPublicado(await resultadoPublicacao(titulo));
    });
});