import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';

// "Quero ler" e "Vistos por último": ficam neste navegador, sem precisar de conta.
// Cada livro salvo guarda o preço do dia, para avisar quando ele baixa ou volta ao estoque.
const WISH = 'prelo:quero-ler';
const RECENT = 'prelo:vistos';
const Ctx = createContext(null);

function read(key) {
  try {
    return JSON.parse(localStorage.getItem(key)) || [];
  } catch {
    return [];
  }
}
function write(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    /* navegação privada: segue só em memória */
  }
}

/** Só o que a vitrine precisa para desenhar o card de volta. */
export function snapshot(b) {
  return {
    id: b.id, slug: b.slug, title: b.title, subtitle: b.subtitle || null,
    authors: (b.authors || []).map((a) => (typeof a === 'string' ? a : a.name)),
    price: Number(b.price), listPrice: Number(b.listPrice), coverStyle: b.coverStyle, format: b.format,
    inStock: !!b.inStock, rating: b.rating, ratingCount: b.ratingCount, category: b.category, pageCount: b.pageCount,
  };
}

export function LibraryProvider({ children }) {
  const [wish, setWish] = useState(() => read(WISH));
  const [recent, setRecent] = useState(() => read(RECENT));

  useEffect(() => {
    const sync = (e) => {
      if (e.key === WISH) setWish(read(WISH));
      if (e.key === RECENT) setRecent(read(RECENT));
    };
    window.addEventListener('storage', sync);
    return () => window.removeEventListener('storage', sync);
  }, []);

  const saved = useCallback((id) => wish.some((w) => w.id === id), [wish]);

  const toggle = useCallback((book) => {
    const has = wish.some((w) => w.id === book.id);
    const next = has ? wish.filter((w) => w.id !== book.id)
      : [{ ...snapshot(book), savedPrice: Number(book.price), savedInStock: !!book.inStock, savedAt: new Date().toISOString() }, ...wish].slice(0, 200);
    write(WISH, next);
    setWish(next);
    return !has;
  }, [wish]);

  const removeWish = useCallback((id) => setWish((list) => {
    const next = list.filter((w) => w.id !== id);
    write(WISH, next);
    return next;
  }), []);

  /** Atualiza preço e estoque do que está salvo sem perder o preço do dia em que foi salvo. */
  const refreshWish = useCallback((fresh) => setWish((list) => {
    const next = list.map((w) => {
      const f = fresh.find((x) => x.id === w.id);
      return f ? { ...w, ...snapshot(f), savedPrice: w.savedPrice, savedInStock: w.savedInStock, savedAt: w.savedAt } : w;
    });
    write(WISH, next);
    return next;
  }), []);

  const seen = useCallback((book) => setRecent((list) => {
    const next = [snapshot(book), ...list.filter((r) => r.id !== book.id)].slice(0, 12);
    write(RECENT, next);
    return next;
  }), []);

  const clearRecent = useCallback(() => {
    write(RECENT, []);
    setRecent([]);
  }, []);

  const news = useMemo(() => wish.filter((w) => w.price < w.savedPrice - 0.001 || (!w.savedInStock && w.inStock)).length, [wish]);

  const value = useMemo(() => ({ wish, saved, toggle, removeWish, refreshWish, recent, seen, clearRecent, news }),
    [wish, saved, toggle, removeWish, refreshWish, recent, seen, clearRecent, news]);
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export const useLibrary = () => useContext(Ctx);
