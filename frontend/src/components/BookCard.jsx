import { Plus } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router';
import { useCart } from '../lib/cart.jsx';
import { useToast } from '../lib/toast.jsx';
import BookCover from './BookCover.jsx';
import { Price, Spinner, Stars } from './Bits.jsx';

export default function BookCard({ book, index = 0 }) {
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
    <Link to={`/livro/${book.slug}`} className="book-card rise" style={{ '--i': index % 12 }}>
      <div className="cover-wrap" style={{ position: 'relative' }}>
        <BookCover title={book.title} authors={book.authors} style={book.coverStyle} />
        {book.inStock ? (
          <button type="button" className="btn btn-primary btn-icon btn-sm quick" onClick={quickAdd} aria-label={`Adicionar ${book.title} ao carrinho`}>
            {busy ? <Spinner size={14} /> : <Plus size={16} />}
          </button>
        ) : (
          <span className="badge dark flag">Esgotado</span>
        )}
      </div>
      <div className="meta">
        <span className="title">{book.title}</span>
        <span className="author">{book.authors?.join(', ')}</span>
        <Stars rating={book.rating} count={book.ratingCount} />
      </div>
      <Price price={book.price} listPrice={book.listPrice} />
    </Link>
  );
}
