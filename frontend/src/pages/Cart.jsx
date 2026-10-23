import { ShoppingBag, Tag } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import CartLines from '../components/CartLines.jsx';
import { Empty } from '../components/Bits.jsx';
import { post } from '../lib/api.js';
import { useAuth } from '../lib/auth.jsx';
import { useCart } from '../lib/cart.jsx';
import { money, plural } from '../lib/format.js';
import { useTitle } from '../lib/hooks.js';

export default function Cart() {
  useTitle('Carrinho');
  const { cart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [zip, setZip] = useState('');
  const [options, setOptions] = useState(null);
  const [coupon, setCoupon] = useState(() => sessionStorage.getItem('prelo:cupom') || '');

  async function quote(e) {
    e.preventDefault();
    try {
      setOptions(await post('/api/shipping/quote', { zipCode: zip, weightGrams: cart.weightGrams || 400, subtotal: Number(cart.subtotal) }));
    } catch (err) {
      setOptions({ error: err.message });
    }
  }
  function goCheckout() {
    try { sessionStorage.setItem('prelo:cupom', coupon.trim()); } catch { /* sem armazenamento */ }
    navigate(user ? '/checkout' : '/entrar?voltar=/checkout');
  }

  if (!cart.itemCount) {
    return <div className="container page"><Empty title="Seu carrinho está vazio" action={<Link to="/livros" className="btn btn-dark"><ShoppingBag size={16} /> Explorar livros</Link>}>Os livros que você adicionar aparecem aqui.</Empty></div>;
  }
  return (
    <div className="container page">
      <div className="listing-head"><h1>Carrinho</h1><p>{plural(cart.itemCount, 'livro', 'livros')} no carrinho.</p></div>
      <div className="two-col" style={{ paddingTop: 0 }}>
        <div className="card"><CartLines /></div>
        <aside className="aside stack" style={{ gap: 14 }}>
          <div className="card stack" style={{ gap: 14 }}>
            <h3>Resumo</h3>
            <div className="sum-row"><span>Subtotal</span><span>{money(cart.subtotal)}</span></div>
            {Number(cart.savings) > 0 && <div className="sum-row" style={{ color: 'var(--success)' }}><span>Você economiza</span><span>{money(cart.savings)}</span></div>}
            <form className="row" onSubmit={quote}>
              <input className="input" placeholder="CEP para o frete" inputMode="numeric" value={zip}
                onChange={(e) => setZip(e.target.value.replace(/\D/g, '').slice(0, 8).replace(/^(\d{5})(\d)/, '$1-$2'))} />
              <button type="submit" className="btn">OK</button>
            </form>
            {options?.error && <span className="err" style={{ color: 'var(--danger)', fontSize: 13 }}>{options.error}</span>}
            {Array.isArray(options) && options.map((o) => (
              <div key={o.code} className="sum-row fade"><span>{o.name}, {o.minDays} a {o.maxDays} dias</span><b>{o.free ? 'Grátis' : money(o.fee)}</b></div>
            ))}
            <div className="field">
              <label htmlFor="cupom"><Tag size={13} style={{ display: 'inline', marginRight: 6 }} />Cupom</label>
              <input id="cupom" className="input" placeholder="Ex.: BEMVINDO10" value={coupon} onChange={(e) => setCoupon(e.target.value.toUpperCase())} />
              <span className="hint">O desconto é aplicado no fechamento.</span>
            </div>
            <hr className="sep" style={{ margin: '4px 0' }} />
            <div className="sum-row total"><span>Total parcial</span><span>{money(cart.subtotal)}</span></div>
            <button type="button" className="btn btn-primary btn-lg btn-block" onClick={goCheckout}>
              {user ? 'Fechar pedido' : 'Entrar e fechar pedido'}
            </button>
          </div>
          <Link to="/livros" className="btn btn-ghost">Continuar comprando</Link>
        </aside>
      </div>
    </div>
  );
}
