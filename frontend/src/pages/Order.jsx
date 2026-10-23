import { ArrowLeft, Clock, Copy, CreditCard, MapPin, Truck, XCircle } from 'lucide-react';
import { useCallback, useState } from 'react';
import { Link, useParams } from 'react-router';
import BookCover from '../components/BookCover.jsx';
import { Empty, OrderBadge, PaymentBadge, Spinner } from '../components/Bits.jsx';
import { Modal } from '../components/Overlay.jsx';
import PaymentModal from '../components/PaymentModal.jsx';
import { ShipmentSteps, Timeline } from '../components/Shipping.jsx';
import { post } from '../lib/api.js';
import { dateTime, longDay, money, PAYMENT_METHOD, plural } from '../lib/format.js';
import { useCountdown, useFetch, useTitle } from '../lib/hooks.js';
import { useToast } from '../lib/toast.jsx';

function DueIn({ until }) {
  const left = useCountdown(until);
  const h = Math.floor(left / 3600000);
  const m = Math.floor((left % 3600000) / 60000);
  return <span>{left <= 0 ? 'prazo encerrado' : h >= 24 ? `vence ${dateTime(until)}` : `${h ? `${h}h ` : ''}${m}min para pagar`}</span>;
}

export function OrderView({ order, admin, actions }) {
  const s = order.shipment;
  const [copied, setCopied] = useState(false);
  return (
    <div className="order-grid">
      <div className="stack" style={{ gap: 18 }}>
        {s && (
          <section className="card">
            <div className="panel-title">
              <h3 className="row" style={{ gap: 10 }}><Truck size={18} /> Detalhes do envio</h3>
              <span className="subtle">{s.serviceName}, por {s.carrier}</span>
            </div>
            {s.status !== 'RETURNED' && <ShipmentSteps status={s.status} />}
            <div className="row wrap" style={{ margin: '18px 0', gap: 10 }}>
              {s.trackingCode && (
                <span className="tracking">
                  <span className="subtle">Rastreio</span><b className="mono">{s.trackingCode}</b>
                  <button type="button" className="btn btn-ghost btn-icon btn-sm" aria-label="Copiar código"
                    onClick={() => navigator.clipboard?.writeText(s.trackingCode).then(() => { setCopied(true); setTimeout(() => setCopied(false), 1500); })}>
                    <Copy size={14} />
                  </button>
                  {copied && <span className="subtle fade">copiado</span>}
                </span>
              )}
              {s.estimatedDelivery && s.status !== 'DELIVERED' && s.status !== 'RETURNED' && (
                <span className="tracking"><Clock size={16} /> Previsão: <b>{longDay(s.estimatedDelivery)}</b></span>
              )}
              {s.deliveredAt && <span className="tracking"><b>Entregue em {dateTime(s.deliveredAt)}</b></span>}
            </div>
            <Timeline events={s.events} />
          </section>
        )}
        <section className="card">
          <div className="panel-title"><h3>Itens</h3><span className="subtle">{plural(order.itemCount, 'livro', 'livros')}</span></div>
          {order.items.map((it) => (
            <div key={it.bookId} className="line">
              <Link to={`/livro/${it.slug}`}><BookCover title={it.title} authors={it.authors} style={it.coverStyle} showText={false} /></Link>
              <div className="stack" style={{ gap: 4 }}>
                <Link to={`/livro/${it.slug}`} className="t">{it.title}</Link>
                <span className="subtle">{it.authors}</span>
                <span className="subtle">{it.quantity}x {money(it.unitPrice)}</span>
              </div>
              <b>{money(it.lineTotal)}</b>
            </div>
          ))}
        </section>
      </div>

      <div className="stack" style={{ gap: 18 }}>
        {actions}
        <section className="card">
          <h3 style={{ marginBottom: 14 }}>Resumo</h3>
          <div className="stack" style={{ gap: 8 }}>
            <div className="sum-row"><span>Subtotal</span><span>{money(order.subtotal)}</span></div>
            {Number(order.discount) > 0 && <div className="sum-row" style={{ color: 'var(--success)' }}><span>Desconto {order.couponCode ? `(${order.couponCode})` : ''}</span><span>-{money(order.discount)}</span></div>}
            <div className="sum-row"><span>Frete ({order.shipping.name})</span><span>{Number(order.shippingFee) === 0 ? 'Grátis' : money(order.shippingFee)}</span></div>
            <hr className="sep" style={{ margin: '6px 0' }} />
            <div className="sum-row total"><span>Total</span><span>{money(order.total)}</span></div>
          </div>
        </section>
        <section className="card">
          <h3 className="row" style={{ gap: 10, marginBottom: 12 }}><CreditCard size={18} /> Pagamento</h3>
          {order.payments.length === 0 ? <p className="muted">Nenhuma tentativa de pagamento ainda.</p> : (
            <div className="stack" style={{ gap: 10 }}>
              {[...order.payments].reverse().map((p) => (
                <div key={p.reference} className="row between" style={{ fontSize: 14 }}>
                  <div className="stack" style={{ gap: 2 }}>
                    <b>{PAYMENT_METHOD[p.method]}{p.cardLast4 ? `, ${p.cardBrand} final ${p.cardLast4}` : ''}{p.installments > 1 ? ` em ${p.installments}x` : ''}</b>
                    <span className="subtle">{dateTime(p.createdAt)}{p.failureReason ? `. ${p.failureReason}` : ''}</span>
                  </div>
                  <PaymentBadge status={p.status} />
                </div>
              ))}
            </div>
          )}
        </section>
        <section className="card">
          <h3 className="row" style={{ gap: 10, marginBottom: 12 }}><MapPin size={18} /> Entrega</h3>
          <p style={{ fontWeight: 700 }}>{order.shipTo.recipient}</p>
          <p className="muted" style={{ fontSize: 14 }}>
            {order.shipTo.street}, {order.shipTo.number}{order.shipTo.complement ? `, ${order.shipTo.complement}` : ''}<br />
            {order.shipTo.district}, {order.shipTo.city}/{order.shipTo.state}<br />CEP {order.shipTo.zipCode}
          </p>
          <p className="subtle" style={{ marginTop: 8 }}>{order.shipping.name}, {order.shipping.minDays} a {order.shipping.maxDays} dias úteis</p>
          {admin && order.customer && <p className="subtle" style={{ marginTop: 8 }}>Cliente: {order.customer.fullName} ({order.customer.email})</p>}
        </section>
      </div>
    </div>
  );
}

