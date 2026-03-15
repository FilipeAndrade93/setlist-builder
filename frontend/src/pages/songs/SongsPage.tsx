import { useEffect, useState } from "react";
import styles from "./SongsPage.module.scss";
import type { SongResponse, CreateSongRequest } from "../../types/types";
import { useDisclosure } from "@mantine/hooks";
import { songsApi } from "../../api/song.api";
import { notifications } from "@mantine/notifications";
import {
  IconAlertTriangle,
  IconCheck,
  IconCloudDownload,
  IconEdit,
  IconPlus,
  IconTrash,
  IconX,
} from "@tabler/icons-react";
import {
  ActionIcon,
  Alert,
  Badge,
  Button,
  Center,
  Loader,
  Modal,
  NumberInput,
  Select,
  Stack,
  Text,
  TextInput,
  Tooltip,
} from "@mantine/core";

const SOURCE_LABELS: Record<string, string> = {
  LASTFM: "last.fm",
  SPOTIFY: "spotify",
  MANUAL: "manual",
  ARRANGEMENT: "arrangement",
};

const SongsPage = () => {
  const [songs, setSongs] = useState<SongResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [editDuration, setEditDuration] = useState<number>(0);
  const [savingId, setSavingId] = useState<string | null>(null);
  const [importLoading, setImportLoading] = useState(false);
  const [addOpened, { open: openAdd, close: closeAdd }] = useDisclosure(false);
  const [newName, setNewName] = useState("");
  const [newDuration, setNewDuration] = useState<number>(0);
  const [newOriginalId, setNewOriginalId] = useState<string | null>(null);
  const [addLoading, setAddLoading] = useState(false);

  const load = async () => {
    try {
      const data = await songsApi.getAll();
      setSongs(data);
    } catch {
      setError("Failed to load songs");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const startEdit = (song: SongResponse) => {
    setEditingId(song.id);
    setEditName(song.name);
    setEditDuration(song.durationSeconds);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditName("");
    setEditDuration(0);
  };

  const saveEdit = async (id: string) => {
    setSavingId(id);
    try {
      const updated = await songsApi.update(id, {
        name: editName,
        durationSeconds: editDuration,
      });
      setSongs((prev) => prev.map((song) => (song.id === id ? updated : song)));
      setEditingId(null);
      notifications.show({
        message: "Song updated",
        color: "green",
        icon: <IconCheck size={16} />,
      });
    } catch {
      notifications.show({ message: "Failed to update song", color: "red" });
    } finally {
      setSavingId(null);
    }
  };

  const deleteSong = async (id: string) => {
    try {
      await songsApi.delete(id);
      setSongs((prev) => prev.filter((song) => song.id !== id));
      notifications.show({
        message: "Song deleted",
        color: "green",
        icon: <IconCheck size={16} />,
      });
    } catch {
      notifications.show({ message: "Failed to delete song", color: "red" });
    }
  };

  const handleImport = async () => {
    setImportLoading(true);
    try {
      const result = await songsApi.importFromLastFm();
      notifications.show({
        message: `Imported ${result.imported} songs, skipped ${result.skipped}`,
        color: "green",
        icon: <IconCheck size={16} />,
      });
      await load();
    } catch {
      notifications.show({ message: "Import failed", color: "red" });
    } finally {
      setImportLoading(false);
    }
  };

  const handleAdd = async () => {
    setAddLoading(true);
    try {
      const request: CreateSongRequest = {
        name: newName,
        durationSeconds: newDuration,
        originalSongId: newOriginalId ?? undefined,
      };
      const created = await songsApi.create(request);
      setSongs((prev) => [...prev, created]);
      closeAdd();
      setNewName("");
      setNewDuration(0);
      setNewOriginalId(null);
      notifications.show({
        message: "Song added",
        color: "green",
        icon: <IconCheck size={16} />,
      });
    } catch {
      notifications.show({ message: "Failed to add song", color: "red" });
    } finally {
      setAddLoading(false);
    }
  };

  const songOptions = songs
    .filter((song) => song.source !== "ARRANGEMENT")
    .map((song) => ({ value: song.id, label: song.name }));

  const durationInputs = (
    duration: number,
    onChange: (seconds: number) => void,
  ) => (
    <div className={styles.durationInputs}>
      <NumberInput
        value={Math.floor(duration / 60)}
        onChange={(v) => onChange(Number(v) * 60 + (duration % 60))}
        min={0}
        max={99}
        size="xs"
        w={52}
        classNames={{ input: styles.editInput }}
      />
      <Text size="xs" c="bordeaux" className={styles.durationSeparator}>
        :
      </Text>
      <NumberInput
        value={duration % 60}
        onChange={(v) => onChange(Math.floor(duration / 60) * 60 + Number(v))}
        min={0}
        max={59}
        size="xs"
        w={52}
        classNames={{ input: styles.editInput }}
      />
    </div>
  );

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
          <h1 className={styles.pageTitle}>songs</h1>
          <Text className={styles.pageMeta}>{songs.length} tracks</Text>
        </div>
        <div className={styles.actions}>
          <Button
            variant="outline"
            color="bordeaux"
            leftSection={<IconCloudDownload size={16} />}
            loading={importLoading}
            onClick={handleImport}
            className={styles.actionButton}
          >
            import from last.fm
          </Button>
          <Button
            color="bordeaux"
            leftSection={<IconPlus size={16} />}
            onClick={openAdd}
            className={styles.actionButton}
          >
            add song
          </Button>
        </div>
      </div>

      <div className={styles.tableWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th className={styles.th}>title</th>
              <th className={styles.th}>duration</th>
              <th className={`${styles.th} ${styles.hiddenMd}`}>popularity</th>
              <th className={`${styles.th} ${styles.hiddenMd}`}>source</th>
              <th className={styles.th} />
            </tr>
          </thead>
          <tbody>
            {songs.map((song) => (
              <tr key={song.id} className={styles.row}>
                {editingId === song.id ? (
                  <>
                    <td className={styles.td}>
                      <TextInput
                        value={editName}
                        onChange={(e) => setEditName(e.currentTarget.value)}
                        size="xs"
                        classNames={{ input: styles.editInput }}
                      />
                    </td>
                    <td className={styles.td}>
                      {durationInputs(editDuration, setEditDuration)}
                    </td>
                    <td className={`${styles.td} ${styles.hiddenMd}`} />
                    <td className={`${styles.td} ${styles.hiddenMd}`} />
                    <td className={styles.td}>
                      <div className={styles.rowActions}>
                        <ActionIcon
                          color="bordeaux"
                          variant="filled"
                          size="sm"
                          loading={savingId === song.id}
                          onClick={() => saveEdit(song.id)}
                        >
                          <IconCheck size={14} />
                        </ActionIcon>
                        <ActionIcon
                          color="bordeaux"
                          variant="outline"
                          size="sm"
                          onClick={cancelEdit}
                        >
                          <IconX size={14} />
                        </ActionIcon>
                      </div>
                    </td>
                  </>
                ) : (
                  <>
                    <td className={styles.td}>
                      <div className={styles.songName}>
                        {song.name}
                        {song.durationSeconds === 0 && (
                          <Tooltip label="missing duration" position="right">
                            <IconAlertTriangle
                              size={14}
                              className={styles.warnIcon}
                            />
                          </Tooltip>
                        )}
                      </div>
                    </td>
                    <td className={styles.td}>
                      <Text className={styles.duration}>
                        {song.formattedDuration}
                      </Text>
                    </td>
                    <td className={`${styles.td} ${styles.hiddenMd}`}>
                      <div className={styles.popularityCell}>
                        <div className={styles.popularityBar}>
                          <div
                            className={styles.popularityFill}
                            style={{ width: `${song.popularity}%` }}
                          />
                        </div>
                        <Text className={styles.popularityValue}>
                          {song.popularity}%
                        </Text>
                      </div>
                    </td>
                    <td className={`${styles.td} ${styles.hiddenMd}`}>
                      <Badge
                        size="xs"
                        variant="light"
                        color="bordeaux"
                        className={styles.badge}
                      >
                        {SOURCE_LABELS[song.source] ??
                          song.source.toLowerCase()}
                      </Badge>
                    </td>
                    <td className={styles.td}>
                      <div className={styles.rowActions}>
                        <ActionIcon
                          variant="subtle"
                          color="bordeaux"
                          size="sm"
                          onClick={() => startEdit(song)}
                        >
                          <IconEdit size={14} />
                        </ActionIcon>
                        <ActionIcon
                          variant="subtle"
                          color="red"
                          size="sm"
                          onClick={() => deleteSong(song.id)}
                        >
                          <IconTrash size={14} />
                        </ActionIcon>
                      </div>
                    </td>
                  </>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* ── Card layout (below sm) ── */}
      <div className={styles.cardList}>
        {songs.map((song) => (
          <div key={song.id} className={styles.card}>
            {editingId === song.id ? (
              <div className={styles.cardEditArea}>
                <TextInput
                  value={editName}
                  onChange={(e) => setEditName(e.currentTarget.value)}
                  size="xs"
                  classNames={{ input: styles.editInput }}
                />
                {durationInputs(editDuration, setEditDuration)}
                <div className={styles.cardEditRow}>
                  <ActionIcon
                    color="bordeaux"
                    variant="filled"
                    size="sm"
                    loading={savingId === song.id}
                    onClick={() => saveEdit(song.id)}
                  >
                    <IconCheck size={14} />
                  </ActionIcon>
                  <ActionIcon
                    color="bordeaux"
                    variant="outline"
                    size="sm"
                    onClick={cancelEdit}
                  >
                    <IconX size={14} />
                  </ActionIcon>
                </div>
              </div>
            ) : (
              <>
                <div className={styles.cardHeader}>
                  <div className={styles.cardName}>
                    {song.name}
                    {song.durationSeconds === 0 && (
                      <Tooltip label="missing duration" position="right">
                        <IconAlertTriangle
                          size={14}
                          className={styles.warnIcon}
                        />
                      </Tooltip>
                    )}
                  </div>
                  <div className={styles.rowActions}>
                    <ActionIcon
                      variant="subtle"
                      color="bordeaux"
                      size="sm"
                      onClick={() => startEdit(song)}
                    >
                      <IconEdit size={14} />
                    </ActionIcon>
                    <ActionIcon
                      variant="subtle"
                      color="red"
                      size="sm"
                      onClick={() => deleteSong(song.id)}
                    >
                      <IconTrash size={14} />
                    </ActionIcon>
                  </div>
                </div>
                <div className={styles.cardMeta}>
                  <Text className={styles.cardDuration}>
                    {song.formattedDuration}
                  </Text>
                  <div className={styles.cardPopularity}>
                    <div className={styles.popularityBar}>
                      <div
                        className={styles.popularityFill}
                        style={{ width: `${song.popularity}%` }}
                      />
                    </div>
                    <Text className={styles.popularityValue}>
                      {song.popularity}%
                    </Text>
                  </div>
                  <Badge size="xs" variant="light" color="bordeaux">
                    {SOURCE_LABELS[song.source] ?? song.source.toLowerCase()}
                  </Badge>
                </div>
              </>
            )}
          </div>
        ))}
      </div>

      <Modal
        opened={addOpened}
        onClose={closeAdd}
        title="add song"
        centered
        classNames={{ title: styles.modalTitle }}
      >
        <Stack gap="md">
          <TextInput
            label="Name"
            placeholder="song name"
            value={newName}
            onChange={(e) => setNewName(e.currentTarget.value)}
            required
          />
          <div>
            <Text size="sm" fw={500} mb={4}>
              Duration
            </Text>
            <div className={styles.durationInputs}>
              <NumberInput
                description="min"
                value={Math.floor(newDuration / 60)}
                onChange={(v) =>
                  setNewDuration(Number(v) * 60 + (newDuration % 60))
                }
                min={0}
                max={99}
                w={80}
              />
              <Text c="bordeaux" className={styles.durationSeparator}>
                :
              </Text>
              <NumberInput
                description="sec"
                value={newDuration % 60}
                onChange={(v) =>
                  setNewDuration(Math.floor(newDuration / 60) * 60 + Number(v))
                }
                min={0}
                max={59}
                w={80}
              />
            </div>
          </div>
          <Select
            label="Arrangement of"
            placeholder="select original song (optional)"
            data={songOptions}
            value={newOriginalId}
            onChange={setNewOriginalId}
            clearable
            searchable
          />
          <Button
            color="bordeaux"
            loading={addLoading}
            onClick={handleAdd}
            disabled={!newName}
            fullWidth
          >
            add song
          </Button>
        </Stack>
      </Modal>
    </div>
  );
};

export default SongsPage;
