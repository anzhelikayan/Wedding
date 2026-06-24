const openButton = document.querySelector("#openInvitation");
const cover = document.querySelector("#cover");

if (openButton && cover) {
    openButton.addEventListener("click", () => {
        openButton.classList.add("opened");
        window.setTimeout(() => {
            smoothScrollToElement(document.querySelector("#intro"), 760);
        }, 120);
    });
}

if (openButton) {
    openButton.addEventListener("pointermove", (event) => {
        const rect = openButton.getBoundingClientRect();
        const x = (event.clientX - rect.left) / rect.width - 0.5;
        const y = (event.clientY - rect.top) / rect.height - 0.5;
        openButton.style.setProperty("--tilt-x", `${x * 7}deg`);
        openButton.style.setProperty("--tilt-y", `${y * -7}deg`);
    });

    openButton.addEventListener("pointerleave", () => {
        openButton.style.setProperty("--tilt-x", "0deg");
        openButton.style.setProperty("--tilt-y", "0deg");
    });
}

const countdown = document.querySelector("[data-countdown]");
const rsvpForm = document.querySelector(".rsvp form");
const formSuccess = document.querySelector(".form-success");
const animatedScreens = document.querySelectorAll(".screen");

function smoothScrollToElement(element, duration = 1900) {
    if (!element) {
        return;
    }

    element.classList?.add("is-visible");
    const start = window.scrollY;
    const target = element.getBoundingClientRect().top + window.scrollY;
    const distance = target - start;
    const startedAt = performance.now();

    function easeInOutCubic(progress) {
        return progress < 0.5
            ? 4 * progress * progress * progress
            : 1 - Math.pow(-2 * progress + 2, 3) / 2;
    }

    function step(now) {
        const progress = Math.min((now - startedAt) / duration, 1);
        window.scrollTo(0, start + distance * easeInOutCubic(progress));

        if (progress < 1) {
            window.requestAnimationFrame(step);
        }
    }

    window.requestAnimationFrame(step);
}

function createCelebrationBurst() {
    const colors = ["#b7aa77", "#d8ce9f", "#8f9d7a", "#fff2d9"];
    const count = 34;

    for (let i = 0; i < count; i++) {
        const petal = document.createElement("span");
        petal.className = "burst-petal";
        petal.style.background = `linear-gradient(145deg, ${colors[i % colors.length]}, rgba(255,250,242,.82))`;
        petal.style.setProperty("--dx", `${Math.cos(i / count * Math.PI * 2) * (90 + Math.random() * 120)}px`);
        petal.style.setProperty("--dy", `${Math.sin(i / count * Math.PI * 2) * (80 + Math.random() * 160) - 80}px`);
        petal.style.setProperty("--rotate", `${Math.random() * 360}deg`);
        document.body.appendChild(petal);

        window.setTimeout(() => petal.remove(), 1500);
    }
}

if ("IntersectionObserver" in window && animatedScreens.length > 0) {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add("is-visible");
            }
        });
    }, { threshold: 0.26 });

    animatedScreens.forEach((screen) => observer.observe(screen));
} else {
    animatedScreens.forEach((screen) => screen.classList.add("is-visible"));
}

animatedScreens.forEach((screen) => {
    const rect = screen.getBoundingClientRect();
    if (rect.top < window.innerHeight * 0.86 && rect.bottom > 0) {
        screen.classList.add("is-visible");
    }
});

if (rsvpForm) {
    rsvpForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const submitButton = rsvpForm.querySelector('button[type="submit"]');
        const originalText = submitButton?.textContent;

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = "Відправляємо...";
        }

        try {
            await fetch(rsvpForm.action, {
                method: "POST",
                body: new FormData(rsvpForm),
                redirect: "follow"
            });

            if (formSuccess) {
                formSuccess.hidden = false;
            }

            createCelebrationBurst();
            smoothScrollToElement(document.querySelector("#countdown"), 2200);
        } catch (error) {
            rsvpForm.submit();
        } finally {
            if (submitButton) {
                window.setTimeout(() => {
                    submitButton.disabled = false;
                    submitButton.textContent = originalText;
                }, 900);
            }
        }
    });
}

function updateCountdown() {
    if (!countdown) {
        return;
    }

    const target = new Date(countdown.dataset.countdown).getTime();
    const distance = Math.max(target - Date.now(), 0);

    const days = Math.floor(distance / (1000 * 60 * 60 * 24));
    const hours = Math.floor((distance / (1000 * 60 * 60)) % 24);
    const minutes = Math.floor((distance / (1000 * 60)) % 60);
    const seconds = Math.floor((distance / 1000) % 60);

    countdown.querySelector("[data-days]").textContent = days;
    countdown.querySelector("[data-hours]").textContent = String(hours).padStart(2, "0");
    countdown.querySelector("[data-minutes]").textContent = String(minutes).padStart(2, "0");
    countdown.querySelector("[data-seconds]").textContent = String(seconds).padStart(2, "0");
}

updateCountdown();
window.setInterval(updateCountdown, 1000);
