/** Mesmo slug da API: sem acentos, minúsculo, palavras unidas por hífen. */
export const slugify = (s) => (s || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
  .replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
