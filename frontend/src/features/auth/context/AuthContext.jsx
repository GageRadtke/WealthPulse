import {
  useState,
  useCallback,
  useEffect,
} from "react";

import apiClient, { TOKEN_KEY } from "../../../api/client";
import * as authApi from "../api/authApi";
import { AuthContext } from "./authContext";

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const logout = useCallback(() => {
    localStorage.removeItem("username");
    localStorage.removeItem(TOKEN_KEY);
    setUser(null);
  }, []);

  const restoreSession = useCallback(async () => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      setUser(null);
      setLoading(false);
      return;
    }

    try {
      const response = await apiClient.get("/auth/me");
      setUser({
    username: response.data.username,
    name: response.data.username,
    email: response.data.email,
});
    } catch (error) {
      console.error("Session restoration failed:", error);
      logout();
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, [logout]);

  const setAuthenticatedUser = useCallback((user) => {

    setUser(user);

    localStorage.setItem("username", user.username);

}, []);

  const register = useCallback(async (username, password, email) => {
    setLoading(true);
    try {
      const result = await authApi.register(username, password, email);
      setUser(result.user);
      return result;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    restoreSession();
  }, [restoreSession]);

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        restoreSession,
        setAuthenticatedUser,
        register,
        logout, }}
    >
      {children}
    </AuthContext.Provider>
  );
}
