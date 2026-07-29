import { useAuthContext } from "../context/authContext";

export function useAuth() {
  // Simply return everything the Context Provider exposes!
  return useAuthContext();
}
