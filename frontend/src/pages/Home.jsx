import { ArrowRight, ChevronLeft, ChevronRight, CreditCard, RotateCcw, Truck } from 'lucide-react';
import { useRef } from 'react';
import { Link } from 'react-router';
import { GridSkeleton } from '../components/Bits.jsx';
import BookCard from '../components/BookCard.jsx';
import BookCover from '../components/BookCover.jsx';
import { useAuth } from '../lib/auth.jsx';
import { greeting } from '../lib/format.js';
import { useFetch, useTitle } from '../lib/hooks.js';

function Rail({ catalog, index }) {
  const ref = useRef(null);
  const scroll = (dir) => ref.current?.scrollBy({ left: dir * ref.current.clientWidth * 0.8, behavior: 'smooth' });
  return (
    <section className="section rise" style={{ '--i': index }}>
      <div className="rail-head">
        <div>
          <span className="eyebrow">{catalog.highlight}</span>
          <h2 style={{ marginTop: 6 }}>{catalog.name}</h2>
        </div>
        <div className="row">
          <Link to={`/catalogos/${catalog.slug}`} className="btn btn-ghost btn-sm">Ver tudo <ArrowRight size={15} /></Link>
          <div className="rail-nav desktop-only">
            <button type="button" className="btn btn-icon btn-sm" onClick={() => scroll(-1)} aria-label="Voltar"><ChevronLeft size={16} /></button>
            <button type="button" className="btn btn-icon btn-sm" onClick={() => scroll(1)} aria-label="Avançar"><ChevronRight size={16} /></button>
          </div>
        </div>
      </div>
      <div className="rail" ref={ref}>
        {catalog.books.map((b, i) => <BookCard key={b.id} book={b} index={i} />)}
      </div>
    </section>
  );
}

export default function Home() {
  useTitle(null);
  const { user } = useAuth();
  const { data, loading } = useFetch('/api/home');
  const spotlight = data?.spotlight || [];
  const books = data?.catalogs?.reduce((n, c) => n + c.bookCount, 0);

  return (
    <div className="container page">
      <section className="hero">
        <span className="blob b1" /><span className="blob b2" />
        <div className="hero-inner">
          <div>
            <span className="eyebrow" style={{ color: '#9E5570' }}>{user ? `${greeting()}, ${user.fullName.split(' ')[0]}` : 'Livraria online'}</span>
            <h1 style={{ marginTop: 12 }}>Histórias que acabaram de <em>sair do prelo</em>.</h1>
            <p>Clássicos, lançamentos e livros técnicos escolhidos a dedo, com frete grátis acima de R$ 199 e Pix aprovado na hora.</p>
            <div className="row wrap">
              <Link to="/catalogos/lancamentos" className="btn btn-dark btn-lg">Ver lançamentos <ArrowRight size={18} /></Link>
              <Link to="/categorias" className="btn btn-ghost btn-lg">Explorar categorias</Link>
            </div>
            <div className="stats">
              <div><b>{data ? data.categories.length : '—'}</b>categorias</div>
              <div><b>{data ? data.catalogs.length : '—'}</b>catálogos</div>
              <div><b>6x</b>sem juros</div>
            </div>
          </div>
          <div className="hero-books" aria-hidden="true">
            {(spotlight.length ? spotlight : [{}, {}, {}]).map((b, i) => (
              <Link key={b.id || i} to={b.slug ? `/livro/${b.slug}` : '#'} className="hb" tabIndex={-1}>
                {b.title ? <BookCover title={b.title} authors={b.authors} style={b.coverStyle} /> : <div className="skeleton" style={{ aspectRatio: '2 / 3' }} />}
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="section band">
        <div className="perk rise" style={{ '--i': 0 }}><span className="ic"><Truck size={20} /></span><div><h4>Frete grátis acima de R$ 199</h4><p>Na entrega econômica, para todo o Brasil.</p></div></div>
        <div className="perk rise" style={{ '--i': 1 }}><span className="ic"><CreditCard size={20} /></span><div><h4>Pix, boleto ou 6x sem juros</h4><p>Escolha na janela de pagamento.</p></div></div>
        <div className="perk rise" style={{ '--i': 2 }}><span className="ic"><RotateCcw size={20} /></span><div><h4>Cancelamento simples</h4><p>Antes do envio, com estorno automático.</p></div></div>
      </section>

      {loading && <GridSkeleton n={6} />}
      {data?.catalogs.slice(0, 2).map((c, i) => <Rail key={c.id} catalog={c} index={i} />)}

      {data && (
        <section className="section">
          <div className="rail-head"><div><span className="eyebrow">Por onde começar</span><h2 style={{ marginTop: 6 }}>Categorias</h2></div>
            <Link to="/categorias" className="btn btn-ghost btn-sm">Todas <ArrowRight size={15} /></Link></div>
          <div className="cat-grid">
            {data.categories.map((c, i) => (
              <Link key={c.id} to={`/categorias/${c.slug}`} className="cat-tile rise" style={{ '--i': i }}>
                <span className="swatch" style={{ background: c.accent }} />
                <h3>{c.name}</h3>
                <span className="n">{c.bookCount} {c.bookCount === 1 ? 'livro' : 'livros'}</span>
              </Link>
            ))}
          </div>
        </section>
      )}

      {data && (
        <section className="section promo rise">
          <span className="ring" />
          <div className="stack" style={{ gap: 8, position: 'relative' }}>
            <span className="eyebrow" style={{ color: 'var(--butter)' }}>Primeira compra</span>
            <h3>10% de desconto em qualquer livro</h3>
            <p style={{ opacity: .8 }}>Use o cupom no carrinho. Vale também para os lançamentos.</p>
          </div>
          <span className="code">BEMVINDO10</span>
        </section>
      )}

      {data?.catalogs.slice(2).map((c, i) => <Rail key={c.id} catalog={c} index={i} />)}
      {books === 0 && <p className="muted">Nenhum livro cadastrado ainda.</p>}
    </div>
  );
}
