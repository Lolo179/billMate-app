document.getElementById("loginForm").addEventListener("submit", async function(e) {
    e.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();
    const errorMsg = document.getElementById("error-msg");

    try {
        const response = await fetch("/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("jwt", data.token);
                setTimeout(() => {
                    window.location.href = "/dashboard";
                }, 50);
        } else {
            const msg = await response.text();
            errorMsg.style.display = "block";
            errorMsg.innerText = "❌ Error: " + msg;
        }
    } catch (err) {
        errorMsg.style.display = "block";
        errorMsg.innerText = "❌ Error de red. Intenta nuevamente.";
    }
});