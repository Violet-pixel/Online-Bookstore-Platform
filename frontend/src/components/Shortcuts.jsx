import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { useCart } from '../lib/cart.jsx';
import { Modal } from './Overlay.jsx';

const LIST = [
  ['/', 'Buscar livros'],
  ['g depois i', 'Ir para a vitrine'],
  ['g depois l', 'Ir para Quero ler'],
  ['g depois c', 'Ir para Minhas compras'],
  ['c', 'Abrir o carrinho'],
  ['?', 'Mostrar estes atalhos'],
];

/** Atalhos de teclado da loja. A tecla ? mostra a lista. */
export default function Shortcuts() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const { setOpen: openCart } = useCart();

  useEffect(() => {
    let g = 0;
    const onKey = (e) => {
      if (e.ctrlKey || e.metaKey || e.altKey) return;
      if (['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName) || document.activeElement?.isContentEditable) return;
      if (e.key === '?') { e.preventDefault(); setOpen(true); return; }
      if (e.key === 'g') { g = Date.now(); return; }
      if (Date.now() - g < 900) {
        const to = { i: '/', l: '/quero-ler', c: '/compras' }[e.key];
        g = 0;
        if (to) { e.preventDefault(); navigate(to); }
        return;
      }
      if (e.key === 'c') openCart(true);
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [navigate, openCart]);

  return (
    <Modal open={open} onClose={() => setOpen(false)} title="Atalhos de teclado">
      <dl className="shortcuts">
        {LIST.map(([k, v]) => (
          <div key={k}><dt>{k.split(' depois ').map((x, i) => <span key={x}>{i > 0 && <em> depois </em>}<kbd>{x}</kbd></span>)}</dt><dd>{v}</dd></div>
        ))}
      </dl>
    </Modal>
  );
}
