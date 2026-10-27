// Capas tipográficas geradas pela própria interface, no jeito das capas de tecido e papel: nenhuma imagem de terceiros.
// Cada estilo combina uma cor de encadernação com um leiaute (etiqueta colada, faixa, moldura, régua, lua, pauta).
export const COVERS = [
  { c1: '#2B2A33', c2: '#E0B94F', fg: '#F2EDE4', layout: 'label' },
  { c1: '#8E3B59', c2: '#F2E6DA', fg: '#FBF3F5', layout: 'rule' },
  { c1: '#E0B94F', c2: '#2B2A33', fg: '#2B2A33', layout: 'moon' },
  { c1: '#3F5F4B', c2: '#E8DFC9', fg: '#F1ECDF', layout: 'frame' },
  { c1: '#EFE9DF', c2: '#8E3B59', fg: '#2B2A33', layout: 'band' },
  { c1: '#4E6178', c2: '#E0B94F', fg: '#F2EEE6', layout: 'lines' },
  { c1: '#B86B84', c2: '#2B2A33', fg: '#2B2A33', layout: 'label' },
  { c1: '#9C4A3C', c2: '#F0E2C8', fg: '#F7EDE0', layout: 'frame' },
  { c1: '#211F22', c2: '#B86B84', fg: '#F2EDE4', layout: 'moon' },
  { c1: '#D9D0C1', c2: '#3F5F4B', fg: '#2B2A33', layout: 'rule' },
  { c1: '#4A3956', c2: '#E0B94F', fg: '#F1EAF0', layout: 'band' },
  { c1: '#76805A', c2: '#F1ECDF', fg: '#F7F4EA', layout: 'lines' },
];

export const coverOf = (style = 0) => COVERS[Math.abs(Number(style) || 0) % COVERS.length];

export default function BookCover({ title, authors, style = 0, className = '', showText = true }) {
  const s = coverOf(style);
  const author = Array.isArray(authors) ? authors.join(', ') : authors;
  return (
    <div className={`cover layout-${s.layout} ${className}`} style={{ '--c1': s.c1, '--c2': s.c2, '--fg': s.fg }} aria-hidden="true">
      <span className="hinge" />
      {s.layout === 'moon' && <span className="moon" />}
      {s.layout === 'lines' && <span className="lines" />}
      {s.layout === 'band' && <span className="band" />}
      {s.layout === 'frame' && <span className="frame" />}
      {showText && (
        <div className="txt">
          <div className="t">{title}</div>
          {s.layout === 'rule' && <span className="rl" />}
          <div className="a">{author}</div>
        </div>
      )}
    </div>
  );
}
