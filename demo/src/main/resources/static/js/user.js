document.addEventListener("DOMContentLoaded", function () {
    loadUserInfo();
});

function loadUserInfo() {
    fetch("/api/user")  //
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to load user data");
            }
            return response.json();
        })
        .then(user => {
            document.getElementById("userId").textContent = user.id;
            document.getElementById("username").textContent = user.username;
            document.getElementById("country").textContent = user.country;
            document.getElementById("car").textContent = user.car;
            document.getElementById("roles").textContent = user.roles
                .map(role => role.name.replace("ROLE_", "")) // Преобразуем в читаемый вид
                .join(", ");
            document.getElementById("password").textContent = user.password;
        })
        .catch(error => {
            console.error("Error:", error);
        });
}
