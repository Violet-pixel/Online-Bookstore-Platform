import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { del, get, patch, post } from './api.js';
import { useAuth } from './auth.jsx';

// Sem login, o carrinho fica no navegador; ao entrar, ele é somado ao da conta.
const KEY = 'prelo:carrinho';
const Ctx = createContext(null);

function readLocal() {
  try {
    return JSON.parse(localStorage.getItem(KEY)) || [];
  } catch {
    return [];
  }
}
function writeLocal(items) {
  try {
    localStorage.setItem(KEY, JSON.stringify(items));
  } catch {
    /* navegação privada: segue só em memória */
  }
}
function localView(items) {
  const lines = items.map((i) => ({
    book: i.book, quantity: i.quantity, available: i.book.stockQuantity ?? 10,
    unitPrice: i.book.price, lineTotal: Number(i.book.price) * i.quantity,
  }));
  return {
    items: lines,
    itemCount: lines.reduce((s, l) => s + l.quantity, 0),
    subtotal: lines.reduce((s, l) => s + l.lineTotal, 0),
    savings: lines.reduce((s, l) => s + Math.max(0, (Number(l.book.listPrice) - Number(l.book.price)) * l.quantity), 0),
    weightGrams: items.reduce((s, i) => s + (i.book.weightGrams || 400) * i.quantity, 0),
  };
}

export function CartProvider({ children }) {
  const { user, ready } = useAuth();
  const [cart, setCart] = useState(() => localView(readLocal()));
  const [open, setOpen] = useState(false);
  const [bump, setBump] = useState(0);
  const merged = useRef(null);

  useEffect(() => {
    if (!ready) return;
    if (!user) {
      merged.current = null;
      setCart(localView(readLocal()));
      return;
    }
    if (merged.current === user.id) return;
    merged.current = user.id;
    const local = readLocal();
    const req = local.length
      ? post('/api/cart/merge', { items: local.map((i) => ({ bookId: i.book.id, quantity: i.quantity })) })
      : get('/api/cart');
    req.then((c) => {
      writeLocal([]);
      setCart(c);
    }).catch(() => {});
  }, [user, ready]);

  const add = useCallback(async (book, quantity = 1) => {
    if (user) {
      setCart(await post('/api/cart/items', { bookId: book.id, quantity }));
    } else {
      const items = readLocal();
      const found = items.find((i) => i.book.id === book.id);
      if (found) found.quantity = Math.min(10, found.quantity + quantity);
      else items.push({ book, quantity });
      writeLocal(items);
      setCart(localView(items));
    }
    setBump((b) => b + 1);
  }, [user]);

  const change = useCallback(async (bookId, quantity) => {
    if (user) {
      setCart(await patch(`/api/cart/items/${bookId}`, { quantity }));
    } else {
      const items = readLocal().map((i) => (i.book.id === bookId ? { ...i, quantity } : i)).filter((i) => i.quantity > 0);
      writeLocal(items);
      setCart(localView(items));
    }
  }, [user]);

  const remove = useCallback(async (bookId) => {
    if (user) setCart(await del(`/api/cart/items/${bookId}`));
    else {
      const items = readLocal().filter((i) => i.book.id !== bookId);
      writeLocal(items);
      setCart(localView(items));
    }
  }, [user]);

  const reload = useCallback(async () => {
    if (user) setCart(await get('/api/cart'));
  }, [user]);

  const value = useMemo(() => ({ cart, add, change, remove, reload, open, setOpen, bump }), [cart, add, change, remove, reload, open, bump]);
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export const useCart = () => useContext(Ctx);
