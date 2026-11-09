const form = document.querySelector('form');
if (form) {
  form.addEventListener('submit', () => {
    form.querySelector('button[type="submit"]').disabled = true;
    document.getElementById('save-state').textContent = '저장 중…';
  });
  window.addEventListener('pageshow', () => {
    form.querySelector('button[type="submit"]').disabled = false;
    document.getElementById('save-state').textContent = '';
  });
}
