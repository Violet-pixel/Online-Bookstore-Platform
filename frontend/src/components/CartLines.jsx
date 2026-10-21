import { Minus, Plus, Trash2 } from 'lucide-react';
import { Link } from 'react-router';
import { useCart } from '../lib/cart.jsx';
import { money } from '../lib/format.js';
import { useToast } from '../lib/toast.jsx';
import BookCover from './BookCover.jsx';

export default function CartLines({ compact, onNavigate }) {
  const { cart, change, remove } = useCart();
  const toast = useToast();
  const run = (fn) => fn().catch((e) => toast(e.message, { error: true }));
  return (
    <div>
      {cart.items.map((l, i) => (
        <div key={l.book.id} className="line" style={{ animationDelay: `${i * 40}ms` }}>
          <Link to={`/livro/${l.book.slug}`} onClick={onNavigate}>
            <BookCover title={l.book.title} authors={l.book.authors} style={l.book.coverStyle} showText={false} />
          </Link>
          <div className="stack" style={{ gap: 6, minWidth: 0 }}>
            <Link to={`/livro/${l.book.slug}`} className="t" onClick={onNavigate}>{l.book.title}</Link>
            <span className="subtle">{l.book.authors?.join(', ')}</span>
            <div className="row" style={{ gap: 8 }}>
              <div className="qty">
                <button type="button" aria-label="Diminuir" onClick={() => run(() => change(l.book.id, l.quantity - 1))}><Minus size={14} /></button>
                <span>{l.quantity}</span>
                <button type="button" aria-label="Aumentar" disabled={l.quantity >= Math.min(10, l.available)}
                  onClick={() => run(() => change(l.book.id, l.quantity + 1))}><Plus size={14} /></button>
              </div>
              {!compact && <span className="subtle">{money(l.unitPrice)} cada</span>}
            </div>
          </div>
          <div className="stack" style={{ alignItems: 'flex-end', gap: 8 }}>
            <b>{money(l.lineTotal)}</b>
            <button type="button" className="btn btn-ghost btn-icon btn-sm" aria-label="Remover" onClick={() => run(() => remove(l.book.id))}>
              <Trash2 size={15} />
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
