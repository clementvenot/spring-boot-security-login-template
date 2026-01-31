/** 
 * Shows/hides the password when clicking the "monkey" button. 
 * - The button must have a data-target attribute with the id of the input field. 
 * - Also toggles the icon 🙈 <-> 🐵 and updates aria-pressed. 
 */
function togglePasswordIcon(button) {
    const inputId = button.getAttribute("data-target");
    const input = document.getElementById(inputId);

    if (!input) return;

    const isHidden = input.type === "password";

    // Change the input field type
    input.type = isHidden ? "text" : "password";

    // Toggle the icon
    button.textContent = isHidden ? "🐵" : "🙈";

    // Accessibility updates
    button.setAttribute("aria-pressed", isHidden.toString());
}

// Optional: allows functionality even without inline "onclick".
// Here we bind the event to all buttons with the .pw-toggle class.
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".pw-toggle").forEach(btn => {
        // If an onclick attribute is already present, do not add another handler.
        if (!btn.hasAttribute("onclick")) {
            btn.addEventListener("click", () => togglePasswordIcon(btn));
        }
    });

    // Small bonus: enable/disable the Submit button when passwords match
    const pwd = document.getElementById("password");
    const confirm = document.getElementById("confirmPassword");
    const submitBtn = document.getElementById("submitBtn");
    const help = document.getElementById("pwdHelp");

    if (pwd && confirm && submitBtn) {
        const checkMatch = () => {
            const ok = pwd.value.length > 0 && confirm.value.length > 0 && pwd.value === confirm.value;
            submitBtn.disabled = !ok;

            if (confirm.value.length > 0) {
                confirm.classList.toggle("mismatch", !ok);
                confirm.classList.toggle("valid", ok);
            } else {
                confirm.classList.remove("mismatch", "valid");
            }

            if (help) help.style.display = ok ? "none" : (confirm.value.length ? "block" : "none");
        };
        pwd.addEventListener("input", checkMatch);
        confirm.addEventListener("input", checkMatch);
    }
});