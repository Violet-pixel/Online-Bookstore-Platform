import { Link, useParams, useSearchParams } from 'react-router';
import BookCard from '../components/BookCard.jsx';
import { Empty, GridSkeleton } from '../components/Bits.jsx';
import { qs } from '../lib/api.js';
import { plural } from '../lib/format.js';
import { useFetch, useTitle } from '../lib/hooks.js';
import { slugify } from '../lib/slug.js';

/** Todos os livros de uma pessoa autora, a partir da busca do catálogo (que já procura por autor, sem acentos). */
export default function Author() {
  const { slug } = useParams();
  const [params] = useSearchParams();
  const name = params.get('nome') || slug.replace(/-/g, ' ');
  const { data, loading } = useFetch(`/api/books${qs({ q: name, size: 48, sort: 'recent' })}`);
  const books = (data?.items || []).filter((b) => b.authors.some((a) => slugify(a) === slug));
  const shown = books[0]?.authors.find((a) => slugify(a) === slug) || name;
  useTitle(shown);

  return (
    <div className="container page">
      <div className="listing-head">
        <div className="crumbs"><Link to="/">Início</Link><span aria-hidden="true">/</span><Link to="/livros">Livros</Link><span aria-hidden="true">/</span><span>Autoria</span></div>
        <h1>{shown}</h1>
        {data && <p>{books.length ? `${plural(books.length, 'livro', 'livros')} na livraria.` : 'Nenhum livro desta pessoa à venda agora.'}</p>}
      </div>
      {loading && !data ? <GridSkeleton n={5} /> : books.length ? (
        <div className="book-grid">{books.map((b) => <BookCard key={b.id} book={b} />)}</div>
      ) : (
        <Empty title="Nada por aqui" action={<Link to="/livros" className="btn btn-dark">Ver todos os livros</Link>}>
          Os livros podem ter saído de catálogo. Tente a busca pelo título.
        </Empty>
      )}
    </div>
  );
}
