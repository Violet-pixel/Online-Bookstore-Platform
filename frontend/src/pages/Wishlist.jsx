import { Bookmark, ShoppingBag, Trash2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router';
import BookCover from '../components/BookCover.jsx';
import { Empty, Spinner } from '../components/Bits.jsx';
import { get } from '../lib/api.js';
import { useCart } from '../lib/cart.jsx';
import { date, money, plural } from '../lib/format.js';
import { useTitle } from '../lib/hooks.js';
import { useLibrary } from '../lib/library.jsx';
import { useToast } from '../lib/toast.jsx';

const SORTS = [['saved', 'Guardados por último'], ['drop', 'Maior queda de preço'], ['price', 'Menor preço'], ['title', 'Título']];

/** Quero ler: livros guardados neste navegador, com aviso de queda de preço e de volta ao estoque. */
export default function Wishlist() {
  useTitle('Quero ler');
  const { wish, removeWish, refreshWish } = useLibrary();
  const { add } = useCart();
  const toast = useToast();
  const [sort, setSort] = useState('saved');
  const [checking, setChecking] = useState(false);
  const [busy, setBusy] = useState(null);

  // Confere preço e estoque de hoje de cada livro guardado.
  useEffect(() => {
    if (!wish.length) return;
    setChecking(true);
    Promise.allSettled(wish.map((w) => get(`/api/books/${w.slug}`)))
      .then((rs) => refreshWish(rs.filter((r) => r.status === 'fulfilled').map((r) => r.value)))
      .finally(() => setChecking(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const list = useMemo(() => {
    const l = [...wish];
    if (sort === 'drop') l.sort((a, b) => (b.savedPrice - b.price) - (a.savedPrice - a.price));
    if (sort === 'price') l.sort((a, b) => a.price - b.price);
    if (sort === 'title') l.sort((a, b) => a.title.localeCompare(b.title, 'pt-BR'));
    return l;
  }, [wish, sort]);
  const total = wish.filter((w) => w.inStock).reduce((s, w) => s + w.price, 0);

  async function toCart(w, remove) {
    setBusy(w.id);
    try {
      await add(w, 1);
      if (remove) removeWish(w.id);
      toast(`“${w.title}” foi para o carrinho`, { action: { to: '/carrinho', label: 'Ver' } });
    } catch (e) {
      toast(e.message, { error: true });
    } finally {
      setBusy(null);
    }
  }
  async function allToCart() {
    setBusy('all');
    try {
      for (const w of wish.filter((x) => x.inStock)) await add(w, 1);
      toast('Os livros disponíveis foram para o carrinho', { action: { to: '/carrinho', label: 'Ver' } });
    } catch (e) {
      toast(e.message, { error: true });
    } finally {
      setBusy(null);
    }
  }

  if (!wish.length) {
    return (
      <div className="container page">
        <div className="listing-head"><h1>Quero ler</h1></div>
        <Empty title="Nenhum livro guardado ainda" action={<Link to="/livros" className="btn btn-dark">Explorar livros</Link>}>
          Toque no marcador <Bookmark size={14} style={{ display: 'inline', verticalAlign: '-2px' }} /> de um livro para guardá-lo aqui. Avisamos quando o preço baixar ou ele voltar ao estoque.
        </Empty>
      </div>
    );
  }

  return (
    <div className="container page">
      <div className="listing-head">
        <h1>Quero ler</h1>
        <p>{plural(wish.length, 'livro guardado', 'livros guardados')} neste navegador. {checking ? 'Conferindo preços de hoje...' : 'Preços e estoque conferidos agora.'}</p>
      </div>
      <div className="toolbar">
        <div className="row wrap">
          <button type="button" className="btn btn-primary" onClick={allToCart} disabled={busy === 'all' || total === 0}>
            {busy === 'all' ? <Spinner /> : <ShoppingBag size={16} />} Pôr os disponíveis no carrinho ({money(total)})
          </button>
        </div>
        <select className="select" value={sort} onChange={(e) => setSort(e.target.value)} aria-label="Ordenar">
          {SORTS.map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>
      <ul className="wish-list">
        {list.map((w) => {
          const drop = w.savedPrice - w.price;
          const back = !w.savedInStock && w.inStock;
          return (
            <li key={w.id} className="wish-row">
              <Link to={`/livro/${w.slug}`} className="thumb"><BookCover title={w.title} authors={w.authors} style={w.coverStyle} showText={false} /></Link>
              <div className="stack" style={{ gap: 4, minWidth: 0 }}>
                <Link to={`/livro/${w.slug}`} className="t">{w.title}</Link>
                <span className="subtle">{w.authors.join(', ')}. Guardado em {date(w.savedAt)}.</span>
                <div className="row wrap" style={{ gap: 8 }}>
                  {drop > 0.001 && <span className="badge green">Baixou {money(drop)}</span>}
                  {drop < -0.001 && <span className="badge">Subiu {money(-drop)}</span>}
                  {back && <span className="badge green">Voltou ao estoque</span>}
                  {!w.inStock && <span className="badge red">Esgotado</span>}
                </div>
              </div>
              <div className="wish-price">
                <b>{money(w.price)}</b>
                {Math.abs(drop) > 0.001 && <span className="subtle">era {money(w.savedPrice)}</span>}
              </div>
              <div className="row" style={{ gap: 6 }}>
                {w.inStock && (
                  <button type="button" className="btn btn-sm" disabled={busy === w.id} onClick={() => toCart(w, true)}>
                    {busy === w.id ? <Spinner size={13} /> : <ShoppingBag size={14} />} Mover para o carrinho
                  </button>
                )}
                <button type="button" className="btn btn-ghost btn-icon btn-sm" aria-label={`Tirar ${w.title} de Quero ler`} onClick={() => removeWish(w.id)}><Trash2 size={15} /></button>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
