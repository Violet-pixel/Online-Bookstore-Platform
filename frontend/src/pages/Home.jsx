import { ChevronLeft, ChevronRight } from 'lucide-react';
import { useRef } from 'react';
import { Link } from 'react-router';
import { GridSkeleton } from '../components/Bits.jsx';
import BookCard from '../components/BookCard.jsx';
import Shelf from '../components/Shelf.jsx';
import { useAuth } from '../lib/auth.jsx';
import { greeting } from '../lib/format.js';
import { useFetch, useTitle } from '../lib/hooks.js';
import { useLibrary } from '../lib/library.jsx';

export function Rail({ title, lead, to, books, action }) {
  const ref = useRef(null);
  const scroll = (dir) => ref.current?.scrollBy({ left: dir * ref.current.clientWidth * 0.8, behavior: 'smooth' });
  return (
    <section className="section">
      <div className="rail-head">
        <div>
          <h2>{title}</h2>
          {lead && <p>{lead}</p>}
        </div>
        <div className="row">
          {action}
          {to && <Link to={to} className="text-link">Ver tudo</Link>}
          <div className="rail-nav desktop-only">
            <button type="button" className="btn btn-icon btn-sm" onClick={() => scroll(-1)} aria-label="Voltar"><ChevronLeft size={16} /></button>
            <button type="button" className="btn btn-icon btn-sm" onClick={() => scroll(1)} aria-label="Avançar"><ChevronRight size={16} /></button>
          </div>
        </div>
      </div>
      <div className="rail" ref={ref}>
        {books.map((b) => <BookCard key={b.id} book={b} />)}
      </div>
    </section>
  );
}

export default function Home() {
  useTitle(null);
  const { user } = useAuth();
  const { recent, clearRecent } = useLibrary();
  const { data, loading } = useFetch('/api/home');
  const shelf = useFetch('/api/books?sort=popular&size=34');
  const first = user?.fullName.split(' ')[0];

  return (
    <div className="page home">
      <section className="hero container">
        <div className="hero-copy">
          <h1>{first ? `${greeting()}, ${first}. Escolha pela lombada.` : 'Escolha pela lombada.'}</h1>
          <p>
            Clássicos, lançamentos e livros técnicos. Passe o mouse ou toque numa lombada para ver o livro.
            Frete grátis acima de R$ 199 e Pix aprovado na hora.
          </p>
        </div>
        <Shelf books={shelf.data?.items || []} />
      </section>

      <div className="container">
        <ul className="facts">
          <li><b>Frete grátis acima de R$ 199</b> na entrega econômica, para todo o Brasil.</li>
          <li><b>Pix, boleto ou 6x sem juros</b> no cartão, na janela de pagamento.</li>
          <li><b>Cancelamento antes do envio</b> com estorno automático.</li>
        </ul>

        {recent.length > 1 && (
          <Rail title="Vistos por último" books={recent} action={<button type="button" className="text-link" onClick={clearRecent}>Limpar</button>} />
        )}

        {loading && <GridSkeleton n={6} />}
        {data?.catalogs.slice(0, 2).map((c) => <Rail key={c.id} title={c.name} lead={c.highlight} to={`/catalogos/${c.slug}`} books={c.books} />)}

        {data && (
          <section className="section">
            <div className="rail-head">
              <div><h2>Estantes</h2><p>Cada categoria é uma estante da livraria.</p></div>
              <Link to="/categorias" className="text-link">Categorias e catálogos</Link>
            </div>
            <div className="cat-grid">
              {data.categories.map((c) => (
                <Link key={c.id} to={`/categorias/${c.slug}`} className="cat-tile" style={{ '--accent-cat': c.accent }}>
                  <span className="mini-spines" aria-hidden="true"><i /><i /><i /><i /></span>
                  <h3>{c.name}</h3>
                  <span className="n">{c.bookCount} {c.bookCount === 1 ? 'livro' : 'livros'}</span>
                </Link>
              ))}
            </div>
          </section>
        )}

        {data && (
          <section className="section coupon-strip">
            <div>
              <h3>Primeira compra? Use o cupom e leve 10% de desconto.</h3>
              <p>Vale para qualquer livro, inclusive lançamentos. Digite no carrinho.</p>
            </div>
            <span className="coupon-code">BEMVINDO10</span>
          </section>
        )}

        {data?.catalogs.slice(2).map((c) => <Rail key={c.id} title={c.name} lead={c.highlight} to={`/catalogos/${c.slug}`} books={c.books} />)}
      </div>
    </div>
  );
}
