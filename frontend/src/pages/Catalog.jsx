import { SlidersHorizontal, X } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router';
import BookCard from '../components/BookCard.jsx';
import { Empty, GridSkeleton, Pager } from '../components/Bits.jsx';
import { qs } from '../lib/api.js';
import { FORMAT, plural } from '../lib/format.js';
import { useFetch, useTitle } from '../lib/hooks.js';

const SORTS = [
  ['popular', 'Mais vendidos'], ['recent', 'Novidades'], ['rating', 'Mais bem avaliados'],
  ['price_asc', 'Menor preço'], ['price_desc', 'Maior preço'], ['title', 'Título (A–Z)'],
];
const PRICES = [['', '', 'Qualquer preço'], ['', '30', 'Até R$ 30'], ['30', '60', 'R$ 30 a R$ 60'], ['60', '100', 'R$ 60 a R$ 100'], ['100', '', 'Acima de R$ 100']];

export default function Catalog({ mode }) {
  const { slug } = useParams();
  const [params, setParams] = useSearchParams();
  const [showFilters, setShowFilters] = useState(false);
  const categories = useFetch('/api/categories');
  const catalogInfo = useFetch(mode === 'catalog' ? `/api/catalogs/${slug}` : null);

  const q = params.get('q') || '';
  const category = mode === 'category' ? slug : params.get('categoria') || '';
  const format = params.get('formato') || '';
  const min = params.get('min') || '';
  const max = params.get('max') || '';
  const inStock = params.get('estoque') === '1';
  const sort = params.get('ordem') || 'popular';
  const page = Number(params.get('pagina') || 0);

  const path = `/api/books${qs({ q, category, catalog: mode === 'catalog' ? slug : '', format, minPrice: min, maxPrice: max, inStock, sort, page, size: 24 })}`;
  const { data, loading } = useFetch(path);
  const cat = categories.data?.find((c) => c.slug === category);

  const title = mode === 'catalog' ? catalogInfo.data?.name : mode === 'category' ? cat?.name : q ? `Resultados para “${q}”` : 'Todos os livros';
  const lead = mode === 'catalog' ? catalogInfo.data?.description : mode === 'category' ? cat?.description : 'Busque, filtre e encontre a próxima leitura.';
  useTitle(title || 'Livros');

  function update(next) {
    const p = new URLSearchParams(params);
    Object.entries(next).forEach(([k, v]) => (v === '' || v === null || v === undefined || v === false ? p.delete(k) : p.set(k, v)));
    if (!('pagina' in next)) p.delete('pagina');
    setParams(p, { replace: false });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  const activeChips = useMemo(() => {
    const chips = [];
    if (q) chips.push(['q', `“${q}”`]);
    if (mode !== 'category' && category) chips.push(['categoria', categories.data?.find((c) => c.slug === category)?.name || category]);
    if (format) chips.push(['formato', FORMAT[format]]);
    if (min || max) chips.push(['preco', PRICES.find((p) => p[0] === min && p[1] === max)?.[2] || 'Preço']);
    if (inStock) chips.push(['estoque', 'Em estoque']);
    return chips;
  }, [q, category, format, min, max, inStock, mode, categories.data]);

  return (
    <div className="container page">
      <div className="listing-head">
        <div className="crumbs">
          <Link to="/">Início</Link><span>/</span>
          {mode === 'category' ? <><Link to="/categorias">Categorias</Link><span>/</span></> : mode === 'catalog' ? <><span>Catálogos</span><span>/</span></> : null}
          <span>{title || '...'}</span>
        </div>
        {mode === 'catalog' && catalogInfo.data?.highlight && <span className="eyebrow">{catalogInfo.data.highlight}</span>}
        <h1>{title || <span className="skeleton" style={{ display: 'inline-block', width: 260, height: 40 }} />}</h1>
        {lead && <p>{lead}</p>}
      </div>

      <div className="listing">
        <aside className={`filters ${showFilters ? '' : 'collapsed'}`}>
          {mode !== 'category' && (
            <div>
              <h4>Categoria</h4>
              <div className="filter-list">
                <button type="button" className={!category ? 'on' : ''} onClick={() => update({ categoria: '' })}>Todas</button>
                {categories.data?.map((c) => (
                  <button key={c.slug} type="button" className={category === c.slug ? 'on' : ''} onClick={() => update({ categoria: c.slug })}>
                    <span className="row" style={{ gap: 8 }}><span className="dot" style={{ width: 8, height: 8, borderRadius: 4, background: c.accent, border: '1px solid var(--line)' }} />{c.name}</span>
                    <span className="n">{c.bookCount}</span>
                  </button>
                ))}
              </div>
            </div>
          )}
          <div>
            <h4>Formato</h4>
            <div className="filter-list">
              <button type="button" className={!format ? 'on' : ''} onClick={() => update({ formato: '' })}>Todos</button>
              {Object.entries(FORMAT).map(([k, v]) => (
                <button key={k} type="button" className={format === k ? 'on' : ''} onClick={() => update({ formato: k })}>{v}</button>
              ))}
            </div>
          </div>
          <div>
            <h4>Preço</h4>
            <div className="filter-list">
              {PRICES.map(([a, b, label]) => (
                <button key={label} type="button" className={min === a && max === b ? 'on' : ''} onClick={() => update({ min: a, max: b })}>{label}</button>
              ))}
            </div>
          </div>
          <label className="check"><input type="checkbox" checked={inStock} onChange={(e) => update({ estoque: e.target.checked ? '1' : '' })} /> Só o que tem em estoque</label>
        </aside>

        <div>
          <div className="toolbar">
            <div className="row wrap" style={{ gap: 8 }}>
              <span className="muted" style={{ fontWeight: 700 }}>{data ? plural(data.total, 'livro', 'livros') : 'Carregando...'}</span>
              {activeChips.map(([k, label]) => (
                <button key={k} type="button" className="chip on fade" onClick={() => update(k === 'preco' ? { min: '', max: '' } : { [k]: '' })}>
                  {label} <X size={13} />
                </button>
              ))}
            </div>
            <div className="row">
              <button type="button" className="btn btn-sm mobile-only" onClick={() => setShowFilters((s) => !s)}><SlidersHorizontal size={15} /> Filtros</button>
              <select className="select" value={sort} onChange={(e) => update({ ordem: e.target.value })} aria-label="Ordenar">
                {SORTS.map(([k, v]) => <option key={k} value={k}>{v}</option>)}
              </select>
            </div>
          </div>
          {loading && !data ? <GridSkeleton /> : data?.items.length ? (
            <div className="book-grid" key={path} style={{ opacity: loading ? 0.55 : 1, transition: 'opacity .2s' }}>
              {data.items.map((b, i) => <BookCard key={b.id} book={b} index={i} />)}
            </div>
          ) : (
            <Empty title="Nenhum livro por aqui" action={<button type="button" className="btn btn-dark" onClick={() => setParams({})}>Limpar filtros</button>}>
              Tente outra palavra ou tire algum filtro.
            </Empty>
          )}
          {data && <Pager page={data.page} totalPages={data.totalPages} onPage={(p) => update({ pagina: p })} />}
        </div>
      </div>
    </div>
  );
}
