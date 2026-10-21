// Aplica o tema salvo antes da primeira pintura (evita piscar claro/escuro).
try {
  var t = localStorage.getItem('prelo:tema');
  if (t === 'dark' || t === 'light') document.documentElement.dataset.theme = t;
} catch (e) { /* sem armazenamento */ }
