import { Check, MapPin, Package, Truck, Home, X } from 'lucide-react';
import { dateTime, SHIPMENT_STATUS } from '../lib/format.js';

const ORDER = ['PREPARING', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED'];
const ICON = { PREPARING: Package, IN_TRANSIT: Truck, OUT_FOR_DELIVERY: MapPin, DELIVERED: Home, RETURNED: X };

export function ShipmentSteps({ status }) {
  const at = ORDER.indexOf(status);
  return (
    <div className="steps">
      {ORDER.map((s, i) => (
        <div key={s} className={`step ${i < at || status === 'DELIVERED' ? 'done' : i === at ? 'now' : ''}`}>
          <div className="bar"><i /></div>
          {SHIPMENT_STATUS[s]}
        </div>
      ))}
    </div>
  );
}

export function Timeline({ events }) {
  const list = [...(events || [])].reverse();
  return (
    <div className="timeline">
      {list.map((e, i) => {
        const Icon = i === 0 && e.status !== 'DELIVERED' ? ICON[e.status] || Package : Check;
        return (
          <div key={`${e.occurredAt}-${i}`} className={`tl-item ${i === 0 ? (e.status === 'DELIVERED' ? 'done' : 'now') : 'done'}`} style={{ '--i': i }}>
            <span className="tl-dot"><Icon size={14} /></span>
            <div className="stack" style={{ gap: 2 }}>
              <h4>{e.title}</h4>
              {e.detail && <span className="muted" style={{ fontSize: 14 }}>{e.detail}</span>}
              <span className="when">{dateTime(e.occurredAt)}{e.location ? ` · ${e.location}` : ''}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
}
