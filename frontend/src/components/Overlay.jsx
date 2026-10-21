import { X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';

function useClosing(open, onClose) {
  const [closing, setClosing] = useState(false);
  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => e.key === 'Escape' && close();
    document.addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = prev;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);
  function close() {
    setClosing(true);
    setTimeout(() => {
      setClosing(false);
      onClose();
    }, 220);
  }
  return [closing, close];
}

export function Modal({ open, onClose, title, children, wide, dismissable = true }) {
  const [closing, close] = useClosing(open, onClose);
  if (!open) return null;
  return createPortal(
    <>
      <div className={`overlay ${closing ? 'closing' : ''}`} onClick={dismissable ? close : undefined} />
      <div className={`modal ${wide ? 'wide' : ''}`} role="dialog" aria-modal="true" aria-label={title}
        style={closing ? { opacity: 0, transition: 'opacity .2s' } : undefined}>
        <div className="modal-head">
          <h2 style={{ fontSize: 24 }}>{title}</h2>
          {dismissable && <button type="button" className="btn btn-ghost btn-icon" onClick={close} aria-label="Fechar"><X size={18} /></button>}
        </div>
        <div className="modal-body">{typeof children === 'function' ? children(close) : children}</div>
      </div>
    </>,
    document.body,
  );
}

export function Drawer({ open, onClose, title, children, footer }) {
  const [closing, close] = useClosing(open, onClose);
  if (!open) return null;
  return createPortal(
    <>
      <div className={`overlay ${closing ? 'closing' : ''}`} onClick={close} />
      <aside className={`drawer ${closing ? 'closing' : ''}`} role="dialog" aria-modal="true" aria-label={title}>
        <div className="drawer-head">
          <h2 style={{ fontSize: 22 }}>{title}</h2>
          <button type="button" className="btn btn-ghost btn-icon" onClick={close} aria-label="Fechar"><X size={18} /></button>
        </div>
        <div className="drawer-body">{typeof children === 'function' ? children(close) : children}</div>
        {footer && <div className="drawer-foot">{typeof footer === 'function' ? footer(close) : footer}</div>}
      </aside>
    </>,
    document.body,
  );
}
