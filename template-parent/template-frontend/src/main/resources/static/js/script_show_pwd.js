// src/main/resources/static/js/script_show_pwd.js
(function () {
  /**
   * Toggle password visibility + met à jour icon/ARIA.
   * inputId: id de l'input
   * btn: bouton déclencheur (this)
   * 🙈 (hidden) -> 🙉 (visible)
   */
  window.togglePasswordIcon = function (inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input || !btn) return;

    const isHidden = input.type === 'password';
    input.type = isHidden ? 'text' : 'password';

    btn.textContent = isHidden ? '🙉' : '🙈';
    btn.setAttribute('aria-pressed', String(isHidden));
    btn.setAttribute('aria-label', isHidden ? 'Hide password' : 'Show password');
    btn.setAttribute('title', isHidden ? 'Hide password' : 'Show password');
  };

  // Validation simple "password === confirmPassword"
  window.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('register-form');
    // Si on est sur la page Login, ces éléments n'existent pas : on sort proprement
    if (!form) return;

    const pwd = document.getElementById('password');
    const confirm = document.getElementById('confirmPassword');
    const submitBtn = document.getElementById('submitBtn');
    const help = document.getElementById('pwdHelp');

    function checkMatch() {
      const ok = pwd.value === confirm.value && pwd.value.length > 0;

      if (!ok) {
        confirm.setCustomValidity("Passwords do not match");
        confirm.classList.add("mismatch");
        confirm.classList.remove("valid");
        if (help) help.style.display = "block";
        if (submitBtn) submitBtn.disabled = true;
      } else {
        confirm.setCustomValidity("");
        confirm.classList.remove("mismatch");
        confirm.classList.add("valid");
        if (help) help.style.display = "none";
        if (submitBtn) submitBtn.disabled = !form.checkValidity();
      }
    }

    pwd.addEventListener("input", checkMatch);
    confirm.addEventListener("input", checkMatch);
    checkMatch(); // état initial
  });
})();
