import { get, qs } from './api.js';

/** Busca todas as páginas de uma listagem da API (100 por vez). */
export async function fetchAll(path, params = {}) {
  const items = [];
  for (let page = 0; page < 200; page++) {
    const r = await get(`${path}${qs({ ...params, page, size: 100 })}`);
    items.push(...r.items);
    if (page >= r.totalPages - 1) break;
  }
  return items;
}

/** Gera e baixa um CSV com separador ; e BOM, do jeito que o Excel em português abre direto. */
export function downloadCsv(filename, columns, rows) {
  const cell = (v) => {
    const s = v === null || v === undefined ? '' : String(v);
    return /[";\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
  };
  const lines = [columns.map(([label]) => cell(label)).join(';'), ...rows.map((r) => columns.map(([, fn]) => cell(fn(r))).join(';'))];
  const blob = new Blob([`﻿${lines.join('\r\n')}`], { type: 'text/csv;charset=utf-8' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(a.href), 1000);
}

export const decimal = (v) => Number(v || 0).toFixed(2).replace('.', ',');
export const today = () => new Date().toISOString().slice(0, 10);
