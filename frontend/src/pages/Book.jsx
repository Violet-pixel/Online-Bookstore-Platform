import { Check, Minus, Plus, Share2, ShoppingBag, Truck } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import { SaveButton } from '../components/BookCard.jsx';
import BookCover from '../components/BookCover.jsx';
import { Empty, Price, Spinner, Stars } from '../components/Bits.jsx';
import { post } from '../lib/api.js';
import { useCart } from '../lib/cart.jsx';
import { FORMAT, money } from '../lib/format.js';
import { useFetch, useTitle } from '../lib/hooks.js';
import { useLibrary } from '../lib/library.jsx';
import { useToast } from '../lib/toast.jsx';
import { Rail } from './Home.jsx';

function readingTime(pages) {
  if (!pages) return null;
  const hours = Math.round((pages * 1.6) / 60);
  return hours <= 1 ? 'cerca de 1 hora de leitura' : `cerca de ${hours} horas de leitura`;
}

export default function Book() {
  const { slug } = useParams();
  const { data: b, loading, error } = useFetch(`/api/books/${slug}`);
  const { add } = useCart();
  const { seen, recent } = useLibrary();
  const toast = useToast();
  const navigate = useNavigate();
  const [qty, setQty] = useState(1);
  const [busy, setBusy] = useState(false);
  const [added, setAdded] = useState(false);
  const [zip, setZip] = useState('');
  const [quote, setQuote] = useState(null);
  useTitle(b?.title);

  useEffect(() => {
    if (b) seen(b);
    setQty(1);
    setQuote(null);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [b?.id]);

  if (error) return <div className="container page"><Empty title="Livro não encontrado" action={<Link to="/livros" className="btn btn-dark">Ver catálogo</Link>}>O endereço pode ter mudado ou o livro saiu de catálogo.</Empty></div>;
  if (loading || !b) {
    return (
      <div className="container page book-page">
        <div className="skeleton" style={{ aspectRatio: '2 / 3' }} />
        <div className="stack"><div className="skeleton" style={{ height: 48, width: '70%' }} /><div className="skeleton" style={{ height: 20, width: '40%' }} /><div className="skeleton" style={{ height: 180 }} /></div>
      </div>
    );
  }

  const authors = b.authors.map((a) => a.name);
  const max = Math.min(10, b.stockQuantity);
  const installments = Math.min(6, Math.floor(Number(b.price) / 30));
  const others = recent.filter((r) => r.id !== b.id);

  async function addToCart(buyNow) {
    setBusy(true);
    try {
      await add({ ...b, authors }, qty);
      setAdded(true);
      setTimeout(() => setAdded(false), 1600);
      if (buyNow) navigate('/carrinho');
      else toast(`${qty > 1 ? `${qty} exemplares` : '1 exemplar'} no carrinho`, { action: { to: '/carrinho', label: 'Ver' } });
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
  async function share() {
    const url = window.location.href;
    try {
      if (navigator.share) await navigator.share({ title: b.title, text: `${b.title}, de ${authors.join(', ')}`, url });
      else {
        await navigator.clipboard.writeText(url);
        toast('Link copiado');
      }
    } catch { /* compartilhamento cancelado */ }
  }

  return (
    <div className="container page">
      <div className="crumbs">
        <Link to="/">Início</Link><span aria-hidden="true">/</span><Link to={`/categorias/${b.category.slug}`}>{b.category.name}</Link>
      </div>
      <div className="book-page">
        <div className="cover-stage">
          <BookCover title={b.title} authors={authors} style={b.coverStyle} />
        </div>
        <div className="book-main">
          <h1>{b.title}</h1>
          {b.subtitle && <p className="book-sub">{b.subtitle}</p>}
          <p className="byline">
            {b.authors.map((a, i) => (
              <span key={a.slug}>{i > 0 && ', '}<Link to={`/autor/${a.slug}?nome=${encodeURIComponent(a.name)}`}>{a.name}</Link></span>
            ))}
          </p>
          <div className="row wrap book-facts">
            <Stars rating={b.rating} count={b.ratingCount} />
            <span>{FORMAT[b.format]}</span>
            {b.pageCount && <span>{b.pageCount} páginas, {readingTime(b.pageCount)}</span>}
          </div>

          <div className="buy-box">
            <div className="row between wrap" style={{ alignItems: 'flex-end' }}>
              <div className="stack" style={{ gap: 4 }}>
                <Price price={b.price} listPrice={b.listPrice} size="lg" />
                <span className="subtle">{installments > 1 ? `ou ${installments}x de ${money(Number(b.price) / installments)} sem juros no cartão` : 'à vista no Pix, boleto ou cartão'}</span>
              </div>
              {b.inStock ? (
                <span className={`stock ${b.stockQuantity <= 5 ? 'low' : ''}`}>
                  {b.stockQuantity <= 5 ? `Restam ${b.stockQuantity} exemplares` : 'Em estoque, sai em até 1 dia útil'}
                </span>
              ) : <span className="stock out">Esgotado. Guarde em Quero ler para saber quando voltar.</span>}
            </div>
            <div className="row wrap buy-actions">
              {b.inStock && (
                <>
                  <div className="qty" aria-label="Quantidade">
                    <button type="button" onClick={() => setQty((q) => Math.max(1, q - 1))} aria-label="Diminuir"><Minus size={15} /></button>
                    <span aria-live="polite">{qty}</span>
                    <button type="button" onClick={() => setQty((q) => Math.min(max, q + 1))} disabled={qty >= max} aria-label="Aumentar"><Plus size={15} /></button>
                  </div>
                  <button type="button" className="btn btn-primary btn-lg grow" disabled={busy} onClick={() => addToCart(false)}>
                    {busy ? <Spinner /> : added ? <Check size={18} /> : <ShoppingBag size={18} />} {added ? 'No carrinho' : 'Adicionar ao carrinho'}
                  </button>
                  <button type="button" className="btn btn-lg" disabled={busy} onClick={() => addToCart(true)}>Comprar agora</button>
                </>
              )}
              <SaveButton book={b} className="big" />
              <button type="button" className="btn btn-icon btn-lg btn-ghost" onClick={share} aria-label="Compartilhar"><Share2 size={17} /></button>
            </div>
            <form className="ship-calc" onSubmit={estimate}>
              <Truck size={17} aria-hidden="true" />
              <label htmlFor="cep-livro">Frete e prazo</label>
              <input id="cep-livro" className="input mono" placeholder="00000-000" inputMode="numeric" value={zip}
                onChange={(e) => setZip(e.target.value.replace(/\D/g, '').slice(0, 8).replace(/^(\d{5})(\d)/, '$1-$2'))} />
              <button type="submit" className="btn btn-sm" disabled={zip.length < 9}>Calcular</button>
            </form>
            {quote && (
              <ul className="quote fade">
                {quote.map((o) => (
                  <li key={o.code}><span>{o.name}, {o.minDays} a {o.maxDays} dias úteis</span><b>{o.free ? 'Grátis' : money(o.fee)}</b></li>
                ))}
              </ul>
            )}
          </div>

          <section className="book-section">
            <h2>Sinopse</h2>
            <p className="synopsis">{b.synopsis}</p>
          </section>

          {b.authors.some((a) => a.bio) && (
            <section className="book-section">
              <h2>Sobre {authors.length > 1 ? 'os autores' : 'quem escreveu'}</h2>
              {b.authors.filter((a) => a.bio).map((a) => <p key={a.slug} className="synopsis"><b>{a.name}.</b> {a.bio}</p>)}
            </section>
          )}

          <section className="book-section">
            <h2>Ficha catalográfica</h2>
            <div className="ficha">
              <p>{authors[0]?.split(' ').slice(-1)[0]}, {authors[0]?.split(' ').slice(0, -1).join(' ')}</p>
              <p className="indent">{b.title}{b.subtitle ? `: ${b.subtitle.toLowerCase()}` : ''} / {authors.join(', ')}. {b.publisher}, {b.publishedYear}.</p>
              <p className="indent">{b.pageCount ? `${b.pageCount} p.` : ''} {FORMAT[b.format].toLowerCase()}. {b.weightGrams} g.</p>
              <p className="indent">ISBN {b.isbn}</p>
              <p className="indent">1. {b.category.name}. I. Título.</p>
              <p className="lang">Idioma: {b.language === 'pt-BR' ? 'português' : b.language}</p>
            </div>
          </section>
        </div>
      </div>
      {b.related.length > 0 && <Rail title="Da mesma estante" lead={`Outros livros de ${b.category.name}.`} to={`/categorias/${b.category.slug}`} books={b.related} />}
      {others.length > 0 && <Rail title="Vistos por último" books={others} />}
    </div>
  );
}
