// Corre os casos de teste pela "Execution Order" definida no Jira (RQA-9 a RQA-28).
// Uso: node run-ordem.js   (ou "npm run test:ordem", se o acrescentares ao package.json)
const { execSync } = require('child_process');

// [ficheiro, caso de teste, ordem no Jira]
const ORDEM = [
    ['login', 'TC-LOG-01', 1],
    ['registo', 'TC-REG-01', 2],
    ['publicar', 'TC-PUB-01', 3],
    ['comprar', 'TC-COM-01', 4],
    ['emprestimo', 'TC-EMP-01', 5],
    ['login', 'TC-LOG-02', 6],
    ['registo', 'TC-REG-02', 7],
    ['publicar', 'TC-PUB-02', 8],
    ['comprar', 'TC-COM-02', 9],
    ['emprestimo', 'TC-EMP-02', 10],
    ['login', 'TC-LOG-03', 11],
    ['registo', 'TC-REG-03', 12],
    ['publicar', 'TC-PUB-03', 13],
    ['comprar', 'TC-COM-03', 14],
    ['emprestimo', 'TC-EMP-03', 15],
    ['login', 'TC-LOG-04', 16],
    ['registo', 'TC-REG-04', 17],
    ['comprar', 'TC-COM-04', 18],
    ['emprestimo', 'TC-EMP-04', 19],
    ['comprar', 'TC-COM-05', 20]
];

const correr = (cmd) => {
    try { execSync(cmd, { stdio: 'inherit' }); return true; } catch { return false; }
};

// Pré-condição de todos os casos: dados de teste criados
if (!correr('npx mocha 00-preparar-dados.test.js --timeout 120000')) {
    console.error('\nA preparação dos dados falhou: a execução foi interrompida.');
    process.exit(1);
}

const resultados = [];
for (const [ficheiro, tc, ordem] of ORDEM) {
    console.log(`\n========== ${ordem}. ${tc} (${ficheiro}.test.js) ==========`);
    const ok = correr(`npx mocha ${ficheiro}.test.js --timeout 180000 --grep "${tc}"`);
    resultados.push({ ordem, tc, resultado: ok ? 'PASSOU' : 'FALHOU' });
}

console.log('\n========== Resumo pela ordem de execução do Jira ==========');
console.table(resultados);
process.exitCode = resultados.some((r) => r.resultado === 'FALHOU') ? 1 : 0;