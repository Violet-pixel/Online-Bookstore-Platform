import { describe, expect, it } from 'vitest';
import { discountPct, money, ORDER_STATUS, plural } from './format.js';

describe('formatação', () => {
  it('moeda em reais', () => {
    expect(money(1234.5).replace(/\s/g, ' ')).toBe('R$ 1.234,50');
  });
  it('desconto em porcentagem', () => {
    expect(discountPct(34.9, 44.9)).toBe(22);
    expect(discountPct(50, 50)).toBe(0);
  });
  it('plural', () => {
    expect(plural(1, 'livro', 'livros')).toBe('1 livro');
    expect(plural(3, 'livro', 'livros')).toBe('3 livros');
  });
  it('todos os status de pedido têm rótulo', () => {
    ['PENDING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELED'].forEach((s) => expect(ORDER_STATUS[s].label).toBeTruthy());
  });
});
