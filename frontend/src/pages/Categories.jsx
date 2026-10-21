import { Link } from 'react-router';
import { useFetch, useTitle } from '../lib/hooks.js';

export default function Categories() {
  useTitle('Categorias');
  const { data } = useFetch('/api/categories');
  const catalogs = useFetch('/api/catalogs');
  return (
    <div className="container page">
      <div className="listing-head">
        <span className="eyebrow">Explorar</span>
        <h1>Categorias e catálogos</h1>
        <p>Do clássico ao técnico: escolha uma estante e boa leitura.</p>
      </div>
      <div className="cat-grid section">
        {(data || Array.from({ length: 8 })).map((c, i) => (c ? (
          <Link key={c.id} to={`/categorias/${c.slug}`} className="cat-tile rise" style={{ '--i': i, minHeight: 190 }}>
            <span className="swatch" style={{ background: c.accent }} />
            <div className="stack" style={{ gap: 8 }}>
              <h3>{c.name}</h3>
              <p className="muted" style={{ fontSize: 13.5, maxWidth: '80%' }}>{c.description}</p>
            </div>
            <span className="n">{c.bookCount} {c.bookCount === 1 ? 'livro' : 'livros'}</span>
          </Link>
        ) : <div key={i} className="skeleton" style={{ height: 190, borderRadius: 20 }} />))}
      </div>
      <h2 style={{ marginBottom: 18 }}>Catálogos curados</h2>
      <div className="band">
        {catalogs.data?.map((c, i) => (
          <Link key={c.id} to={`/catalogos/${c.slug}`} className="perk rise" style={{ '--i': i, flexDirection: 'column' }}>
            <span className="eyebrow">{c.highlight}</span>
            <h3>{c.name}</h3>
            <p className="muted" style={{ fontSize: 14 }}>{c.description}</p>
            <span className="subtle">{c.bookCount} livros</span>
          </Link>
        ))}
      </div>
    </div>
  );
}
