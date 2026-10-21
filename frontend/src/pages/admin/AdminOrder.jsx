import { ArrowLeft, ArrowRight, XCircle } from 'lucide-react';
import { useState } from 'react';
import { Link, useParams } from 'react-router';
import { Field, OrderBadge, Spinner } from '../../components/Bits.jsx';
import { post } from '../../lib/api.js';
import { dateTime, SHIPMENT_STATUS } from '../../lib/format.js';
import { useFetch } from '../../lib/hooks.js';
import { useToast } from '../../lib/toast.jsx';
import { OrderView } from '../Order.jsx';

const NEXT = { PREPARING: 'IN_TRANSIT', IN_TRANSIT: 'OUT_FOR_DELIVERY', OUT_FOR_DELIVERY: 'DELIVERED' };

export default function AdminOrder() {
  const { code } = useParams();
  const toast = useToast();
  const { data: order, setData } = useFetch(`/api/admin/orders/${code}`);
  const [tracking, setTracking] = useState('');
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState('');
  if (!order) return <div className="skeleton" style={{ height: 400 }} />;

  const next = order.shipment && NEXT[order.shipment.status];
  async function advance() {
    setBusy('advance');
    try {
      setData(await post(`/api/admin/orders/${code}/shipment/advance`, { status: next, trackingCode: tracking || null, note: note || null }));
      setNote('');
      toast(`Envio atualizado: ${SHIPMENT_STATUS[next]}`);
    } catch (e) { toast(e.message, { error: true }); } finally { setBusy(''); }
  }
  async function cancel() {
    setBusy('cancel');
    try {
      setData(await post(`/api/admin/orders/${code}/cancel`, { reason: 'Cancelado pela livraria.' }));
      toast('Pedido cancelado');
    } catch (e) { toast(e.message, { error: true }); } finally { setBusy(''); }
  }

  const actions = (
    <section className="card rise" style={{ borderColor: next ? 'var(--accent)' : undefined }}>
      <h3 style={{ marginBottom: 12 }}>Ações</h3>
      {next ? (
        <div className="stack" style={{ gap: 12 }}>
          {!order.shipment.trackingCode && (
            <Field label="Código de rastreio" hint="Deixe vazio para gerar um automaticamente.">
              <input className="input" value={tracking} onChange={(e) => setTracking(e.target.value.toUpperCase())} placeholder="LV123456789BR" />
            </Field>
          )}
          <Field label="Observação para o cliente (opcional)"><input className="input" value={note} onChange={(e) => setNote(e.target.value)} /></Field>
          <button type="button" className="btn btn-primary btn-block" disabled={!!busy} onClick={advance}>
            {busy === 'advance' ? <Spinner /> : null} Marcar como “{SHIPMENT_STATUS[next]}” <ArrowRight size={16} />
          </button>
        </div>
      ) : <p className="muted">{order.status === 'PENDING_PAYMENT' ? 'Aguardando o pagamento do cliente.' : 'Nenhuma ação pendente.'}</p>}
      {order.canCancel && (
        <button type="button" className="btn btn-ghost btn-block" style={{ marginTop: 8 }} disabled={!!busy} onClick={cancel}>
          {busy === 'cancel' ? <Spinner /> : <XCircle size={16} />} Cancelar e estornar
        </button>
      )}
    </section>
  );

  return (
    <div className="stack fade" style={{ gap: 18 }}>
      <div className="order-head" style={{ paddingTop: 0 }}>
        <div className="stack" style={{ gap: 8 }}>
          <Link to="/admin/pedidos" className="subtle row" style={{ gap: 6 }}><ArrowLeft size={14} /> Pedidos</Link>
          <h1 style={{ fontSize: 32 }}>Pedido <span className="mono" style={{ fontSize: 28 }}>{order.code}</span></h1>
          <span className="muted">{order.customer?.fullName} · {dateTime(order.placedAt)}</span>
        </div>
        <OrderBadge status={order.status} />
      </div>
      <OrderView order={order} admin actions={actions} />
    </div>
  );
}
