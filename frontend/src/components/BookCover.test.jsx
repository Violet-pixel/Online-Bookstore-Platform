import { render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import BookCover from './BookCover.jsx';

describe('capa gerada', () => {
  it('mostra título e autor com qualquer estilo', () => {
    for (let style = 0; style < 14; style++) {
      const { container, unmount } = render(<BookCover title="Dom Casmurro" authors={['Machado de Assis']} style={style} />);
      expect(container.textContent).toContain('Dom Casmurro');
      expect(container.textContent).toContain('Machado de Assis');
      unmount();
    }
  });
  it('pode esconder o texto (miniaturas)', () => {
    const { container } = render(<BookCover title="Eu" style={3} showText={false} />);
    expect(container.textContent).toBe('');
  });
});
