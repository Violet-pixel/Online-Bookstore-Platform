import { ChevronLeft, ChevronRight, Loader2, Star } from 'lucide-react';
import { discountPct, money, ORDER_STATUS, PAYMENT_STATUS } from '../lib/format.js';

export function Price({ price, listPrice, size }) {
  const off = discountPct(price, listPrice);
  return (
    <div className={`price ${size || ''}`}>
      <span className="now">{money(price)}</span>
      {off > 0 && <span className="was">{money(listPrice)}</span>}
      {off > 0 && <span className="off">-{off}%</span>}
    </div>
  );
}

export function Stars({ rating, count, compact }) {
  if (!count) return null;
  const r = Number(rating).toFixed(1).replace('.', ',');
  return (
    <span className="stars" title={`Nota ${r} de 5, ${count.toLocaleString('pt-BR')} avaliações`}>
      <Star fill="currentColor" strokeWidth={0} aria-hidden="true" /> {r}
      {!compact && <span>({count.toLocaleString('pt-BR')})</span>}
    </span>
  );
}

export function Spinner({ size = 16 }) {
  return <Loader2 size={size} className="spin" />;
}

export function OrderBadge({ status }) {
  const s = ORDER_STATUS[status] || { label: status };
  return (
    <span className={`badge ${s.tone || ''}`}>
      {s.live && <span className="pulse" />} {s.label}
    </span>
  );
}

export function PaymentBadge({ status }) {
  const s = PAYMENT_STATUS[status] || { label: status };
  return <span className={`badge ${s.tone || ''}`}>{s.label}</span>;
}

export function Pager({ page, totalPages, onPage }) {
  if (!totalPages || totalPages < 2) return null;
  const pages = [];
  for (let i = Math.max(0, page - 2); i <= Math.min(totalPages - 1, page + 2); i++) pages.push(i);
  return (
    <nav className="pager" aria-label="Paginação">
      <button type="button" disabled={page === 0} onClick={() => onPage(page - 1)} aria-label="Anterior"><ChevronLeft size={16} /></button>
      {pages.map((p) => (
        <button type="button" key={p} className={p === page ? 'on' : ''} onClick={() => onPage(p)}>{p + 1}</button>
      ))}
      <button type="button" disabled={page >= totalPages - 1} onClick={() => onPage(page + 1)} aria-label="Próxima"><ChevronRight size={16} /></button>
    </nav>
  );
}

export function Field({ label, error, hint, children }) {
  return (
    <div className="field">
      {label && <label>{label}</label>}
      {children}
      {error ? <span className="err">{error}</span> : hint ? <span className="hint">{hint}</span> : null}
    </div>
  );
}

export function EmptyArt() {
  return (
    <svg className="art" viewBox="0 0 140 110" aria-hidden="true">
      <rect x="14" y="96" width="112" height="4" rx="1" fill="var(--line-strong)" />
      <rect x="30" y="44" width="16" height="52" rx="1.5" fill="var(--text)" />
      <rect x="48" y="36" width="12" height="60" rx="1.5" fill="#B86B84" />
      <rect x="62" y="50" width="18" height="46" rx="1.5" fill="#E0B94F" />
      <rect x="86" y="40" width="14" height="56" rx="1.5" fill="#4E6178" transform="rotate(14 93 96)" />
    </svg>
  );
}

export function Empty({ title, children, action }) {
  return (
    <div className="empty fade">
      <EmptyArt />
      <h3>{title}</h3>
      {children && <p className="muted">{children}</p>}
      {action}
    </div>
  );
}

export function GridSkeleton({ n = 10 }) {
  return (
    <div className="book-grid">
      {Array.from({ length: n }, (_, i) => (
        <div key={i} className="stack" style={{ gap: 10 }}>
          <div className="skeleton" style={{ aspectRatio: '2 / 3' }} />
          <div className="skeleton" style={{ height: 14, width: '80%' }} />
          <div className="skeleton" style={{ height: 12, width: '50%' }} />
        </div>
      ))}
    </div>
  );
}
