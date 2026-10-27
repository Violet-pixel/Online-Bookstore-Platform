import { Truck } from 'lucide-react';
import { useNavigate } from 'react-router';
import { useCart } from '../lib/cart.jsx';
import { money } from '../lib/format.js';
import { Empty } from './Bits.jsx';
import CartLines from './CartLines.jsx';
import { Drawer } from './Overlay.jsx';

const FREE = 199;

export default function CartDrawer() {
  const { cart, open, setOpen } = useCart();
  const navigate = useNavigate();
  const missing = Math.max(0, FREE - Number(cart.subtotal));
  const pct = Math.min(100, (Number(cart.subtotal) / FREE) * 100);
  return (
    <Drawer open={open} onClose={() => setOpen(false)} title={`Carrinho${cart.itemCount ? ` (${cart.itemCount})` : ''}`}
      footer={cart.itemCount ? (close) => (
        <>
          <div className="stack" style={{ gap: 8 }}>
            <div className="row" style={{ gap: 8, fontSize: 13 }}>
              <Truck size={16} />
              {missing > 0 ? <span>Faltam <b>{money(missing)}</b> para o frete grátis na entrega econômica</span> : <b>Você ganhou frete grátis na entrega econômica</b>}
            </div>
            <div className="free-bar" role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={Math.round(pct)} aria-label="Progresso até o frete grátis"><i className={pct >= 100 ? 'full' : ''} style={{ width: `${pct}%` }} /></div>
          </div>
          <div className="sum-row total"><span>Subtotal</span><span>{money(cart.subtotal)}</span></div>
          <button type="button" className="btn btn-primary btn-lg btn-block" onClick={() => { close(); navigate('/carrinho'); }}>
            Ver carrinho e fechar pedido
          </button>
        </>
      ) : null}>
      {(close) => (cart.itemCount ? <CartLines compact onNavigate={close} /> : (
        <Empty title="Seu carrinho está vazio" action={<button type="button" className="btn btn-dark" onClick={() => { close(); navigate('/livros'); }}>Explorar livros</button>}>
          Os livros que você adicionar aparecem aqui.
        </Empty>
      ))}
    </Drawer>
  );
}
