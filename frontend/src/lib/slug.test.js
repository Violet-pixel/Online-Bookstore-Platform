import { describe, expect, it } from 'vitest';
import { slugify } from './slug.js';
import { snapshot } from './library.jsx';

describe('autoria e Quero ler', () => {
  it('slug igual ao da API, sem acentos', () => {
    expect(slugify('Fiódor Dostoiévski')).toBe('fiodor-dostoievski');
    expect(slugify('  Machado de Assis ')).toBe('machado-de-assis');
  });
  it('guarda só o necessário do livro, com autores como texto', () => {
    const s = snapshot({ id: 1, slug: 'eu', title: 'Eu', authors: [{ name: 'Augusto dos Anjos', slug: 'x' }], price: '29.90', listPrice: '29.90', coverStyle: 2, inStock: true });
    expect(s.authors).toEqual(['Augusto dos Anjos']);
    expect(s.price).toBe(29.9);
    expect(s.synopsis).toBeUndefined();
  });
});
