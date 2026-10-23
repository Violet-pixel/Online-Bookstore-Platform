import { ArrowLeft } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router';
import BookCover from '../../components/BookCover.jsx';
import { Field, Spinner } from '../../components/Bits.jsx';
import { del, get, post, put } from '../../lib/api.js';
import { FORMAT } from '../../lib/format.js';
import { useFetch } from '../../lib/hooks.js';
import { useToast } from '../../lib/toast.jsx';

const EMPTY = {
  isbn: '', title: '', subtitle: '', synopsis: '', authors: '', publisher: '', categorySlug: '', languageCode: 'pt-BR',
  pageCount: '', publishedYear: '', format: 'PAPERBACK', price: '', listPrice: '', stockQuantity: 0, weightGrams: 400, coverStyle: 0, active: true,
};

export default function AdminBookForm() {
  const { id } = useParams();
  const isNew = id === 'novo';
  const navigate = useNavigate();
  const toast = useToast();
  const categories = useFetch('/api/admin/categories');
  const [b, setB] = useState(isNew ? EMPTY : null);
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!isNew) get(`/api/admin/books/${id}`).then((d) => setB({ ...d, authors: d.authors.join('; ') }));
  }, [id, isNew]);
  useEffect(() => {
    if (isNew && categories.data?.length) setB((s) => ({ ...s, categorySlug: s.categorySlug || categories.data[0].slug }));
  }, [isNew, categories.data]);
  if (!b) return <div className="skeleton" style={{ height: 500 }} />;
  const set = (k, v) => { setB((s) => ({ ...s, [k]: v })); setErrors((e) => ({ ...e, [k]: undefined })); };

  async function save(e) {
    e.preventDefault();
    setBusy(true);
    const body = {
      ...b, authors: b.authors.split(/[;\n]/).map((a) => a.trim()).filter(Boolean),
      pageCount: b.pageCount ? Number(b.pageCount) : null, publishedYear: b.publishedYear ? Number(b.publishedYear) : null,
      price: Number(b.price), listPrice: Number(b.listPrice || b.price), stockQuantity: Number(b.stockQuantity),
      weightGrams: Number(b.weightGrams), coverStyle: Number(b.coverStyle),
    };
    try {
      const saved = isNew ? await post('/api/admin/books', body) : await put(`/api/admin/books/${id}`, body);
      toast(isNew ? 'Livro cadastrado' : 'Livro atualizado');
      navigate(`/admin/livros/${saved.id}`, { replace: true });
      setB({ ...saved, authors: saved.authors.join('; ') });
    } catch (err) {
      setErrors(err.fields || {});
      toast(err.message, { error: true });
    } finally { setBusy(false); }
  }
  async function deactivate() {
    await del(`/api/admin/books/${id}`);
    toast('Livro retirado da loja');
    navigate('/admin/livros');
  }

  return (
    <form className="stack fade" style={{ gap: 18 }} onSubmit={save}>
      <div className="stack" style={{ gap: 8 }}>
        <Link to="/admin/livros" className="subtle row" style={{ gap: 6 }}><ArrowLeft size={14} /> Livros</Link>
        <h1>{isNew ? 'Novo livro' : b.title}</h1>
      </div>
      <div className="order-grid">
        <div className="card stack" style={{ gap: 14 }}>
          <div className="grid-2">
            <Field label="Título" error={errors.title}><input className="input" value={b.title} onChange={(e) => set('title', e.target.value)} /></Field>
            <Field label="Subtítulo"><input className="input" value={b.subtitle || ''} onChange={(e) => set('subtitle', e.target.value)} /></Field>
          </div>
          <div className="grid-2">
            <Field label="Autores" hint="Separe com ponto e vírgula." error={errors.authors}><input className="input" value={b.authors} onChange={(e) => set('authors', e.target.value)} /></Field>
            <Field label="Editora" error={errors.publisher}><input className="input" value={b.publisher} onChange={(e) => set('publisher', e.target.value)} /></Field>
          </div>
          <Field label="Sinopse"><textarea className="textarea" value={b.synopsis || ''} onChange={(e) => set('synopsis', e.target.value)} /></Field>
          <div className="grid-3">
            <Field label="ISBN" error={errors.isbn}><input className="input mono" value={b.isbn} onChange={(e) => set('isbn', e.target.value)} /></Field>
            <Field label="Categoria">
              <select className="select" value={b.categorySlug} onChange={(e) => set('categorySlug', e.target.value)}>
                {categories.data?.map((c) => <option key={c.slug} value={c.slug}>{c.name}</option>)}
              </select>
            </Field>
            <Field label="Formato">
              <select className="select" value={b.format} onChange={(e) => set('format', e.target.value)}>
                {Object.entries(FORMAT).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
              </select>
            </Field>
          </div>
          <div className="grid-3">
            <Field label="Preço de venda" error={errors.price}><input className="input" type="number" step="0.01" value={b.price} onChange={(e) => set('price', e.target.value)} /></Field>
            <Field label="Preço de capa" error={errors.listPrice}><input className="input" type="number" step="0.01" value={b.listPrice} onChange={(e) => set('listPrice', e.target.value)} /></Field>
            <Field label="Estoque"><input className="input" type="number" min="0" value={b.stockQuantity} onChange={(e) => set('stockQuantity', e.target.value)} /></Field>
          </div>
          <div className="grid-3">
            <Field label="Páginas"><input className="input" type="number" value={b.pageCount || ''} onChange={(e) => set('pageCount', e.target.value)} /></Field>
            <Field label="Ano"><input className="input" type="number" value={b.publishedYear || ''} onChange={(e) => set('publishedYear', e.target.value)} /></Field>
            <Field label="Peso (g)"><input className="input" type="number" value={b.weightGrams} onChange={(e) => set('weightGrams', e.target.value)} /></Field>
          </div>
          <label className="check"><input type="checkbox" checked={b.active} onChange={(e) => set('active', e.target.checked)} /> À venda na loja</label>
        </div>
        <div className="stack" style={{ gap: 18 }}>
          <div className="card stack" style={{ gap: 14 }}>
            <h3>Capa</h3>
            <div style={{ maxWidth: 200, margin: '0 auto', width: '100%' }}><BookCover title={b.title || 'Título'} authors={b.authors.split(';')[0]} style={Number(b.coverStyle)} /></div>
            <Field label="Estilo da capa"><input type="range" min="0" max="11" value={b.coverStyle} onChange={(e) => set('coverStyle', e.target.value)} /></Field>
          </div>
          <button type="submit" className="btn btn-primary btn-lg" disabled={busy}>{busy && <Spinner />} {isNew ? 'Cadastrar livro' : 'Salvar alterações'}</button>
          {!isNew && b.active && <button type="button" className="btn btn-ghost" onClick={deactivate}>Retirar da loja</button>}
        </div>
      </div>
    </form>
  );
}
