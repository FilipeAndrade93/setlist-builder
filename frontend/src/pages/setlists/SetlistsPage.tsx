import {
  IconCheck,
  IconChevronDown,
  IconDownload,
  IconEdit,
  IconGripVertical,
  IconPlus,
  IconTrash,
  IconWand,
  IconX,
} from "@tabler/icons-react";
import type { SetlistResponse, SongResponse } from "../../types/types";
import styles from "./SetlistsPage.module.scss";
import {
  ActionIcon,
  Alert,
  Button,
  Center,
  Loader,
  Modal,
  NumberInput,
  Select,
  Stack,
  Text,
  TextInput,
} from "@mantine/core";
import { CSS } from "@dnd-kit/utilities";
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { useEffect, useState } from "react";
import { useDisclosure } from "@mantine/hooks";
import {
  closestCenter,
  DndContext,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import { setlistsApi } from "../../api/setlists.api";
import { songsApi } from "../../api/song.api";
import { notifications } from "@mantine/notifications";
import { useGenerateModal } from "../../hooks/useGenerateModal";
import { DateInput } from "@mantine/dates";

interface SortableSongRowProps {
  song: SongResponse;
  position: number;
  onPositionChange: (id: string, newPos: number) => void;
  onRemove: (id: string) => void;
  totalSongs: number;
}

const SortableSongRow = ({
  song,
  position,
  onPositionChange,
  onRemove,
  totalSongs,
}: SortableSongRowProps) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: song.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div ref={setNodeRef} style={style} className={styles.songRow}>
      <div className={styles.songRowLeft}>
        <div className={styles.dragHandle} {...attributes} {...listeners}>
          <IconGripVertical size={14} />
        </div>
        <NumberInput
          value={position}
          onChange={(v) => onPositionChange(song.id, Number(v) - 1)}
          min={1}
          max={totalSongs}
          size="xs"
          w={48}
          classNames={{ input: styles.posInput }}
        />
        <Text className={styles.songRowName}>{song.name}</Text>
      </div>
      <div className={styles.songRowRight}>
        <Text className={styles.songRowDuration}>{song.formattedDuration}</Text>
        <ActionIcon
          variant="subtle"
          color="red"
          size="sm"
          onClick={() => onRemove(song.id)}
        >
          <IconX size={14} />
        </ActionIcon>
      </div>
    </div>
  );
};

interface DraftSetlist {
  venueName: string;
  eventDate: string;
  songs: SongResponse[];
}

