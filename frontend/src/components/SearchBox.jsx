import { Clock, Search, X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router';
import { get, qs } from '../lib/api.js';
import { money } from '../lib/format.js';
import { useDebounced } from '../lib/hooks.js';
import BookCover from './BookCover.jsx';

const KEY = 'prelo:buscas';
const readRecent = () => {
  try { return JSON.parse(localStorage.getItem(KEY)) || []; } catch { return []; }
};
const saveRecent = (q) => {
  try { localStorage.setItem(KEY, JSON.stringify([q, ...readRecent().filter((x) => x !== q)].slice(0, 5))); } catch { /* ok */ }
};

/** Busca do cabeçalho: sugere livros enquanto você digita e lembra as últimas buscas. Atalho: tecla /. */
export default function SearchBox() {
  const navigate = useNavigate();
  const location = useLocation();
  const [q, setQ] = useState('');
  const [open, setOpen] = useState(false);
  const [results, setResults] = useState(null);
  const [cursor, setCursor] = useState(-1);
  const [recent, setRecent] = useState(readRecent);
  const term = useDebounced(q.trim(), 180);
  const input = useRef(null);
  const box = useRef(null);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    setQ(location.pathname === '/livros' ? params.get('q') || '' : '');
    setOpen(false);
  }, [location.pathname, location.search]);

  useEffect(() => {
    if (term.length < 2) { setResults(null); return undefined; }
    let alive = true;
    get(`/api/books${qs({ q: term, size: 6 })}`).then((r) => alive && setResults(r)).catch(() => alive && setResults(null));
    return () => { alive = false; };
  }, [term]);

  useEffect(() => {
    const onKey = (e) => {
      if (e.key === '/' && !['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName)) {
        e.preventDefault();
        input.current?.focus();
      }
    };
    const onClick = (e) => box.current && !box.current.contains(e.target) && setOpen(false);
    document.addEventListener('keydown', onKey);
    document.addEventListener('mousedown', onClick);
    return () => {
      document.removeEventListener('keydown', onKey);
      document.removeEventListener('mousedown', onClick);
    };
  }, []);

  const items = term.length >= 2 ? (results?.items || []) : [];
  const showRecent = open && term.length < 2 && recent.length > 0;

  function go(text) {
    const t = text.trim();
    if (t) { saveRecent(t); setRecent(readRecent()); }
    navigate(`/livros${t ? `?q=${encodeURIComponent(t)}` : ''}`);
    setOpen(false);
    input.current?.blur();
  }
  function pick(b) {
    saveRecent(q.trim());
    setRecent(readRecent());
    navigate(`/livro/${b.slug}`);
    setOpen(false);
    input.current?.blur();
  }
  function onKeyDown(e) {
    const list = showRecent ? recent : items;
    if (e.key === 'ArrowDown') { e.preventDefault(); setOpen(true); setCursor((c) => Math.min(list.length - 1, c + 1)); }
    else if (e.key === 'ArrowUp') { e.preventDefault(); setCursor((c) => Math.max(-1, c - 1)); }
    else if (e.key === 'Escape') { setOpen(false); input.current?.blur(); }
    else if (e.key === 'Enter' && cursor >= 0 && list[cursor]) {
      e.preventDefault();
      if (showRecent) go(list[cursor]); else pick(list[cursor]);
    }
  }

  return (
    <div className="search-box" ref={box}>
      <form className="search" role="search" onSubmit={(e) => { e.preventDefault(); go(q); }}>
        <Search className="icon" aria-hidden="true" />
        <input ref={input} value={q} placeholder="Buscar por título, autor ou ISBN" aria-label="Buscar livros"
          role="combobox" aria-expanded={open && (items.length > 0 || showRecent)} aria-controls="search-pop" aria-autocomplete="list"
          onFocus={() => setOpen(true)} onKeyDown={onKeyDown}
          onChange={(e) => { setQ(e.target.value); setOpen(true); setCursor(-1); }} />
        {q ? <button type="button" className="clear" aria-label="Limpar busca" onClick={() => { setQ(''); input.current?.focus(); }}><X size={15} /></button>
          : <kbd aria-hidden="true">/</kbd>}
      </form>
      {open && (items.length > 0 || showRecent || (term.length >= 2 && results)) && (
        <div className="search-pop" id="search-pop" role="listbox">
          {showRecent && (
            <>
              <div className="pop-label">Buscas recentes</div>
              {recent.map((r, i) => (
                <button key={r} type="button" role="option" aria-selected={cursor === i} className={`pop-row ${cursor === i ? 'on' : ''}`} onClick={() => go(r)}>
                  <Clock size={15} className="muted" /> {r}
                </button>
              ))}
            </>
          )}
          {term.length >= 2 && items.map((b, i) => (
            <button key={b.id} type="button" role="option" aria-selected={cursor === i} className={`pop-row book ${cursor === i ? 'on' : ''}`} onClick={() => pick(b)}>
              <span className="thumb"><BookCover title={b.title} style={b.coverStyle} showText={false} /></span>
              <span className="grow">
                <span className="t">{b.title}</span>
                <span className="subtle">{b.authors.join(', ')}</span>
              </span>
              <span className="p">{money(b.price)}</span>
            </button>
          ))}
          {term.length >= 2 && results && (
            <button type="button" className="pop-all" onClick={() => go(q)}>
              {results.total ? `Ver os ${results.total} resultados para “${term}”` : `Nada encontrado para “${term}”. Buscar no catálogo todo`}
            </button>
          )}
        </div>
      )}
    </div>
  );
}
