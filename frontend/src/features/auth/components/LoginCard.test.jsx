import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import LoginCard from "./LoginCard";

describe("LoginCard", () => {
  it("submits the trimmed username and entered password", () => {
    const onLoginAttempt = vi.fn();

    render(
      <LoginCard
        onLoginAttempt={onLoginAttempt}
        onNavigateToRegister={vi.fn()}
      />,
    );

    fireEvent.change(screen.getByPlaceholderText("Username"), {
      target: { value: "  gage  " },
    });
    fireEvent.change(screen.getByPlaceholderText("P@SSW0RD!"), {
      target: { value: "SecurePassword123!" },
    });
    fireEvent.click(
      screen.getByRole("button", { name: "Authenticate Credentials" }),
    );

    expect(onLoginAttempt).toHaveBeenCalledWith({
      username: "gage",
      password: "SecurePassword123!",
    });
  });

  it("opens registration when the create-account button is clicked", () => {
    const onNavigateToRegister = vi.fn();

    render(
      <LoginCard
        onLoginAttempt={vi.fn()}
        onNavigateToRegister={onNavigateToRegister}
      />,
    );

    fireEvent.click(
      screen.getByRole("button", { name: /Create User Account/i }),
    );

    expect(onNavigateToRegister).toHaveBeenCalledOnce();
  });
});
