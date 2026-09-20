const form = document.querySelector("#hello-form");
const nameInput = document.querySelector("#name");
const greetingResult = document.querySelector("#hello-result");
const piValue = document.querySelector("#pi-value");

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  greetingResult.classList.remove("error");
  greetingResult.textContent = "Loading greeting…";

  try {
    const response = await fetch(`/hello?name=${encodeURIComponent(nameInput.value)}`);
    if (!response.ok) throw new Error("The greeting endpoint could not respond.");
    greetingResult.textContent = await response.text();
  } catch (error) {
    greetingResult.classList.add("error");
    greetingResult.textContent = error.message;
  }
});

async function loadPi() {
  try {
    const response = await fetch("/pi");
    if (!response.ok) throw new Error("The pi endpoint could not respond.");
    piValue.textContent = await response.text();
  } catch (error) {
    piValue.textContent = "Unavailable";
  }
}

loadPi();
