/*
 * Client-side validation mirrors ValidationUtil.java so the user gets
 * instant feedback — but the server re-checks every one of these
 * rules independently, since JavaScript validation can always be
 * bypassed.
 */
const RULES = {
  name: /^[A-Za-z][A-Za-z .'-]{1,79}$/,
  registerNumber: /^[A-Za-z0-9/-]{3,20}$/,
  email: /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/,
  phone: /^[6-9]\d{9}$/,
  password: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,64}$/
};

function showFieldError(input, message) {
  const err = input.parentElement.querySelector('.field-error');
  if (err) {
    err.textContent = message;
    err.style.display = message ? 'block' : 'none';
  }
  input.style.borderColor = message ? 'var(--rust)' : 'var(--line)';
}

function validateField(input) {
  const rule = input.dataset.rule;
  const value = input.value.trim();
  if (!rule) return true;

  if (input.required && !value) {
    showFieldError(input, 'This field is required.');
    return false;
  }
  if (!value) { showFieldError(input, ''); return true; }

  if (rule === 'confirmPassword') {
    const passwordField = document.getElementById(input.dataset.matches);
    if (passwordField && value !== passwordField.value) {
      showFieldError(input, 'Passwords do not match.');
      return false;
    }
    showFieldError(input, '');
    return true;
  }

  const regex = RULES[rule];
  if (regex && !regex.test(value)) {
    showFieldError(input, input.dataset.errorMessage || 'Invalid value.');
    return false;
  }
  showFieldError(input, '');
  return true;
}

function attachLiveValidation(form) {
  const fields = form.querySelectorAll('[data-rule]');
  fields.forEach(f => {
    f.addEventListener('blur', () => validateField(f));
    f.addEventListener('input', () => { if (f.style.borderColor === 'rgb(162, 62, 45)') validateField(f); });
  });

  form.addEventListener('submit', (e) => {
    let allValid = true;
    fields.forEach(f => { if (!validateField(f)) allValid = false; });
    if (!allValid) {
      e.preventDefault();
      const firstInvalid = form.querySelector('.field-error[style*="block"]');
      if (firstInvalid) firstInvalid.parentElement.querySelector('input,select,textarea').focus();
    }
  });
}

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('form[data-validate]').forEach(attachLiveValidation);
});
