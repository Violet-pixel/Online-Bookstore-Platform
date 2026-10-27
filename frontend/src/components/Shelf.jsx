import { useState } from 'react';
import { Link } from 'react-router';
import { money } from '../lib/format.js';
import { coverOf } from './BookCover.jsx';

// Estante com as lombadas dos livros: a largura vem do número de páginas e a altura, do formato.
const HEIGHT = { POCKET: 74, PAPERBACK: 86, HARDCOVER: 96 };

function width(b) {
  if (!b.pageCount) return 30 + ((b.id * 37) % 24);
  return Math.round(Math.min(64, Math.max(30, 22 + b.pageCount / 14)));
}

export default function Shelf({ books }) {
  const [active, setActive] = useState(null);
  const shown = active ?? books[0];
  if (!books.length) return <div className="shelf skeleton" style={{ height: 300 }} />;
  return (
    <div className="shelf-wrap">
      <div className="shelf" role="list" onMouseLeave={() => setActive(null)}>
        {books.map((b, i) => {
          const s = coverOf(b.coverStyle);
          return (
            <Link key={b.id} to={`/livro/${b.slug}`} role="listitem" className={`spine ${shown?.id === b.id ? 'on' : ''}`}
              style={{ '--c1': s.c1, '--c2': s.c2, '--fg': s.fg, width: width(b), height: `${HEIGHT[b.format] || 86}%`, '--tilt': i % 9 === 4 ? '-4deg' : '0deg' }}
              onMouseEnter={() => setActive(b)} onFocus={() => setActive(b)}
              aria-label={`${b.title}, de ${b.authors.join(', ')}, ${money(b.price)}`}>
              <span className="spine-title">{b.title}</span>
              <span className="spine-mark" />
            </Link>
          );
        })}
      </div>
      <div className="plank" />
      {shown && (
        <p className="shelf-caption" aria-live="polite">
          <Link to={`/livro/${shown.slug}`}><b className="serif">{shown.title}</b></Link>
          <span>{shown.authors.join(', ')}</span>
          <span className="tag-price">{money(shown.price)}</span>
        </p>
      )}
    </div>
  );
}
