import { Check, Plus, Search } from 'lucide-react';
import { useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import BookCover from '../../components/BookCover.jsx';
import { Pager } from '../../components/Bits.jsx';
import { patch, qs } from '../../lib/api.js';
import { money } from '../../lib/format.js';
import { useFetch } from '../../lib/hooks.js';
import { useToast } from '../../lib/toast.jsx';

function StockCell({ book, onSaved }) {
  const [v, setV] = useState(book.stockQuantity);
  const toast = useToast();
  const dirty = Number(v) !== book.stockQuantity;
  async function save() {
    try {
      onSaved(await patch(`/api/admin/books/${book.id}/stock`, { stockQuantity: Number(v) }));
      toast(`Estoque de “${book.title}” atualizado`);
    } catch (e) { toast(e.message, { error: true }); }
  }
  return (
    <div className="row" style={{ gap: 6 }}>
      <input className="input" type="number" min="0" style={{ width: 84, height: 36 }} value={v} onChange={(e) => setV(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && dirty && save()} />
      {dirty && <button type="button" className="btn btn-primary btn-icon btn-sm fade" onClick={save} aria-label="Salvar estoque"><Check size={15} /></button>}
    </div>
  );
}

export default function AdminBooks() {
  const [params, setParams] = useSearchParams();
  const q = params.get('q') || '';
  const page = Number(params.get('pagina') || 0);
  const [term, setTerm] = useState(q);
  const { data, setData } = useFetch(`/api/admin/books${qs({ q, page, size: 15 })}`);
  const replace = (b) => setData({ ...data, items: data.items.map((x) => (x.id === b.id ? b : x)) });
  return (
    <div className="stack fade" style={{ gap: 18 }}>
      <div className="row between wrap"><h1 style={{ fontSize: 36 }}>Livros e estoque</h1>
        <div className="row">
          <form className="search" style={{ maxWidth: 280 }} onSubmit={(e) => { e.preventDefault(); setParams(term ? { q: term } : {}); }}>
            <Search className="icon" /><input value={term} onChange={(e) => setTerm(e.target.value)} placeholder="Título, autor, ISBN" />
          </form>
          <Link to="/admin/livros/novo" className="btn btn-primary"><Plus size={16} /> Novo livro</Link>
        </div>
      </div>
      <div className="card" style={{ padding: 0 }}>
        <div className="table-wrap">
          <table className="table">
            <thead><tr><th>Livro</th><th>Categoria</th><th>Preço</th><th>Vendidos</th><th>Estoque</th><th /></tr></thead>
            <tbody>
              {data?.items.map((b) => (
                <tr key={b.id} style={{ opacity: b.active ? 1 : 0.5 }}>
                  <td>
                    <div className="row" style={{ gap: 12 }}>
                      <div style={{ width: 34, flex: 'none' }}><BookCover title={b.title} style={b.coverStyle} showText={false} /></div>
                      <div><b>{b.title}</b><div className="subtle">{b.authors.join(', ')} · {b.isbn}</div></div>
                    </div>
                  </td>
                  <td className="subtle">{b.categoryName}</td>
                  <td style={{ fontWeight: 700 }}>{money(b.price)}</td>
                  <td className="subtle">{b.soldCount}</td>
                  <td><StockCell key={`${b.id}-${b.stockQuantity}`} book={b} onSaved={replace} /></td>
                  <td><Link to={`/admin/livros/${b.id}`} className="btn btn-sm">Editar</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {data && <Pager page={data.page} totalPages={data.totalPages} onPage={(p) => setParams({ ...(q ? { q } : {}), pagina: p })} />}
    </div>
  );
}
