import React, { useState } from "react";
import { useAuth } from "../../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import styles from "./LoginPage.module.scss";
import {
  Alert,
  Button,
  PasswordInput,
  Stack,
  Text,
  TextInput,
} from "@mantine/core";

const LoginPage = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSumbit = async (e: React.SubmitEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login({ username, password });
      navigate("/");
    } catch {
      setError("Invalid username or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.wrapper}>
        <div className={styles.header}>
          <h1 className={styles.title}>bombazine</h1>
          <Text size="sm" c="dimmed" className={styles.subtitle}>
            setlist builder
          </Text>
        </div>

        <form onSubmit={handleSumbit}>
          <Stack gap="md">
            {error && (
              <Alert color="red" variant="light">
                {error}
              </Alert>
            )}
          </Stack>

          <TextInput
            label="Username"
            placeholder="username"
            value={username}
            onChange={(e) => setUsername(e.currentTarget.value)}
            required
            mb="sm"
            classNames={{ label: styles.label, input: styles.input }}
          />

          <PasswordInput
            label="Password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.currentTarget.value)}
            required
            classNames={{ label: styles.label, input: styles.input }}
          />

          <Button
            type="submit"
            loading={loading}
            fullWidth
            mt="sm"
            className={styles.submitButton}
          >
            Sign in
          </Button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
