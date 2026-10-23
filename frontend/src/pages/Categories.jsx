import { Link } from 'react-router';
import { useFetch, useTitle } from '../lib/hooks.js';

export default function Categories() {
  useTitle('Categorias');
  const { data } = useFetch('/api/categories');
  const catalogs = useFetch('/api/catalogs');
  return (
    <div className="container page">
      <div className="listing-head">
        <h1>Categorias e catálogos</h1>
        <p>Categorias agrupam os livros por assunto. Catálogos são seleções montadas pela equipe da livraria.</p>
      </div>
      <div className="cat-grid section">
        {(data || Array.from({ length: 8 })).map((c, i) => (c ? (
          <Link key={c.id} to={`/categorias/${c.slug}`} className="cat-tile" style={{ minHeight: 170 }}>
            <span className="swatch" style={{ background: c.accent }} />
            <div className="stack" style={{ gap: 8 }}>
              <h3>{c.name}</h3>
              <p style={{ fontSize: 14, maxWidth: '36ch' }}>{c.description}</p>
            </div>
            <span className="n">{c.bookCount} {c.bookCount === 1 ? 'livro' : 'livros'}</span>
          </Link>
        ) : <div key={i} className="skeleton" style={{ height: 170 }} />))}
      </div>
      <div className="rail-head"><div><h2>Catálogos</h2><p>Seleções que mudam ao longo do ano.</p></div></div>
      <div className="band">
        {catalogs.data?.map((c) => (
          <Link key={c.id} to={`/catalogos/${c.slug}`} className="perk" style={{ flexDirection: 'column', gap: 8 }}>
            <h3>{c.name}</h3>
            {c.highlight && <span className="subtle">{c.highlight}</span>}
            <p className="muted" style={{ fontSize: 14 }}>{c.description}</p>
            <span className="subtle">{c.bookCount} livros</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