export default function Order() {
  const { code } = useParams();
  const toast = useToast();
  const { data: order, loading, error, reload, setData } = useFetch(`/api/orders/${code}`);
  const [paying, setPaying] = useState(false);
  const [canceling, setCanceling] = useState(false);
  const [busy, setBusy] = useState(false);
  useTitle(order ? `Pedido ${order.code}` : 'Pedido');
  const refresh = useCallback(() => reload(), [reload]);

  if (error) return <div className="container page"><Empty title="Pedido não encontrado" action={<Link to="/compras" className="btn btn-dark">Minhas compras</Link>} /></div>;
  if (loading && !order) return <div className="container page"><div className="skeleton" style={{ height: 80, margin: '26px 0' }} /><div className="skeleton" style={{ height: 380 }} /></div>;

  async function cancel(close) {
    setBusy(true);
    try {
      setData(await post(`/api/orders/${order.code}/cancel`, { reason: 'Cancelado por você.' }));
      toast('Pedido cancelado. Os valores pagos são estornados.');
      close();
    } catch (e) {
      toast(e.message, { error: true });
    } finally {
      setBusy(false);
    }
  }

  const actions = (order.canPay || order.canCancel) ? (
    <section className="card" style={{ borderColor: order.canPay ? 'var(--yellow)' : undefined, borderTopWidth: order.canPay ? 4 : undefined }}>
      {order.canPay && (
        <div className="stack" style={{ gap: 12 }}>
          <b style={{ fontSize: 16 }}>Falta só o pagamento</b>
          <span className="muted row" style={{ gap: 6, fontSize: 14 }}><Clock size={15} /> <DueIn until={order.paymentDueAt} /></span>
          <button type="button" className="btn btn-primary btn-lg btn-block" onClick={() => setPaying(true)}>Pagar {money(order.total)}</button>
        </div>
      )}
      {order.canCancel && (
        <button type="button" className="btn btn-ghost btn-block" style={{ marginTop: order.canPay ? 8 : 0 }} onClick={() => setCanceling(true)}>
          <XCircle size={16} /> Cancelar pedido
        </button>
      )}
    </section>
  ) : null;

  return (
    <div className="container page">
      <div className="order-head">
        <div className="stack" style={{ gap: 10 }}>
          <Link to="/compras" className="subtle row" style={{ gap: 6 }}><ArrowLeft size={14} /> Minhas compras</Link>
          <h1 className="num" style={{ fontSize: 'clamp(30px, 3.8vw, 44px)' }}>Pedido {order.code}</h1>
          <span className="muted">Feito em {dateTime(order.placedAt)}{order.paidAt ? `, pago em ${dateTime(order.paidAt)}` : ''}</span>
        </div>
        <OrderBadge status={order.status} />
      </div>
      {order.status === 'CANCELED' && <div className="alert red" style={{ marginBottom: 18 }}><XCircle size={18} /> Pedido cancelado em {dateTime(order.canceledAt)}. {order.cancelReason}</div>}
      <OrderView order={order} actions={actions} />
      <PaymentModal order={order} open={paying} onClose={() => { setPaying(false); refresh(); }} onChange={refresh} />
      <Modal open={canceling} onClose={() => setCanceling(false)} title="Cancelar pedido?">
        {(close) => (
          <div className="stack" style={{ gap: 18 }}>
            <p className="muted">Os livros voltam ao estoque e, se você já pagou, o valor é estornado na mesma forma de pagamento.</p>
            <div className="row">
              <button type="button" className="btn btn-dark" disabled={busy} onClick={() => cancel(close)}>{busy && <Spinner />} Sim, cancelar</button>
              <button type="button" className="btn btn-ghost" onClick={close}>Manter pedido</button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