const SetlistsPage = () => {
  const [setlists, setSetlists] = useState<SetlistResponse[]>([]);
  const [songs, setSongs] = useState<SongResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [expandedSongs, setExpandedSongs] = useState<SongResponse[]>([]);
  const [saving, setSaving] = useState(false);
  const [editingHeader, setEditingHeader] = useState(false);
  const [editVenue, setEditVenue] = useState("");
  const [editDate, setEditDate] = useState<string | null>(null);

  const [draft, setDraft] = useState<DraftSetlist | null>(null);
  const [draftSaving, setDraftSaving] = useState(false);
  const [createOpened, { open: openCreate, close: closeCreate }] =
    useDisclosure(false);
  const [newVenue, setNewVenue] = useState("");
  const [newDate, setNewDate] = useState<string | null>(null);

  const { openGenerateModal, registerOnGenerated } = useGenerateModal();

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  );

  const load = async () => {
    try {
      const [setlistsData, songsData] = await Promise.all([
        setlistsApi.getAll(),
        songsApi.getAll(),
      ]);
      setSetlists(setlistsData);
      setSongs(songsData);
    } catch {
      setError("Failed to load setlists");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    registerOnGenerated((generated: SetlistResponse) => {
      setSetlists((prev) => [...prev, generated]);
      setDraft(null);
      setExpandedId(generated.id);
      setExpandedSongs([...generated.songs]);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleStartCreate = () => {
    setNewVenue("");
    setNewDate(null);
    openCreate();
  };

  const handleConfirmCreate = () => {
    if (!newVenue || !newDate) return;

    setDraft({
      venueName: newVenue,
      eventDate: newDate,
      songs: [],
    });

    setExpandedId(null);
    closeCreate();
  };

  const handleDraftAddSong = (songId: string | null) => {
    if (!songId || !draft) return;
    const song = songs.find((song) => song.id === songId);
    if (!song) return;
    setDraft((prev) =>
      prev ? { ...prev, songs: [...prev.songs, song] } : prev,
    );
  };

  const handleDraftRemoveSong = (songId: string) => {
    setDraft((prev) =>
      prev
        ? { ...prev, songs: prev.songs.filter((song) => song.id !== songId) }
        : prev,
    );
  };

  const handleDraftDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id || !draft) return;
    const oldIndex = draft.songs.findIndex((song) => song.id === active.id);
    const newIndex = draft.songs.findIndex((song) => song.id === over.id);
    setDraft((prev) =>
      prev
        ? { ...prev, songs: arrayMove(prev.songs, oldIndex, newIndex) }
        : prev,
    );
  };

  const handleDraftPositionChange = (songId: string, newIndex: number) => {
    if (!draft) return;
    const oldIndex = draft.songs.findIndex((song) => song.id === songId);
    const clamped = Math.max(0, Math.min(newIndex, draft.songs.length - 1));
    setDraft((prev) =>
      prev
        ? { ...prev, songs: arrayMove(prev.songs, oldIndex, clamped) }
        : prev,
    );
  };

  const handleSaveDraft = async () => {
    if (!draft || draft.songs.length === 0) {
      notifications.show({
        message: "Add at least one song before saving",
        color: "yellow",
      });
      return;
    }
    setDraftSaving(true);
    try {
      const created = await setlistsApi.create({
        venueName: draft.venueName,
        eventDate: draft.eventDate,
        songIds: draft.songs.map((song) => song.id),
      });
      setSetlists((prev) => [...prev, created]);
      setDraft(null);
      notifications.show({
        message: "Setlist created",
        color: "green",
        icon: <IconCheck size={16} />,
      });
    } catch {
      notifications.show({ message: "Failed to create setlist", color: "red" });
    } finally {
      setDraftSaving(false);
    }
  };

  const handleDiscardDraft = () => {
    setDraft(null);
  };

  const toggleExpand = (setlist: SetlistResponse) => {
    if (expandedId === setlist.id) {
      setExpandedId(null);
      setExpandedSongs([]);
      setEditingHeader(false);
    } else {
      setExpandedId(setlist.id);
      setExpandedSongs([...setlist.songs]);
      setEditingHeader(false);
      setDraft(null);
    }
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    setExpandedSongs((prev) => {
      const oldIndex = prev.findIndex((song) => song.id === active.id);
      const newIndex = prev.findIndex((song) => song.id === over.id);
      return arrayMove(prev, oldIndex, newIndex);
    });
  };

  const handlePositionChange = (songId: string, newIndex: number) => {
    setExpandedSongs((prev) => {
      const oldIndex = prev.findIndex((s) => s.id === songId);
      const clamped = Math.max(0, Math.min(newIndex, prev.length - 1));
      return arrayMove(prev, oldIndex, clamped);
    });
  };

  const handleRemoveSong = (songId: string) => {
    setExpandedSongs((prev) => prev.filter((song) => song.id !== songId));
  };

  const handleAddSong = (songId: string | null) => {
    if (!songId) return;
    const song = songs.find((song) => song.id === songId);
    if (!song) return;
    setExpandedSongs((prev) => [...prev, song]);
  };

  const handleSave = async () => {
    if (!expandedId) return;
    if (expandedSongs.length === 0) {
      notifications.show({
        message: "A setlist must have at least one song",
        color: "yellow",
      });
      return;
    }
    setSaving(true);
    try {
      const setlist = setlists.find((s) => s.id === expandedId);
      if (!setlist) {
        notifications.show({ message: "Setlist not found", color: "red" });
        return;
      }
      const updated = await setlistsApi.update(expandedId, {
        venueName: editingHeader ? editVenue : setlist.venueName,
        eventDate: editingHeader && editDate ? editDate : setlist.eventDate,
        songIds: expandedSongs.map((song) => song.id),
      });
      setSetlists((prev) =>
        prev.map((song) => (song.id === expandedId ? updated : song)),
      );
      setExpandedSongs([...updated.songs]);
      setEditingHeader(false);
      notifications.show({
        message: "Setlist saved",
        color: "green",
        icon: <IconCheck size={16} />,
      });
    } catch {
      notifications.show({ message: "Failed to save setlist", color: "red" });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await setlistsApi.delete(id);
      setSetlists((prev) => prev.filter((setlist) => setlist.id !== id));
      if (expandedId === id) {
        setExpandedId(null);
        setExpandedSongs([]);
      }
      notifications.show({
        message: "Setlist deleted",
        color: "green",
        icon: <IconCheck size={16} />,
      });
    } catch {
      notifications.show({ message: "Failed to delete setlist", color: "red" });
    }
  };

  const handleDownloadPdf = async (
    id: string,
    venueName: string,
    eventDate: string,
  ) => {
    try {
      const blob = await setlistsApi.downloadPdf(id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `bombazine_setlist_${venueName.replace(/[^a-zA-Z0-9]/g, "-").toLowerCase()}_${eventDate}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      notifications.show({ message: "Failed to download PDF", color: "red" });
    }
  };

  const startEditHeader = (setlist: SetlistResponse) => {
    setEditVenue(setlist.venueName);
    setEditDate(setlist.eventDate);
    setEditingHeader(true);
  };

  const formatDate = (dateString: string) =>
    new Date(dateString).toLocaleDateString("en-GB", {
      day: "numeric",
      month: "long",
      year: "numeric",
    });

  const calcDuration = (songList: SongResponse[]) => {
    const total = songList.reduce((acc, s) => acc + s.durationSeconds, 0);
    if (total === 0) return "—";
    const hours = Math.floor(total / 3600);
    const minutes = Math.floor((total % 3600) / 60);
    const seconds = total % 60;
    if (hours > 0) {
      return `${hours}h ${String(minutes).padStart(2, "0")}m ${String(seconds).padStart(2, "0")}s`;
    }
    return `${minutes}:${String(seconds).padStart(2, "0")}`;
  };

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
          <h1 className={styles.pageTitle}>setlists</h1>
          <Text className={styles.pageMeta}>
            {setlists.length} setlist{setlists.length !== 1 && "s"}
          </Text>
        </div>
        <div className={styles.headerActions}>
          <Button
            variant="outline"
            color="bordeaux"
            leftSection={<IconWand size={16} />}
            onClick={openGenerateModal}
            className={styles.actionButton}
          >
            generate
          </Button>
          <Button
            color="bordeaux"
            leftSection={<IconPlus size={16} />}
            onClick={handleStartCreate}
            className={styles.actionButton}
            disabled={!!draft}
          >
            new setlist
          </Button>
        </div>
      </div>
      <div className={styles.setlistList}>
        {draft && (
          <div className={styles.setlistItem}>
            <div className={styles.expandedDetail}>
              <div className={styles.detailHeader}>
                <div className={styles.detailHeaderInfo}>
                  <Text className={styles.detailVenue}>{draft.venueName}</Text>
                  <Text className={styles.detailMeta}>
                    {formatDate(draft.eventDate)}
                  </Text>
                  <span className={styles.draftBadge}>unsaved</span>
                </div>
                <div className={styles.detailActions}>
                  <Button
                    size="xs"
                    variant="subtle"
                    color="red"
                    onClick={handleDiscardDraft}
                  >
                    discard
                  </Button>
                  <Button
                    size="xs"
                    color="bordeaux"
                    loading={draftSaving}
                    onClick={handleSaveDraft}
                    disabled={draft.songs.length === 0}
                  >
                    save
                  </Button>
                </div>
              </div>
              <DndContext
                sensors={sensors}
                collisionDetection={closestCenter}
                onDragEnd={handleDraftDragEnd}
              >
                <SortableContext
                  items={draft.songs.map((song) => song.id)}
                  strategy={verticalListSortingStrategy}
                >
                  {draft.songs.map((song, index) => (
                    <SortableSongRow
                      key={song.id}
                      song={song}
                      position={index + 1}
                      onPositionChange={handleDraftPositionChange}
                      onRemove={handleDraftRemoveSong}
                      totalSongs={draft.songs.length}
                    />
                  ))}
                </SortableContext>
              </DndContext>
              <div className={styles.addSongRow}>
                <Select
                  placeholder={
                    draft.songs.length === songs.length
                      ? "all songs added"
                      : "add a song"
                  }
                  data={songs
                    .filter(
                      (song) =>
                        !draft.songs.some(
                          (draftSong) => draftSong.id === song.id,
                        ),
                    )
                    .sort((a, b) => b.popularity - a.popularity)
                    .map((s) => ({ value: s.id, label: s.name }))}
                  onChange={handleDraftAddSong}
                  value={null}
                  searchable
                  clearable
                  size="sm"
                  className={styles.addSongSelect}
                />
              </div>

              {draft.songs.length > 0 && (
                <div className={styles.detailFooter}>
                  <Text className={styles.totalDuration}>
                    total: {calcDuration(draft.songs)}
                  </Text>
                </div>
              )}
            </div>
          </div>
        )}

        {setlists.length === 0 && !draft && (
          <Text className={styles.emptyState}>no setlists</Text>
        )}

        {setlists.map((setlist) => {
          const isExpanded = expandedId === setlist.id;
          const addableSongs = songs.filter(
            (song) => !expandedSongs.some((es) => es.id === song.id),
          );

          return (
            <div key={setlist.id} className={styles.setlistItem}>
              <div
                className={styles.setlistRow}
                onClick={() => toggleExpand(setlist)}
              >
                <div className={styles.setlistRowLeft}>
                  <IconChevronDown
                    size={16}
                    className={`${styles.chevron} ${isExpanded ? styles.chevronOpen : ""}`}
                  />
                  <div>
                    <Text className={styles.setlistVenue}>
                      {setlist.venueName}
                    </Text>
                    <Text className={styles.setlistMeta}>
                      {formatDate(setlist.eventDate)} · {setlist.songs.length}{" "}
                      songs · {setlist.formattedDuration}
                    </Text>
                  </div>
                </div>
                <div
                  className={styles.setlistRowRight}
                  onClick={(e) => e.stopPropagation()}
                >
                  <ActionIcon
                    variant="subtle"
                    color="bordeaux"
                    size="sm"
                    onClick={() =>
                      handleDownloadPdf(
                        setlist.id,
                        setlist.venueName,
                        setlist.eventDate,
                      )
                    }
                  >
                    <IconDownload size={14} />
                  </ActionIcon>
                  <ActionIcon
                    variant="subtle"
                    color="red"
                    size="sm"
                    onClick={() => handleDelete(setlist.id)}
                  >
                    <IconTrash size={14} />
                  </ActionIcon>
                </div>
              </div>

              {isExpanded && (
                <div className={styles.expandedDetail}>
                  <div className={styles.detailHeader}>
                    {editingHeader ? (
                      <div className={styles.headerEditRow}>
                        <TextInput
                          value={editVenue}
                          onChange={(e) => setEditVenue(e.currentTarget.value)}
                          size="sm"
                          placeholder="venue name"
                          classNames={{ input: styles.headerInput }}
                        />
                        <DateInput
                          value={editDate}
                          onChange={setEditDate}
                          size="sm"
                          classNames={{ input: styles.headerInput }}
                        />
                        <ActionIcon
                          color="bordeaux"
                          variant="subtle"
                          size="sm"
                          onClick={() => setEditingHeader(false)}
                        >
                          <IconX size={14} />
                        </ActionIcon>
                      </div>
                    ) : (
                      <div className={styles.detailHeaderInfo}>
                        <Text className={styles.detailVenue}>
                          {setlist.venueName}
                        </Text>
                        <Text className={styles.detailMeta}>
                          {formatDate(setlist.eventDate)}
                        </Text>
                        <ActionIcon
                          variant="subtle"
                          color="bordeaux"
                          size="sm"
                          onClick={() => startEditHeader(setlist)}
                        >
                          <IconEdit size={14} />
                        </ActionIcon>
                      </div>
                    )}
                    <div className={styles.detailActions}>
                      <Button
                        size="xs"
                        color="bordeaux"
                        variant="outline"
                        leftSection={<IconDownload size={14} />}
                        onClick={() =>
                          handleDownloadPdf(
                            setlist.id,
                            setlist.venueName,
                            setlist.eventDate,
                          )
                        }
                      >
                        pdf
                      </Button>
                      <Button
                        size="xs"
                        color="bordeaux"
                        loading={saving}
                        onClick={handleSave}
                      >
                        save
                      </Button>
                    </div>
                  </div>

                  <DndContext
                    sensors={sensors}
                    collisionDetection={closestCenter}
                    onDragEnd={handleDragEnd}
                  >
                    <SortableContext
                      items={expandedSongs.map((s) => s.id)}
                      strategy={verticalListSortingStrategy}
                    >
                      {expandedSongs.map((song, index) => (
                        <SortableSongRow
                          key={song.id}
                          song={song}
                          position={index + 1}
                          onPositionChange={handlePositionChange}
                          onRemove={handleRemoveSong}
                          totalSongs={expandedSongs.length}
                        />
                      ))}
                    </SortableContext>
                  </DndContext>

                  <div className={styles.addSongRow}>
                    <Select
                      placeholder={
                        addableSongs.length === 0
                          ? "all songs added"
                          : "add a song..."
                      }
                      data={addableSongs
                        .sort((a, b) => b.popularity - a.popularity)
                        .map((song) => ({ value: song.id, label: song.name }))}
                      onChange={handleAddSong}
                      value={null}
                      searchable
                      clearable
                      size="sm"
                      className={styles.addSongSelect}
                    />
                  </div>

                  {expandedSongs.length > 0 && (
                    <div className={styles.detailFooter}>
                      <Text className={styles.totalDuration}>
                        total: {calcDuration(expandedSongs)}
                      </Text>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      <Modal
        opened={createOpened}
        onClose={closeCreate}
        title="new setlist"
        centered
        classNames={{ title: styles.modalTitle }}
      >
        <Stack gap="md">
          <TextInput
            label="Venue"
            placeholder="venue name"
            value={newVenue}
            onChange={(e) => setNewVenue(e.currentTarget.value)}
            required
          />
          <DateInput
            label="Date"
            placeholder="pick a date"
            value={newDate}
            onChange={setNewDate}
            required
          />
          <Button
            color="bordeaux"
            onClick={handleConfirmCreate}
            disabled={!newVenue || !newDate}
            fullWidth
          >
            next - add songs
          </Button>
        </Stack>
      </Modal>
    </div>
  );
};

export default SetlistsPage;
