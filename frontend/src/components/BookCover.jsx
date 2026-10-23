// Capas tipográficas impressas com as tintas de processo do projeto: nenhuma imagem de terceiros.
const P = { key: '#1B1B1D', paper: '#F6F6F2', cyan: '#0A84B8', magenta: '#C8236A', yellow: '#F2C230', card: '#E4E4DD' };
const STYLES = [
  { c1: P.key, c2: P.magenta, fg: P.paper, art: 'arc' },
  { c1: P.magenta, c2: P.yellow, fg: '#FFFFFF', art: 'bands' },
  { c1: P.yellow, c2: P.cyan, fg: P.key, art: 'circle' },
  { c1: P.cyan, c2: P.key, fg: P.paper, art: 'frame' },
  { c1: P.paper, c2: P.magenta, fg: P.key, art: 'stack' },
  { c1: P.key, c2: P.yellow, fg: P.yellow, art: 'sun' },
  { c1: P.card, c2: P.key, fg: P.key, art: 'grid' },
  { c1: P.magenta, c2: P.key, fg: P.paper, art: 'arc' },
  { c1: P.key, c2: P.cyan, fg: P.paper, art: 'bands' },
  { c1: P.yellow, c2: P.magenta, fg: P.key, art: 'sun' },
  { c1: P.paper, c2: P.cyan, fg: P.key, art: 'circle' },
  { c1: P.card, c2: P.magenta, fg: P.key, art: 'frame' },
];

function Art({ kind, c2 }) {
  switch (kind) {
    case 'arc':
      return <svg className="art" viewBox="0 0 200 300" preserveAspectRatio="none"><circle cx="200" cy="300" r="150" fill={c2} /><circle cx="200" cy="300" r="95" fill="none" stroke="currentColor" strokeOpacity=".25" strokeWidth="2" /></svg>;
    case 'bands':
      return <svg className="art" viewBox="0 0 200 300" preserveAspectRatio="none"><rect y="190" width="200" height="26" fill={c2} /><rect y="226" width="200" height="10" fill={c2} opacity=".7" /><rect y="246" width="200" height="4" fill={c2} opacity=".5" /></svg>;
    case 'circle':
      return <svg className="art" viewBox="0 0 200 300" preserveAspectRatio="none"><circle cx="100" cy="196" r="58" fill={c2} /><circle className="ov" cx="140" cy="166" r="34" fill="#C8236A" /></svg>;
    case 'frame':
      return <svg className="art" viewBox="0 0 200 300" preserveAspectRatio="none"><rect x="14" y="14" width="172" height="272" rx="4" fill="none" stroke={c2} strokeWidth="2" /><rect x="22" y="22" width="156" height="256" rx="2" fill="none" stroke={c2} strokeOpacity=".4" /><path d="M60 230 L100 190 L140 230 Z" fill={c2} /></svg>;
    case 'stack':
      return <svg className="art" viewBox="0 0 200 300" preserveAspectRatio="none"><rect x="38" y="236" width="130" height="18" fill="#1B1B1D" /><rect className="ov" x="50" y="212" width="110" height="22" fill={c2} /><rect className="ov" x="42" y="196" width="116" height="22" fill="#0A84B8" /></svg>;
    case 'sun':
      return <svg className="art" viewBox="0 0 200 300" preserveAspectRatio="none"><circle cx="100" cy="250" r="64" fill={c2} /><g stroke={c2} strokeWidth="4" strokeLinecap="round" opacity=".7"><path d="M100 168v-16M40 196l-12-10M160 196l12-10M22 248H6M178 248h16" /></g></svg>;
    default:
      return <svg className="art" viewBox="0 0 200 300" preserveAspectRatio="none"><g stroke={c2} strokeOpacity=".22">{Array.from({ length: 9 }, (_, i) => <path key={i} d={`M${i * 25} 160V300M0 ${160 + i * 18}H200`} />)}</g><rect x="120" y="200" width="44" height="44" fill={c2} opacity=".8" /></svg>;
  }
}

export default function BookCover({ title, authors, style = 0, className = '', showText = true }) {
  const s = STYLES[Math.abs(style) % STYLES.length];
  const author = Array.isArray(authors) ? authors.join(', ') : authors;
  return (
    <div className={`cover ${className}`} style={{ '--c1': s.c1, '--c2': s.c2, '--fg': s.fg }} aria-hidden="true">
      <Art kind={s.art} c2={s.c2} />
      {showText && (
        <div className="txt">
          <div className="t">{title}</div>
          <div className="a">{author}</div>
          <div className="pub">Prelo</div>
        </div>
      )}
    </div>
  );
}
