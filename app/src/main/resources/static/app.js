document.querySelectorAll('form').forEach(form => {
  const button = form.querySelector('button[type="submit"]');
  const state = form.querySelector('[data-submit-state], #save-state');
  form.addEventListener('submit', () => {
    if (button) button.disabled = true;
    if (state) state.textContent = '처리 중…';
  });
  window.addEventListener('pageshow', () => {
    if (button) button.disabled = false;
    if (state) state.textContent = '';
  });
});
