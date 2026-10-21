import { Check, Minus, Plus, ShoppingBag, Truck, Zap } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import BookCard from '../components/BookCard.jsx';
import BookCover from '../components/BookCover.jsx';
import { Empty, Price, Spinner, Stars } from '../components/Bits.jsx';
import { post } from '../lib/api.js';
import { useCart } from '../lib/cart.jsx';
import { FORMAT, money } from '../lib/format.js';
import { useFetch, useTitle } from '../lib/hooks.js';
import { useToast } from '../lib/toast.jsx';

export default function Book() {
  const { slug } = useParams();
  const { data: b, loading, error } = useFetch(`/api/books/${slug}`);
  const { add, setOpen } = useCart();
  const toast = useToast();
  const navigate = useNavigate();
  const [qty, setQty] = useState(1);
  const [busy, setBusy] = useState(false);
  const [added, setAdded] = useState(false);
  const [zip, setZip] = useState('');
  const [quote, setQuote] = useState(null);
  useTitle(b?.title);

  if (error) return <div className="container page"><Empty title="Livro não encontrado" action={<Link to="/livros" className="btn btn-dark">Ver catálogo</Link>} /></div>;
  if (loading || !b) {
    return (
      <div className="container page book-page">
        <div className="skeleton" style={{ aspectRatio: '2 / 3' }} />
        <div className="stack"><div className="skeleton" style={{ height: 48, width: '70%' }} /><div className="skeleton" style={{ height: 20, width: '40%' }} /><div className="skeleton" style={{ height: 180 }} /></div>
      </div>
    );
  }

  const max = Math.min(10, b.stockQuantity);
  const installments = Math.min(6, Math.floor(Number(b.price) / 30));
  async function addToCart(buyNow) {
    setBusy(true);
    try {
      await add({ ...b, authors: b.authors.map((a) => a.name) }, qty);
      setAdded(true);
      setTimeout(() => setAdded(false), 1600);
      if (buyNow) navigate('/carrinho');
      else toast(`${qty > 1 ? `${qty} unidades` : '1 unidade'} no carrinho`, { action: { to: '/carrinho', label: 'Ver' } });
    } catch (e) {
      toast(e.message, { error: true });
    } finally {
      setBusy(false);
    }
  }
  async function estimate(e) {
    e.preventDefault();
    try {
      setQuote(await post('/api/shipping/quote', { zipCode: zip, weightGrams: b.weightGrams * qty, subtotal: Number(b.price) * qty }));
    } catch (err) {
      toast(err.message, { error: true });
    }
  }

  return (
    <div className="container page">
      <div className="book-page">
        <div className="cover-stage"><BookCover title={b.title} authors={b.authors.map((a) => a.name)} style={b.coverStyle} /></div>
        <div>
          <div className="crumbs">
            <Link to="/">Início</Link><span>/</span><Link to={`/categorias/${b.category.slug}`}>{b.category.name}</Link>
          </div>
          <h1 style={{ marginTop: 14 }}>{b.title}</h1>
          {b.subtitle && <p className="muted serif" style={{ fontSize: 20, marginTop: 6 }}>{b.subtitle}</p>}
          <div className="row wrap" style={{ marginTop: 14, gap: 14 }}>
            <span style={{ fontWeight: 700 }}>{b.authors.map((a) => a.name).join(', ')}</span>
            <Stars rating={b.rating} count={b.ratingCount} />
            <span className="badge">{FORMAT[b.format]}</span>
          </div>

          <div className="buy-box rise">
            <div className="row between wrap">
              <Price price={b.price} listPrice={b.listPrice} size="lg" />
              {b.inStock ? (
                <span className={`badge ${b.stockQuantity <= 5 ? 'gold' : 'green'}`}>
                  {b.stockQuantity <= 5 ? `Restam só ${b.stockQuantity}` : 'Em estoque'}
                </span>
              ) : <span className="badge red">Esgotado</span>}
            </div>
            <span className="subtle">{installments > 1 ? `ou ${installments}x de ${money(Number(b.price) / installments)} sem juros` : 'à vista no Pix, boleto ou cartão'}</span>
            {b.inStock && (
              <div className="row wrap">
                <div className="qty" style={{ height: 52 }}>
                  <button type="button" onClick={() => setQty((q) => Math.max(1, q - 1))} aria-label="Diminuir"><Minus size={15} /></button>
                  <span>{qty}</span>
                  <button type="button" onClick={() => setQty((q) => Math.min(max, q + 1))} disabled={qty >= max} aria-label="Aumentar"><Plus size={15} /></button>
                </div>
                <button type="button" className="btn btn-primary btn-lg grow" disabled={busy} onClick={() => addToCart(false)}>
                  {busy ? <Spinner /> : added ? <Check size={18} /> : <ShoppingBag size={18} />} {added ? 'Adicionado' : 'Adicionar ao carrinho'}
                </button>
                <button type="button" className="btn btn-dark btn-lg" disabled={busy} onClick={() => addToCart(true)}><Zap size={17} /> Comprar agora</button>
              </div>
            )}
            <form className="row" onSubmit={estimate}>
              <Truck size={18} className="muted" />
              <input className="input" style={{ height: 40, maxWidth: 160 }} placeholder="CEP" inputMode="numeric" value={zip}
                onChange={(e) => setZip(e.target.value.replace(/\D/g, '').slice(0, 8).replace(/^(\d{5})(\d)/, '$1-$2'))} />
              <button type="submit" className="btn btn-sm">Calcular frete</button>
            </form>
            {quote && (
              <div className="stack fade" style={{ gap: 6 }}>
                {quote.map((o) => (
                  <div key={o.code} className="sum-row"><span>{o.name} · {o.minDays}–{o.maxDays} dias úteis</span><b>{o.free ? 'Grátis' : money(o.fee)}</b></div>
                ))}
              </div>
            )}
            <button type="button" className="btn btn-ghost btn-sm" style={{ alignSelf: 'flex-start' }} onClick={() => setOpen(true)}>Ver carrinho</button>
          </div>

          <h3 style={{ marginBottom: 10 }}>Sinopse</h3>
          <p className="synopsis">{b.synopsis}</p>
          <div className="specs" style={{ marginTop: 26 }}>
            <div className="spec"><span>Editora</span><b>{b.publisher}</b></div>
            <div className="spec"><span>Ano</span><b>{b.publishedYear}</b></div>
            <div className="spec"><span>Páginas</span><b>{b.pageCount}</b></div>
            <div className="spec"><span>Idioma</span><b>{b.language === 'pt-BR' ? 'Português' : b.language}</b></div>
            <div className="spec" style={{ gridColumn: 'span 2' }}><span>ISBN</span><b className="mono">{b.isbn}</b></div>
            <div className="spec"><span>Peso</span><b>{b.weightGrams} g</b></div>
          </div>
        </div>
      </div>
      {b.related.length > 0 && (
        <section className="section" style={{ marginTop: 70 }}>
          <div className="rail-head"><div><span className="eyebrow">Na mesma estante</span><h2 style={{ marginTop: 6 }}>Você também pode gostar</h2></div></div>
          <div className="rail">{b.related.map((r, i) => <BookCard key={r.id} book={r} index={i} />)}</div>
        </section>
      )}
    </div>
  );
}
