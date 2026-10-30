// Apaga os recursos criados pelos testes (títulos com os padrões abaixo, dos utilizadores de teste).
//   node limpar.js           -> só LISTA o que seria apagado
//   node limpar.js --apagar  -> apaga
// Recursos com compras ou pedidos associados podem não poder ser apagados (o servidor recusa): ficam listados.
const h = require('./helpers');
const api = require('./api');

const PADROES = [
    /^Livro \d{5}-\d$/,                 // 00-preparar-dados
    /^Calculadora \d{5}-\d$/,
    /^Livro Proprio \d{5}$/,
    /^Calculadora Propria \d{5}$/,
    /^Livro de Cálculo( \d{5}-\d)?$/,  // publicar.test.js
    /^Calculadora científica( \d{5}-\d)?$/
];

(async () => {
    const apagar = process.argv.includes('--apagar');
    const todos = await api.todosRecursos(h.USER1);
    const alvo = todos.filter((r) => PADROES.some((p) => p.test(r.title || '')));
    console.log(`${alvo.length} recursos de teste encontrados (de ${todos.length} no total).`);
    for (const r of alvo) console.log(`  #${r.id}  ${r.title}  (dono ${r.owner_id})`);
    if (!apagar) { console.log('\nNada foi apagado. Para apagar: node limpar.js --apagar'); return; }

    let ok = 0;
    for (const r of alvo) {
        // só o dono pode apagar: tentar com cada conta de teste
        let apagado = false;
        for (const u of [h.USER1, h.USER2]) {
            try { await api.apagarRecurso(u, r.id); apagado = true; break; } catch { /* não é o dono */ }
        }
        if (apagado) ok++; else console.log(`  não foi possível apagar #${r.id} ${r.title}`);
    }
    console.log(`${ok} de ${alvo.length} apagados.`);
})().catch((e) => { console.error(e.message); process.exit(1); });