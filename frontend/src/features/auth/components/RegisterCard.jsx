import React, { useState } from "react";

export default function RegisterCard({ onRegisterSubmit, onCancel }) {
  const [regForm, setRegForm] = useState({
    name: "",
    username: "",
    password: "",
    securityQuestion: "",
  });
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const sanitizeInput = (text) => {
    return text.replace(/[<>]/g, "").replace(/['";-]/g, "");
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    const cleanValue = name === "password" ? value : sanitizeInput(value);
    setRegForm((prev) => ({ ...prev, [name]: cleanValue }));
  };

  const validatePasswordStrength = (pass) => {
    const strongPasswordRegex = /^(?=(?:.*[A-Z]){2,})(?=(?:.*\d){2,}).{8,}$/;
    return strongPasswordRegex.test(pass);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    const normalizedForm = {
      ...regForm,
      name: regForm.name.trim(),
      username: regForm.username.trim(),
      securityQuestion: regForm.securityQuestion.trim(),
    };

    if (
      !normalizedForm.name ||
      !normalizedForm.username ||
      !normalizedForm.password ||
      !normalizedForm.securityQuestion
    ) {
      setErrorMessage("All profile fields are mandatory.");
      return;
    }

    if (!validatePasswordStrength(regForm.password)) {
      setErrorMessage(
        "Password criteria unmet! Must be at least 8 characters long, contain at least 2 uppercase letters, and at least 2 numeric digits.",
      );
      return;
    }

    const success = await onRegisterSubmit(normalizedForm);

    if (success) {
      setSuccessMessage("Secure account created! Auto-loading login screen...");
      setRegForm({
        name: "",
        username: "",
        password: "",
        securityQuestion: "",
      });

      setTimeout(() => {
        setSuccessMessage("");
        onCancel();
      }, 3000);
    } else {
      setErrorMessage("This username is already taken. Please try another.");
    }
  };

  return (
    <div className="auth-card">
      <h3>Create Secure Portfolio Account</h3>
      <p className="card-subtext">Initialize account security rules below.</p>

      {errorMessage && (
        <p className="alert-error form-alert">
          ⚠️ {errorMessage}
        </p>
      )}
      {successMessage && <p className="alert-success">✅ {successMessage}</p>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Full Name</label>
          <input
            type="text"
            name="name"
            value={regForm.name}
            onChange={handleChange}
            className="form-input"
            placeholder="John Doe"
          />
        </div>

        <div className="form-group">
          <label className="form-label">Target Username</label>
          <input
            type="text"
            name="username"
            value={regForm.username}
            onChange={handleChange}
            className="form-input"
            placeholder="Choose a Username..."
          />
        </div>

        <div className="form-group">
          <label className="form-label">Security Password</label>
          <input
            type="password"
            name="password"
            value={regForm.password}
            onChange={handleChange}
            className="form-input"
            placeholder="Create strong password..."
          />
          <small className="field-help">
            Requirements: Min 8 characters, 2 uppercase letters, and 2 numbers.
          </small>
        </div>

        <div className="form-group">
          <label className="form-label">Security Answer Reminder</label>
          <input
            type="text"
            name="securityQuestion"
            value={regForm.securityQuestion}
            onChange={handleChange}
            className="form-input"
          />
        </div>

        <div className="btn-flex-container">
          <button type="submit" className="primary-btn blue-variant">
            Establish Entry
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="secondary-btn flex-variant"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
