import React, { useState } from "react";

export default function LoginCard({ onLoginAttempt, onNavigateToRegister }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    onLoginAttempt({
      username: username.trim(),
      password: password,
    });
  };

  return (
    <div className="auth-card">
      <h3>Account Access Panel</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Username</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="form-input"
            placeholder="Username"
          />
        </div>
        <div className="form-group">
          <label className="form-label">Security Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="form-input"
            placeholder="P@SSW0RD!"
          />
        </div>
        <button type="submit" className="primary-btn">
          Authenticate Credentials
        </button>
      </form>

      <div className="card-footer-prompt">
        <p>New around here?</p>
        <button onClick={onNavigateToRegister} className="secondary-btn">
          ➕ Create User Account
        </button>
      </div>
    </div>
  );
}
