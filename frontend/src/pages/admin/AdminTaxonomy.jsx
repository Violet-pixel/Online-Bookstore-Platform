import { Plus } from 'lucide-react';
import { useState } from 'react';
import { Field, Spinner } from '../../components/Bits.jsx';
import { post, put } from '../../lib/api.js';
import { useFetch } from '../../lib/hooks.js';
import { useToast } from '../../lib/toast.jsx';

// Cores da marca para a cor de cada categoria
const PALETTE = ['#B86B84', '#8E3B59', '#E0B94F', '#3F5F4B', '#4E6178', '#212121', '#DADADA'];

function CategoryRow({ c, onSave }) {
  const [v, setV] = useState(c);
  const [busy, setBusy] = useState(false);
  const dirty = JSON.stringify(v) !== JSON.stringify(c);
  return (
    <div className="addr" style={{ alignItems: 'center' }}>
      <div className="row grow wrap" style={{ gap: 10 }}>
        <div className="row" style={{ gap: 4 }}>
          {PALETTE.map((p) => (
            <button key={p} type="button" aria-label={`Cor ${p}`} onClick={() => setV({ ...v, accent: p })}
              style={{ width: 20, height: 20, borderRadius: 0, border: v.accent === p ? '2px solid var(--text)' : '1px solid var(--line)', outline: v.accent === p ? '1px solid var(--bg)' : 'none', outlineOffset: -3, background: p }} />
          ))}
        </div>
        <input className="input grow" style={{ height: 38, minWidth: 160 }} value={v.name} onChange={(e) => setV({ ...v, name: e.target.value })} />
        <label className="check"><input type="checkbox" checked={v.active} onChange={(e) => setV({ ...v, active: e.target.checked })} /> ativa</label>
      </div>
      {dirty && <button type="button" className="btn btn-primary btn-sm fade" disabled={busy}
        onClick={async () => { setBusy(true); try { await onSave(v); } finally { setBusy(false); } }}>{busy && <Spinner size={13} />} Salvar</button>}
    </div>
  );
}

export default function AdminTaxonomy() {
  const toast = useToast();
  const categories = useFetch('/api/admin/categories');
  const catalogs = useFetch('/api/admin/catalogs');
  const [name, setName] = useState('');
  const [catName, setCatName] = useState('');

  async function saveCategory(c) {
    try {
      await put(`/api/admin/categories/${c.id}`, c);
      categories.reload();
      toast('Categoria salva');
    } catch (e) { toast(e.message, { error: true }); }
  }
  async function addCategory(e) {
    e.preventDefault();
    try {
      await post('/api/admin/categories', { name, description: null, accent: '#B86B84', sortOrder: (categories.data?.length || 0) + 1, active: true });
      setName('');
      categories.reload();
    } catch (err) { toast(err.message, { error: true }); }
  }
  async function addCatalog(e) {
    e.preventDefault();
    try {
      await post('/api/admin/catalogs', { name: catName, description: null, highlight: null, sortOrder: (catalogs.data?.length || 0) + 1, active: true, bookIds: [] });
      setCatName('');
      catalogs.reload();
    } catch (err) { toast(err.message, { error: true }); }
  }
  async function toggleCatalog(c) {
    await put(`/api/admin/catalogs/${c.id}`, { ...c, active: !c.active, bookIds: null });
    catalogs.reload();
  }

  return (
    <div className="stack fade" style={{ gap: 22 }}>
      <h1>Categorias e catálogos</h1>
      <section className="card">
        <div className="panel-title"><h3>Categorias</h3></div>
        <div className="stack" style={{ gap: 8 }}>
          {categories.data?.map((c) => <CategoryRow key={`${c.id}-${c.name}-${c.accent}-${c.active}`} c={c} onSave={saveCategory} />)}
        </div>
        <form className="row" style={{ marginTop: 14 }} onSubmit={addCategory}>
          <Field><input className="input" placeholder="Nova categoria" value={name} onChange={(e) => setName(e.target.value)} /></Field>
          <button type="submit" className="btn" disabled={!name.trim()}><Plus size={15} /> Adicionar</button>
        </form>
      </section>
      <section className="card">
        <div className="panel-title"><h3>Catálogos</h3><span className="subtle">A ordem dos livros segue a lista do catálogo.</span></div>
        <div className="stack" style={{ gap: 8 }}>
          {catalogs.data?.map((c) => (
            <div key={c.id} className="addr" style={{ alignItems: 'center' }}>
              <div className="stack" style={{ gap: 2 }}><b>{c.name}</b><span className="subtle">{c.highlight ? `${c.highlight} ` : ''}{c.bookIds.length} livros</span></div>
              <label className="check"><input type="checkbox" checked={c.active} onChange={() => toggleCatalog(c)} /> visível</label>
            </div>
          ))}
        </div>
        <form className="row" style={{ marginTop: 14 }} onSubmit={addCatalog}>
          <Field><input className="input" placeholder="Novo catálogo" value={catName} onChange={(e) => setCatName(e.target.value)} /></Field>
          <button type="submit" className="btn" disabled={!catName.trim()}><Plus size={15} /> Adicionar</button>
        </form>
      </section>
    </div>
  );
}
