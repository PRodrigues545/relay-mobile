// Acesso direto à API do backend (sem passar pela interface). Usado para:
// - preparar os dados de teste (mais rápido e não depende do ecrã de publicar, que também é testado);
// - verificar resultados que a interface não mostra bem (ex.: "foi criada exatamente 1 compra");
// - apagar os recursos criados pelos testes (npm run limpar).
const API = (process.env.RELAY_API || 'https://eva-insensitive-mutually.ngrok-free.dev/api/').replace(/\/?$/, '/');

async function pedido(caminho, opcoes = {}) {
    const r = await fetch(API + caminho, {
        ...opcoes,
        headers: { 'Content-Type': 'application/json', 'ngrok-skip-browser-warning': '1', ...(opcoes.headers || {}) }
    });
    const corpo = await r.text();
    if (!r.ok) throw new Error(`${opcoes.method || 'GET'} ${caminho} respondeu ${r.status}: ${corpo.slice(0, 300)}`);
    try { return JSON.parse(corpo); } catch { return corpo; }
}

const tokens = new Map();
async function token(user) {
    if (!tokens.has(user.email)) {
        const r = await pedido('auth/login', { method: 'POST', body: JSON.stringify({ email: user.email, password: user.password }) });
        tokens.set(user.email, r.accessToken);
    }
    return tokens.get(user.email);
}

const comToken = async (user, caminho, opcoes = {}) =>
    pedido(caminho, { ...opcoes, headers: { Authorization: `Bearer ${await token(user)}` } });

// ---------------------------------------------------------------- histórico
const lista = (x) => (Array.isArray(x) ? x : []);
const contarCompras = async (user) => lista(await comToken(user, 'operation/purchase/history')).length;
const contarEmprestimos = async (user) => lista(await comToken(user, 'operation/borrow/history')).length;

// ---------------------------------------------------------------- recursos
async function primeiraCategoria(user) {
    const cats = lista(await comToken(user, 'resources/categories'));
    if (!cats.length) throw new Error('A API não devolveu categorias');
    return cats[0].id;
}

// r = { titulo, tipo: 'SELL'|'BORROW', preco?, duracao?, zonas? } — os mesmos campos que a app envia
async function criarRecurso(user, r) {
    return comToken(user, 'resources/create', {
        method: 'POST',
        body: JSON.stringify({
            title: r.titulo,
            description: '',
            type: r.tipo,
            condition: 'GOOD',
            price: r.tipo === 'BORROW' ? null : Number(r.preco),
            categoryId: await primeiraCategoria(user),
            borrowDuration: r.tipo === 'SELL' ? null : r.duracao,
            zones: r.tipo === 'SELL' ? null : r.zonas
        })
    });
}

const todosRecursos = async (user) => lista(await comToken(user, 'resources/all'));
const procurarRecurso = async (user, titulo) => (await todosRecursos(user)).find((x) => x.title === titulo) || null;
const apagarRecurso = (user, id) => comToken(user, `resources/delete/${id}`, { method: 'DELETE' });

module.exports = {
    API, contarCompras, contarEmprestimos, criarRecurso, todosRecursos, procurarRecurso, apagarRecurso
};
