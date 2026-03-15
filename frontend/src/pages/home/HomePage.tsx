import { useEffect, useState } from "react";
import styles from "./HomePage.module.scss";
import type { SetlistResponse, SongResponse } from "../../types/types";
import { songsApi } from "../../api/song.api";
import { setlistsApi } from "../../api/setlists.api";
import { Alert, Center, Loader, SimpleGrid, Text } from "@mantine/core";
import { IconAlertTriangle } from "@tabler/icons-react";

const HomePage = () => {
  const [songs, setSongs] = useState<SongResponse[]>([]);
  const [setlists, setSetlists] = useState<SetlistResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        const [songsData, setlistData] = await Promise.all([
          songsApi.getAll(),
          setlistsApi.getAll(),
        ]);
        setSongs(songsData);
        setSetlists(setlistData);
      } catch {
        setError("Failed to load dashboard data");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

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

  const missingDurationSongs = songs.filter(
    (song) => song.durationSeconds === 0,
  );
  const topSongs = [...songs]
    .sort((a, b) => b.popularity - a.popularity)
    .slice(0, 10);

  const upcomingSetlist =
    setlists
      .filter((song) => new Date(song.eventDate) >= new Date())
      .sort(
        (a, b) =>
          new Date(a.eventDate).getTime() - new Date(b.eventDate).getTime(),
      )[0] ?? null;

  const formatDate = (dateString: string) =>
    new Date(dateString).toLocaleDateString("en-GB", {
      day: "numeric",
      month: "long",
      year: "numeric",
    });

  return (
    <div className={styles.container}>
      <h1 className={styles.pageTitle}>dashboard</h1>

      {missingDurationSongs.length > 0 && (
        <Alert
          color="yellow"
          variant="light"
          icon={<IconAlertTriangle size={16} />}
          mb="xl"
          className={styles.alert}
        >
          {missingDurationSongs.length} song
          {missingDurationSongs.length > 1 ? "s" : ""} missing duration:{" "}
          {missingDurationSongs.map((song) => song.name).join(", ")}
        </Alert>
      )}

      <SimpleGrid cols={{ base: 1, sm: 2, md: 4 }} mb="xl">
        <div className={styles.statCard}>
          <Text className={styles.statLabel}>total songs</Text>
          <Text className={styles.statValue}>{songs.length}</Text>
        </div>

        <div className={styles.statCard}>
          <Text className={styles.statLabel}>missing duration</Text>
          <Text
            className={`${styles.statValue} ${missingDurationSongs.length > 0 ? styles.statWarning : ""}`}
          >
            {missingDurationSongs.length}
          </Text>
        </div>

        <div className={styles.statCard}>
          <Text className={styles.statLabel}>total setlists</Text>
          <Text className={styles.statValue}>{setlists.length}</Text>
        </div>

        <div className={styles.statCard}>
          <Text className={styles.statLabel}>next gig</Text>
          <Text className={styles.statValue}>
            {upcomingSetlist ? upcomingSetlist.venueName : "-"}
          </Text>
          {upcomingSetlist && (
            <Text className={styles.statSub}>
              {formatDate(upcomingSetlist.eventDate)}
            </Text>
          )}
        </div>
      </SimpleGrid>

      <div className={styles.grid}>
        <div className={styles.section}>
          <Text className={styles.sectionTitle}>
            top songs (% total streams)
          </Text>
          <div className={styles.topSongs}>
            {topSongs.map((song, i) => (
              <div key={song.id} className={styles.topSongRow}>
                <Text className={styles.topSongRank}>#{i + 1}</Text>
                <div className={styles.topSongInfo}>
                  <Text className={styles.topSongName}>{song.name}</Text>
                  <div className={styles.popularityBar}>
                    <div
                      className={styles.popularityFill}
                      style={{ width: `${song.popularity}%` }}
                    />
                  </div>
                </div>
                <Text className={styles.topSongPop}>{song.popularity}%</Text>
              </div>
            ))}
          </div>
        </div>

        <div className={styles.section}>
          <Text className={styles.sectionTitle}>upcoming setlist</Text>
          {upcomingSetlist ? (
            <>
              <div className={styles.setlistPreview}>
                <div className={styles.setlistHeader}>
                  <Text className={styles.setlistVenue}>
                    {upcomingSetlist.venueName}
                  </Text>
                  <Text className={styles.setlistMeta}>
                    {formatDate(upcomingSetlist.eventDate)} ·{" "}
                    {upcomingSetlist.formattedDuration}
                  </Text>
                </div>
                <div className={styles.setlistSongs}>
                  {upcomingSetlist.songs.map((song, i) => (
                    <div key={song.id} className={styles.setlistSongRow}>
                      <Text className={styles.setlistSongPos}>{i + 1}</Text>
                      <Text className={styles.setlistSongName}>
                        {song.name}
                      </Text>
                      <Text className={styles.setlistSongDuration}>
                        {song.formattedDuration}
                      </Text>
                    </div>
                  ))}
                </div>
              </div>
            </>
          ) : (
            <>
              <Text className={styles.emptyState}>
                no upcoming gig scheduled
              </Text>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default HomePage;
