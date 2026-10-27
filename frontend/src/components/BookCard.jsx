import { Bookmark, Plus } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router';
import { useCart } from '../lib/cart.jsx';
import { useLibrary } from '../lib/library.jsx';
import { useToast } from '../lib/toast.jsx';
import BookCover from './BookCover.jsx';
import { Price, Spinner, Stars } from './Bits.jsx';

export function SaveButton({ book, className = '' }) {
  const { saved, toggle } = useLibrary();
  const toast = useToast();
  const on = saved(book.id);
  return (
    <button type="button" className={`save-btn ${on ? 'on' : ''} ${className}`} aria-pressed={on}
      aria-label={on ? `Tirar ${book.title} de Quero ler` : `Guardar ${book.title} em Quero ler`}
      onClick={(e) => {
        e.preventDefault();
        e.stopPropagation();
        const added = toggle(book);
        toast(added ? `“${book.title}” guardado em Quero ler` : 'Tirado de Quero ler', added ? { action: { to: '/quero-ler', label: 'Abrir' } } : {});
      }}>
      <Bookmark size={16} fill={on ? 'currentColor' : 'none'} />
    </button>
  );
}

export default function BookCard({ book }) {
  const { add } = useCart();
  const toast = useToast();
  const [busy, setBusy] = useState(false);

  async function quickAdd(e) {
    e.preventDefault();
    setBusy(true);
    try {
      await add(book, 1);
      toast(`“${book.title}” foi para o carrinho`, { action: { to: '/carrinho', label: 'Ver' } });
    } catch (err) {
      toast(err.message, { error: true });
    } finally {
      setBusy(false);
    }
  }

  return (
    <article className="book-card">
      <Link to={`/livro/${book.slug}`} className="cover-wrap" aria-label={book.title}>
        <BookCover title={book.title} authors={book.authors} style={book.coverStyle} />
        {!book.inStock && <span className="flag">Esgotado</span>}
      </Link>
      <div className="card-actions">
        <SaveButton book={book} />
        {book.inStock && (
          <button type="button" className="add-btn" onClick={quickAdd} aria-label={`Adicionar ${book.title} ao carrinho`}>
            {busy ? <Spinner size={14} /> : <Plus size={16} />}
          </button>
        )}
      </div>
      <Link to={`/livro/${book.slug}`} className="meta">
        <span className="title">{book.title}</span>
        <span className="author">{book.authors?.join(', ')}</span>
      </Link>
      <div className="foot">
        <Price price={book.price} listPrice={book.listPrice} />
        <Stars rating={book.rating} count={book.ratingCount} compact />
      </div>
    </article>
  );
}
