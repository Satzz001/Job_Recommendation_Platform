import { createContext, useContext, useMemo, useState } from "react";
import { Auth } from "../lib/api.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => Auth.user());

  const value = useMemo(
    () => ({
      user,
      login: (nextUser) => setUser(nextUser),
      logout: () => {
        Auth.clear();
        setUser(null);
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth must be used within AuthProvider");
  return value;
}
