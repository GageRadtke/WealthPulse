import apiClient, { TOKEN_KEY } from '../../../api/client';


/**
 * Login with username and password.
 * @returns {Promise<{token, user: {username, email, name}, expiresInMs}>}
 */
export async function login(credentials) {
  const response = await apiClient.post('/auth/login', credentials);
  const { token, username, email, name, expiresInMs } = response.data;
  
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  }
  return { 
    token, 
    user: { username, email, name: name || username }, 
    expiresInMs 
  };
}

/**
 * Register a new account.
 * @returns {Promise<{token, user: {username, email, name}, expiresInMs}>}
 */
export async function register(username, password, email) {
  const response = await apiClient.post("/auth/register", {
    username,
    password,
    email,
  });

  const {
    token,
    username: registeredUsername,
    email: registeredEmail,
    name,
    expiresInMs,
  } = response.data;

  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  }

  return {
    token,
    user: {
      username: registeredUsername,
      email: registeredEmail,
      name: name || registeredUsername,
    },
    expiresInMs,
  };
}

/**
 * Fetch the currently authenticated user details from the server using the token.
 */
export async function fetchCurrentUser() {
  const response = await apiClient.get("/auth/me");
  return response.data;
}

/**
 * Clear the stored token (logout).
 */
export function logout() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getCurrentUsername() {
  try {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub || null;
  } catch {
    return null;
  }
}

