document.addEventListener('DOMContentLoaded', function() {
    // Переключение между формами
    document.getElementById('show-login').addEventListener('click', function(e) {
        e.preventDefault();
        document.getElementById('register-form').classList.add('hidden');
        document.getElementById('login-form').classList.remove('hidden');
    });

    document.getElementById('show-register').addEventListener('click', function(e) {
        e.preventDefault();
        document.getElementById('login-form').classList.add('hidden');
        document.getElementById('register-form').classList.remove('hidden');
    });

    // Обработка регистрации
    document.getElementById('register').addEventListener('submit', function(e) {
        e.preventDefault();
        const username = document.getElementById('reg-username').value;
        const password = document.getElementById('reg-password').value;

        fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.text();
        })
        .then(token => {
            localStorage.setItem('jwt', token);
            showResult('Регистрация успешна! Токен сохранён.', 'success');
            // Перенаправление на dashboard или другую страницу
            setTimeout(() => window.location.href = '/movies', 1500);
        })
        .catch(error => {
            showResult(`Ошибка регистрации: ${error.message}`, 'error');
        });
    });

    // Обработка входа
    document.getElementById('login').addEventListener('submit', function(e) {
        e.preventDefault();
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;

        fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.text();
        })
        .then(token => {
            localStorage.setItem('jwt', token);
            showResult('Вход выполнен! Токен сохранён.', 'success');
            // Перенаправление на dashboard или другую страницу
            setTimeout(() => window.location.href = '/movies', 1500);
        })
        .catch(error => {
            showResult(`Ошибка входа: ${error.message}`, 'error');
        });
    });

    function showResult(message, type) {
        const resultDiv = document.getElementById('auth-result');
        resultDiv.textContent = message;
        resultDiv.className = type;
    }
});