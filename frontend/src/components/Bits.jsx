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

export function Stars({ rating, count }) {
  if (!count) return null;
  return (
    <span className="stars" title={`${Number(rating).toFixed(1)} de 5`}>
      <Star fill="currentColor" strokeWidth={0} /> {Number(rating).toFixed(1).replace('.', ',')}
      <span>({count.toLocaleString('pt-BR')})</span>
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
      <ellipse cx="70" cy="100" rx="52" ry="6" fill="var(--bg-soft)" />
      <rect x="28" y="70" width="84" height="18" rx="4" fill="var(--text)" />
      <rect x="36" y="50" width="72" height="18" rx="4" fill="#B86B84" />
      <rect x="30" y="30" width="76" height="18" rx="4" fill="#E0B94F" transform="rotate(-6 68 39)" />
      <circle cx="96" cy="16" r="6" fill="#B86B84">
        <animate attributeName="cy" values="16;10;16" dur="2.4s" repeatCount="indefinite" />
      </circle>
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
