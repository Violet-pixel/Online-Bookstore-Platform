import { ArrowRight, Check, MapPin, Plus, Tag, Truck } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router';
import AddressForm from '../components/AddressForm.jsx';
import BookCover from '../components/BookCover.jsx';
import { Empty, Spinner } from '../components/Bits.jsx';
import PaymentModal from '../components/PaymentModal.jsx';
import { get, post } from '../lib/api.js';
import { useCart } from '../lib/cart.jsx';
import { money, plural } from '../lib/format.js';
import { useTitle } from '../lib/hooks.js';
import { useToast } from '../lib/toast.jsx';

function Steps({ at }) {
  const items = ['Endereço', 'Envio', 'Pagamento'];
  return (
    <div className="checkout-steps">
      {items.map((s, i) => (
        <span key={s} className="row" style={{ gap: 8 }}>
          {i > 0 && <span className="bar" />}
          <span className={`s ${i === at ? 'on' : i < at ? 'done' : ''}`}><span className="n">{i < at ? <Check size={14} /> : i + 1}</span>{s}</span>
        </span>
      ))}
    </div>
  );
}

export default function Checkout() {
  useTitle('Fechar pedido');
  const { cart, reload } = useCart();
  const toast = useToast();
  const navigate = useNavigate();
  const [addresses, setAddresses] = useState(null);
  const [addressId, setAddressId] = useState(null);
  const [adding, setAdding] = useState(false);
  const [coupon, setCoupon] = useState(() => sessionStorage.getItem('prelo:cupom') || '');
  const [appliedCoupon, setAppliedCoupon] = useState(coupon);
  const [quote, setQuote] = useState(null);
  const [method, setMethod] = useState(null);
  const [busy, setBusy] = useState(false);
  const [order, setOrder] = useState(null);

  useEffect(() => {
    get('/api/account/addresses').then((list) => {
      setAddresses(list);
      const def = list.find((a) => a.isDefault) || list[0];
      if (def) setAddressId(def.id);
      else setAdding(true);
    });
  }, []);

  useEffect(() => {
    if (!addressId) return;
    post('/api/checkout/quote', { addressId, couponCode: appliedCoupon || null }).then((q) => {
      setQuote(q);
      setMethod((m) => (q.shippingOptions.some((o) => o.code === m) ? m : q.shippingOptions[0]?.code));
    }).catch((e) => toast(e.message, { error: true }));
  }, [addressId, appliedCoupon, cart.itemCount, toast]);

  async function saveAddress(data) {
    const a = await post('/api/account/addresses', data);
    setAddresses((list) => [...(list || []).map((x) => (a.isDefault ? { ...x, isDefault: false } : x)), a]);
    setAddressId(a.id);
    setAdding(false);
  }

  async function place() {
    setBusy(true);
    try {
      const o = await post('/api/orders', { addressId, shippingMethod: method, couponCode: quote?.couponValid ? quote.couponCode : null });
      try { sessionStorage.removeItem('prelo:cupom'); } catch { /* ok */ }
      setOrder(o);
      reload();
    } catch (e) {
      toast(e.message, { error: true });
    } finally {
      setBusy(false);
    }
  }

  if (!order && cart.itemCount === 0) {
    return <div className="container page"><Empty title="Nada para fechar ainda" action={<Link to="/livros" className="btn btn-dark">Explorar livros</Link>}>Adicione livros ao carrinho para continuar.</Empty></div>;
  }

  const option = quote?.shippingOptions.find((o) => o.code === method);
  const total = quote ? Number(quote.subtotal) - Number(quote.discount) + Number(option?.fee || 0) : Number(cart.subtotal);
  const step = !addressId ? 0 : !method ? 1 : 2;

  return (
    <div className="container page">
      <div className="listing-head" style={{ paddingBottom: 0 }}><h1>Fechar pedido</h1></div>
      <Steps at={order ? 3 : step} />
      <div className="two-col" style={{ paddingTop: 0 }}>
        <div className="stack" style={{ gap: 18 }}>
          <section className="card">
            <div className="panel-title"><h3 className="row" style={{ gap: 10 }}><MapPin size={18} /> Endereço de entrega</h3>
              {!adding && <button type="button" className="btn btn-sm" onClick={() => setAdding(true)}><Plus size={14} /> Novo</button>}</div>
            {addresses === null ? <div className="skeleton" style={{ height: 90 }} /> : (
              <div className="choices">
                {addresses.map((a) => (
                  <label key={a.id} className={`choice ${addressId === a.id ? 'on' : ''}`}>
                    <input type="radio" name="addr" className="sr-only" checked={addressId === a.id} onChange={() => setAddressId(a.id)} />
                    <span className="radio" />
                    <div className="stack" style={{ gap: 2 }}>
                      <b>{a.label} {a.isDefault && <span className="badge" style={{ marginLeft: 6 }}>principal</span>}</b>
                      <span className="muted" style={{ fontSize: 14 }}>{a.street}, {a.streetNumber}{a.complement ? ` · ${a.complement}` : ''} · {a.district}</span>
                      <span className="subtle">{a.city}/{a.state} · CEP {a.zipCode} · {a.recipient}</span>
                    </div>
                  </label>
                ))}
                {adding && <div className="card flat" style={{ background: 'var(--surface-2)' }}><AddressForm onSubmit={saveAddress} onCancel={addresses.length ? () => setAdding(false) : undefined} submitLabel="Usar este endereço" /></div>}
              </div>
            )}
          </section>

          <section className="card">
            <div className="panel-title"><h3 className="row" style={{ gap: 10 }}><Truck size={18} /> Forma de envio</h3></div>
            {!quote ? <div className="skeleton" style={{ height: 120 }} /> : (
              <div className="choices">
                {quote.shippingOptions.map((o) => (
                  <label key={o.code} className={`choice ${method === o.code ? 'on' : ''}`}>
                    <input type="radio" name="ship" className="sr-only" checked={method === o.code} onChange={() => setMethod(o.code)} />
                    <span className="radio" />
                    <div className="grow stack" style={{ gap: 2 }}>
                      <b>{o.name}</b>
                      <span className="muted" style={{ fontSize: 14 }}>{o.description} · {o.carrier}</span>
                      <span className="subtle">{o.minDays} a {o.maxDays} dias úteis após o pagamento</span>
                    </div>
                    <b style={{ color: o.free ? 'var(--success)' : undefined }}>{o.free ? 'Grátis' : money(o.fee)}</b>
                  </label>
                ))}
              </div>
            )}
          </section>
        </div>

        <aside className="aside">
          <div className="card stack" style={{ gap: 14 }}>
            <h3>Seu pedido</h3>
            <div className="stack" style={{ gap: 10, maxHeight: 250, overflow: 'auto' }}>
              {cart.items.map((l) => (
                <div key={l.book.id} className="row" style={{ gap: 12 }}>
                  <div style={{ width: 38, flex: 'none' }}><BookCover title={l.book.title} style={l.book.coverStyle} showText={false} /></div>
                  <div className="grow" style={{ fontSize: 13.5 }}><b>{l.book.title}</b><div className="subtle">{l.quantity}x {money(l.unitPrice)}</div></div>
                  <b style={{ fontSize: 14 }}>{money(l.lineTotal)}</b>
                </div>
              ))}
            </div>
            <form className="row" onSubmit={(e) => { e.preventDefault(); setAppliedCoupon(coupon.trim()); }}>
              <div className="grow" style={{ position: 'relative' }}>
                <Tag size={15} style={{ position: 'absolute', left: 12, top: 15, color: 'var(--text-3)' }} />
                <input className="input" style={{ paddingLeft: 34 }} placeholder="Cupom" value={coupon} onChange={(e) => setCoupon(e.target.value.toUpperCase())} />
              </div>
              <button type="submit" className="btn">Aplicar</button>
            </form>
            {quote?.couponCode && (
              <span className={`badge ${quote.couponValid ? 'green' : 'red'} fade`} style={{ alignSelf: 'flex-start' }}>
                {quote.couponValid ? <Check size={13} /> : null} {quote.couponMessage}
              </span>
            )}
            <hr className="sep" style={{ margin: 0 }} />
            <div className="sum-row"><span>Subtotal · {plural(quote?.itemCount ?? cart.itemCount, 'item', 'itens')}</span><span>{money(quote?.subtotal ?? cart.subtotal)}</span></div>
            {Number(quote?.discount) > 0 && <div className="sum-row" style={{ color: 'var(--success)' }}><span>Desconto</span><span>-{money(quote.discount)}</span></div>}
            <div className="sum-row"><span>Frete</span><span>{option ? (option.free ? 'Grátis' : money(option.fee)) : '—'}</span></div>
            <div className="sum-row total"><span>Total</span><span>{money(total)}</span></div>
            <button type="button" className="btn btn-primary btn-lg btn-block" disabled={!addressId || !method || busy} onClick={place}>
              {busy ? <Spinner /> : null} Ir para o pagamento <ArrowRight size={18} />
            </button>
            <p className="subtle" style={{ textAlign: 'center' }}>O estoque fica reservado por 60 minutos enquanto você paga.</p>
          </div>
        </aside>
      </div>
      <PaymentModal order={order} open={!!order} onClose={() => navigate(`/compras/${order.code}`)} />
    </div>
  );
}
