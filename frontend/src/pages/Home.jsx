import { ChevronLeft, ChevronRight, CreditCard, RotateCcw, Truck } from 'lucide-react';
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
    <section className="section">
      <div className="rail-head">
        <div>
          <h2>{catalog.name}</h2>
          {catalog.highlight && <p>{catalog.highlight}</p>}
        </div>
        <div className="row">
          <Link to={`/catalogos/${catalog.slug}`} className="btn btn-ghost btn-sm">Ver tudo</Link>
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
        <span className="crop tl" /><span className="crop tr" /><span className="crop bl" /><span className="crop br" />
        <span className="reg top" /><span className="reg side" />
        <div className="hero-inner">
          <div>
            {user && <p className="hello">{greeting()}, {user.fullName.split(' ')[0]}.</p>}
            <h1 className="misreg settle">Histórias que acabaram de sair do prelo.</h1>
            <p>Clássicos, lançamentos e livros técnicos escolhidos a dedo, com frete grátis acima de R$ 199 e Pix aprovado na hora.</p>
            <div className="row wrap">
              <Link to="/catalogos/lancamentos" className="btn btn-dark btn-lg">Ver lançamentos</Link>
              <Link to="/categorias" className="btn btn-ghost btn-lg">Explorar categorias</Link>
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
        <div className="hero-foot">
          <span className="colorbar" aria-hidden="true"><i /><i /><i /><i /><i /><i /><i /></span>
          <span className="subtle">{data ? `${data.categories.length} categorias e ${data.catalogs.length} catálogos` : ' '}</span>
        </div>
      </section>

      <section className="section band">
        <div className="perk"><Truck className="ic" size={20} /><div><h4>Frete grátis acima de R$ 199</h4><p>Na entrega econômica, para todo o Brasil.</p></div></div>
        <div className="perk"><CreditCard className="ic" size={20} /><div><h4>Pix, boleto ou 6x sem juros</h4><p>Você escolhe na janela de pagamento.</p></div></div>
        <div className="perk"><RotateCcw className="ic" size={20} /><div><h4>Cancelamento antes do envio</h4><p>O estorno sai sozinho, na mesma forma de pagamento.</p></div></div>
      </section>

      {loading && <GridSkeleton n={6} />}
      {data?.catalogs.slice(0, 2).map((c, i) => <Rail key={c.id} catalog={c} index={i} />)}

      {data && (
        <section className="section">
          <div className="rail-head"><div><h2>Categorias</h2><p>Escolha uma estante para começar.</p></div>
            <Link to="/categorias" className="btn btn-ghost btn-sm">Todas as categorias</Link></div>
          <div className="cat-grid">
            {data.categories.map((c) => (
              <Link key={c.id} to={`/categorias/${c.slug}`} className="cat-tile">
                <span className="swatch" style={{ background: c.accent }} />
                <h3>{c.name}</h3>
                <span className="n">{c.bookCount} {c.bookCount === 1 ? 'livro' : 'livros'}</span>
              </Link>
            ))}
          </div>
        </section>
      )}

      {data && (
        <section className="section promo">
          <span className="ring" />
          <div className="stack" style={{ gap: 8, position: 'relative' }}>
            <h3>10% de desconto na primeira compra</h3>
            <p>Digite o cupom no carrinho. Vale para qualquer livro, inclusive lançamentos.</p>
          </div>
          <span className="code">BEMVINDO10</span>
        </section>
      )}

      {data?.catalogs.slice(2).map((c, i) => <Rail key={c.id} catalog={c} index={i} />)}
      {books === 0 && <p className="muted">Nenhum livro cadastrado ainda.</p>}
    </div>
  );
}
