import { createContext, useCallback, useContext, useRef, useState } from 'react';
import { Check, X } from 'lucide-react';
import { Link } from 'react-router';

const Ctx = createContext(() => {});

export function ToastProvider({ children }) {
  const [items, setItems] = useState([]);
  const seq = useRef(0);
  const push = useCallback((message, opts = {}) => {
    const id = ++seq.current;
    setItems((list) => [...list.slice(-2), { id, message, ...opts }]);
    setTimeout(() => setItems((list) => list.map((t) => (t.id === id ? { ...t, out: true } : t))), opts.duration || 3200);
    setTimeout(() => setItems((list) => list.filter((t) => t.id !== id)), (opts.duration || 3200) + 300);
  }, []);
  return (
    <Ctx.Provider value={push}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        {items.map((t) => (
          <div key={t.id} className={`toast ${t.error ? 'error' : ''} ${t.out ? 'out' : ''}`}>
            <span className="ic">{t.error ? <X size={15} /> : <Check size={15} />}</span>
            <span>{t.message}</span>
            {t.action && <Link to={t.action.to}>{t.action.label}</Link>}
          </div>
        ))}
      </div>
    </Ctx.Provider>
  );
}

export const useToast = () => useContext(Ctx);
