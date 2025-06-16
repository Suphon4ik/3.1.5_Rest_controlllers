document.addEventListener("DOMContentLoaded", function () {
    loadUsers();

    document.getElementById("createUserBtn").addEventListener("click", createUser);
});


function loadUsers() {
    fetch("/api/admin/users")
        .then(response => response.json())
        .then(users => {
            const tbody = document.querySelector("#userTable tbody");
            tbody.innerHTML = "";
            users.forEach(user => {
                const row = document.createElement("tr");
                row.innerHTML = `
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.country}</td>
                    <td>${user.car}</td>
                    <td>${user.roles.map(role => role.name).join(", ")}</td>
                    <td>${user.password}</td>
                    <td><button class="btn btn-warning" onclick="editUser(${user.id})">Update</button></td>
                    <td><button class="btn btn-danger" onclick="deleteUser(${user.id})">Delete</button></td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(error => console.error("Error loading users:", error));
}

function createUser() {
    const user = {
        username: document.getElementById("newUsername").value,
        country: document.getElementById("newCountry").value,
        car: document.getElementById("newCar").value,
        password: document.getElementById("newPassword").value,
        roles: Array.from(document.getElementById("roleNew").selectedOptions).map(opt => ({id: parseInt(opt.value)}))
    };

    fetch("/api/admin/new", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(user)
    })
        .then(response => {
            if (!response.ok) throw new Error("Failed to create user");
            return response.json();
        })
        .then(() => {
            loadUsers(); // Обновляем таблицу пользователей
            document.getElementById("createUserForm").reset(); // Очищаем форму

            let usersTableTab = new bootstrap.Tab(document.querySelector("#usersTable-tab"));
            usersTableTab.show();
        })
        .catch(error => console.error("Error creating user:", error));
}


function deleteUser(id) {
    if (!confirm("Are you sure you want to delete this user?")) return;

    fetch(`/api/admin/${id}`)
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch user data");
            return response.json();
        })
        .then(user => {
            const isAdmin = user.roles.some(role => role.name === "ROLE_ADMIN");

            if (isAdmin) {
                alert("You cannot delete a user with the ADMIN role!");
                return;
            }

            fetch(`/api/admin/delete/${id}`, {method: "DELETE"})
                .then(response => {
                    if (!response.ok) throw new Error("Failed to delete user");
                    return response.text();
                })
                .then(() => loadUsers())
                .catch(error => console.error("Error deleting user:", error));
        })
        .catch(error => console.error("Error fetching user data:", error));
}

function editUser(id) {
    fetch(`/api/admin/${id}`)
        .then(response => response.json())
        .then(user => {
            document.getElementById("UpdateUsername").value = user.username;
            document.getElementById("UpdateCountry").value = user.country;
            document.getElementById("UpdateCar").value = user.car;
            document.getElementById("UpdatePassword").value = "";

            const roleSelect = document.getElementById("roleSelect");
            Array.from(roleSelect.options).forEach(option => {
                option.selected = user.roles.some(role => role.id == option.value);
            });
            document.getElementById("updateModal").dataset.userId = id;
            new bootstrap.Modal(document.getElementById("updateModal")).show();
        })
        .catch(error => console.error("Error loading user:", error));
}

function updateUser() {
    const id = document.getElementById("updateModal").dataset.userId;

    fetch(`/api/admin/${id}`)
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch user data");
            return response.json();
        })
        .then(user => {
            const isAdmin = user.roles.some(role => role.name === "ROLE_ADMIN");

            if (isAdmin) {
                alert("You cannot update a user with the ADMIN role!");
                return;
            }

            const updatedUser = {
                username: document.getElementById("UpdateUsername").value,
                country: document.getElementById("UpdateCountry").value,
                car: document.getElementById("UpdateCar").value,
                password: document.getElementById("UpdatePassword").value,
                roles: Array.from(document.getElementById("roleSelect").selectedOptions)
                    .map(opt => ({id: parseInt(opt.value)}))
            };

            fetch(`/api/admin/update/${id}`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(updatedUser)
            })
                .then(response => {
                    if (!response.ok) throw new Error("Failed to update user");
                    return response.text();
                })
                .then(() => {
                    loadUsers();
                    bootstrap.Modal.getInstance(document.getElementById("updateModal")).hide();
                })
                .catch(error => console.error("Error updating user:", error));
        })
        .catch(error => console.error("Error fetching user data:", error));
}

document.getElementById("logoutButton").addEventListener("click", function () {
    fetch("/logout", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
    })
        .then(response => {
            if (response.ok) {
                window.location.href = "/login";
            } else {
                alert("Ошибка при выходе из системы.");
            }
        })
        .catch(error => {
            console.error("Ошибка при выполнении логаута:", error);
        });
});
