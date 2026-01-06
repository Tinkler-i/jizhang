document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.getElementById('loginBtn');
    const btnText = loginBtn.querySelector('.btn-text');
    const btnLoading = loginBtn.querySelector('.btn-loading');
    const alertMessage = document.getElementById('alertMessage');

    function showAlert(message, type = 'error') {
        alertMessage.textContent = message;
        alertMessage.className = `alert-message ${type}`;
        alertMessage.style.display = 'block';
        
        setTimeout(() => {
            alertMessage.style.display = 'none';
        }, 3000);
    }

    function validateUsername(username) {
        if (!username || username.trim() === '') {
            return '用户名不能为空';
        }
        if (username.length < 3) {
            return '用户名至少需要3个字符';
        }
        if (username.length > 20) {
            return '用户名不能超过20个字符';
        }
        return null;
    }

    function validatePassword(password) {
        if (!password || password.trim() === '') {
            return '密码不能为空';
        }
        if (password.length < 6) {
            return '密码至少需要6个字符';
        }
        if (password.length > 20) {
            return '密码不能超过20个字符';
        }
        return null;
    }

    function showError(inputElement, errorElement, message) {
        inputElement.classList.add('error');
        errorElement.textContent = message;
    }

    function clearError(inputElement, errorElement) {
        inputElement.classList.remove('error');
        errorElement.textContent = '';
    }

    usernameInput.addEventListener('blur', function() {
        const usernameError = document.getElementById('usernameError');
        const error = validateUsername(this.value);
        if (error) {
            showError(this, usernameError, error);
        } else {
            clearError(this, usernameError);
        }
    });

    passwordInput.addEventListener('blur', function() {
        const passwordError = document.getElementById('passwordError');
        const error = validatePassword(this.value);
        if (error) {
            showError(this, passwordError, error);
        } else {
            clearError(this, passwordError);
        }
    });

    usernameInput.addEventListener('input', function() {
        const usernameError = document.getElementById('usernameError');
        clearError(this, usernameError);
    });

    passwordInput.addEventListener('input', function() {
        const passwordError = document.getElementById('passwordError');
        clearError(this, passwordError);
    });

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();
        
        let hasError = false;
        
        const usernameError = document.getElementById('usernameError');
        const passwordError = document.getElementById('passwordError');
        
        const usernameValidationError = validateUsername(username);
        if (usernameValidationError) {
            showError(usernameInput, usernameError, usernameValidationError);
            hasError = true;
        }
        
        const passwordValidationError = validatePassword(password);
        if (passwordValidationError) {
            showError(passwordInput, passwordError, passwordValidationError);
            hasError = true;
        }
        
        if (hasError) {
            showAlert('请检查输入信息', 'error');
            return;
        }
        
        loginBtn.disabled = true;
        btnText.style.display = 'none';
        btnLoading.style.display = 'inline';
        
        try {
            const response = await fetch('/jizhang/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    password: password
                })
            });
            
            const result = await response.json();
            
            if (result.code === 200) {
                showAlert('登录成功，正在跳转...', 'success');
                setTimeout(() => {
                    window.location.href = '/jizhang/dashboard';
                }, 1000);
            } else {
                showAlert(result.message || '登录失败', 'error');
                loginBtn.disabled = false;
                btnText.style.display = 'inline';
                btnLoading.style.display = 'none';
            }
        } catch (error) {
            console.error('登录错误:', error);
            showAlert('网络错误，请稍后重试', 'error');
            loginBtn.disabled = false;
            btnText.style.display = 'inline';
            btnLoading.style.display = 'none';
        }
    });

    const rememberMe = document.getElementById('rememberMe');
    if (localStorage.getItem('rememberedUsername')) {
        usernameInput.value = localStorage.getItem('rememberedUsername');
        rememberMe.checked = true;
    }

    loginForm.addEventListener('submit', function() {
        if (rememberMe.checked) {
            localStorage.setItem('rememberedUsername', usernameInput.value);
        } else {
            localStorage.removeItem('rememberedUsername');
        }
    });
});