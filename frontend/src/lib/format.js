const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const dateFmt = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short', year: 'numeric', timeZone: 'America/Sao_Paulo' });
const dateTimeFmt = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit', timeZone: 'America/Sao_Paulo' });
const dayFmt = new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', timeZone: 'UTC' });

export const money = (v) => brl.format(Number(v || 0));
export const date = (v) => (v ? dateFmt.format(new Date(v)) : '');
export const dateTime = (v) => (v ? dateTimeFmt.format(new Date(v)) : '');
export const longDay = (v) => (v ? dayFmt.format(new Date(`${v}T12:00:00Z`)) : '');
export const plural = (n, one, many) => `${n} ${n === 1 ? one : many}`;
export const discountPct = (price, list) => (Number(list) > Number(price) ? Math.round((1 - price / list) * 100) : 0);

export const ORDER_STATUS = {
  PENDING_PAYMENT: { label: 'Aguardando pagamento', tone: 'gold', live: true },
  PAID: { label: 'Pago, em separação', tone: 'rose', live: true },
  SHIPPED: { label: 'A caminho', tone: 'rose', live: true },
  DELIVERED: { label: 'Entregue', tone: 'green' },
  CANCELED: { label: 'Cancelado', tone: 'red' },
};
export const SHIPMENT_STATUS = {
  PREPARING: 'Em separação',
  IN_TRANSIT: 'Em trânsito',
  OUT_FOR_DELIVERY: 'Saiu para entrega',
  DELIVERED: 'Entregue',
  RETURNED: 'Cancelado',
};
export const PAYMENT_METHOD = { CREDIT_CARD: 'Cartão de crédito', PIX: 'Pix', BOLETO: 'Boleto' };
export const PAYMENT_STATUS = {
  PENDING: { label: 'Pendente', tone: 'gold' },
  APPROVED: { label: 'Aprovado', tone: 'green' },
  DECLINED: { label: 'Recusado', tone: 'red' },
  EXPIRED: { label: 'Expirado', tone: '' },
  REFUNDED: { label: 'Estornado', tone: '' },
  CANCELED: { label: 'Cancelado', tone: '' },
};
export const FORMAT = { PAPERBACK: 'Brochura', HARDCOVER: 'Capa dura', POCKET: 'Bolso' };

export function greeting() {
  const h = Number(new Intl.DateTimeFormat('pt-BR', { hour: 'numeric', hour12: false, timeZone: 'America/Sao_Paulo' }).format(new Date()));
  if (h < 12) return 'Bom dia';
  if (h < 18) return 'Boa tarde';
  return 'Boa noite';
}
