import { useEffect, useState } from "react";
import type { UserResponse } from "../../types/types";
import { useDisclosure } from "@mantine/hooks";
import { usersApi } from "../../api/users.api";
import { notifications } from "@mantine/notifications";
import { IconCheck, IconKey, IconPlus, IconTrash } from "@tabler/icons-react";
import {
  ActionIcon,
  Alert,
  Badge,
  Button,
  Center,
  Loader,
  Modal,
  PasswordInput,
  Stack,
  Text,
  TextInput,
} from "@mantine/core";
import styles from "./UsersPage.module.scss";

const UsersPage = () => {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [resetOpened, { open: openReset, close: closeReset }] = useDisclosure();
  const [resetUser, setResetUser] = useState<UserResponse | null>(null);
  const [newPassword, setNewPassword] = useState("");
  const [resetLoading, setResetLoading] = useState(false);

  const [createOpened, { open: openCreate, close: closeCreate }] =
    useDisclosure();
  const [newUsername, setNewUsername] = useState("");
  const [createPassword, setCreatePassowrd] = useState("");
  const [createLoading, setCreateLoading] = useState(false);

  const load = async () => {
    try {
      const data = await usersApi.getAll();
      setUsers(data);
    } catch {
      setError("Failed to load users");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleOpenReset = (user: UserResponse) => {
    setResetUser(user);
    setNewPassword("");
    openReset();
  };

  const handleResetPassword = async () => {
    if (!resetUser) return;
    setResetLoading(true);
    try {
      await usersApi.resetPassword(resetUser.id, { newPassword });
      closeReset();
      notifications.show({
        message: `Password updated for ${resetUser.username}`,
        color: "green",
        icon: <IconCheck size={16} />,
      });
    } catch {
      notifications.show({ message: "Failed to reset password", color: "red" });
    } finally {
      setResetLoading(false);
    }
  };

  const handleDelete = async (user: UserResponse) => {
    setCreateLoading(true);
    try {
      await usersApi.delete(user.id);
      setUsers((prev) => prev.filter((user) => user.id !== user.id));
      notifications.show({
        message: `${user.username} deleted`,
        color: "green",
        icon: <IconCheck size={16} />,
      });
    } catch {
      notifications.show({ message: "Failed to delete user", color: "red" });
    }
  };

  const handleCreate = async () => {
    setCreateLoading(true);
    try {
      await usersApi.create({
        username: newUsername,
        password: createPassword,
      });
      closeCreate();
      setNewUsername("");
      setCreatePassowrd("");
      await load();
      notifications.show({
        message: `${newUsername} created`,
        color: "green",
        icon: <IconCheck size={16} />,
      });
    } catch {
      notifications.show({ message: "Failed to create user", color: "red" });
    } finally {
      setCreateLoading(false);
    }
  };

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString("en-GB", {
      day: "numeric",
      month: "long",
      year: "numeric",
    });

  if (loading)
    return (
      <Center h="60vh">
        <Loader color="bordeaux" />
      </Center>
    );
  if (error)
    return (
      <Alert color="red" variant="light" m="md">
        {error}
      </Alert>
    );

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <h1 className={styles.pageTitle}>users</h1>
          <Text className={styles.pageMeta}>
            {users.length} user{users.length !== 1 && "s"}
          </Text>
        </div>
        <Button
          color="bordeaux"
          leftSection={<IconPlus size={16} />}
          onClick={openCreate}
          className={styles.actionButton}
        >
          add user
        </Button>
      </div>

      <div className={styles.tableWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th className={styles.th}>username</th>
              <th className={styles.th}>role</th>
              <th className={`${styles.th} ${styles.hiddenSm}`}>created</th>
              <th className={styles.th}></th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className={styles.row}>
                <td className={styles.td}>
                  <Text className={styles.username}>{user.username}</Text>
                </td>
                <td className={styles.td}>
                  <Badge
                    size="xs"
                    variant="light"
                    color={user.role === "ADMIN" ? "bordeaux" : "gray"}
                    className={styles.badge}
                  >
                    {user.role.toLocaleLowerCase()}
                  </Badge>
                </td>
                <td className={`${styles.td} ${styles.hiddenSm}`}>
                  <Text className={styles.createdAt}>
                    {formatDate(user.createdAt)}
                  </Text>
                </td>
                <td className={styles.td}>
                  <ActionIcon
                    variant="subtle"
                    color="bordeaux"
                    size="sm"
                    onClick={() => handleOpenReset(user)}
                  >
                    <IconKey size={14} />
                  </ActionIcon>
                  <ActionIcon
                    variant="subtle"
                    color="bordeaux"
                    size="sm"
                    onClick={() => handleDelete(user)}
                  >
                    <IconTrash size={14} />
                  </ActionIcon>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        opened={resetOpened}
        onClose={closeReset}
        title={`reset password — ${resetUser?.username}`}
        centered
        classNames={{ title: styles.modalTitle }}
      >
        <Stack gap="md">
          <PasswordInput
            label="New Password"
            placeholder="min. 8 characters"
            value={newPassword}
            onChange={(e) => setNewPassword(e.currentTarget.value)}
          />
          <Button
            color="bordeaux"
            loading={resetLoading}
            onClick={handleResetPassword}
            disabled={newPassword.length < 8}
            fullWidth
          >
            update password
          </Button>
        </Stack>
      </Modal>

      <Modal
        opened={createOpened}
        onClose={closeCreate}
        title="add user"
        centered
        classNames={{ title: styles.modalTitle }}
      >
        <Stack gap="md">
          <TextInput
            label="Username"
            placeholder="username"
            value={newUsername}
            onChange={(e) => setNewUsername(e.currentTarget.value)}
            required
          />
          <PasswordInput
            label="Password"
            placeholder="min. 8 characters"
            value={createPassword}
            onChange={(e) => setCreatePassowrd(e.currentTarget.value)}
            required
          />
          <Button
            color="bordeaux"
            loading={createLoading}
            onClick={handleCreate}
            disabled={!newUsername || createPassword.length < 8}
            fullWidth
          >
            create user
          </Button>
        </Stack>
      </Modal>
    </div>
  );
};

export default UsersPage;
