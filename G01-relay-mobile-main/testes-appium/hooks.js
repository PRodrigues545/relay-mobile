// Hooks globais do Mocha (carregados pelo .mocharc.js): dão nome ao vídeo de cada teste
// e apagam-no se o teste passou. Para guardar todos os vídeos: GRAVAR_TUDO=1.
const fs = require('fs');
const h = require('./helpers');

exports.mochaHooks = {
    beforeEach() {
        h.estado.titulo = this.currentTest.fullTitle();
        h.estado.ultimoVideo = null;
    },
    afterEach() {
        const v = h.estado.ultimoVideo;
        if (v && this.currentTest.state === 'passed' && process.env.GRAVAR_TUDO !== '1') {
            try { fs.unlinkSync(v); } catch { /* ignorar */ }
        }
    }
};
