import { useRef, useState, type ReactNode } from "react";
import type { SetlistResponse } from "../types/types";
import { useDisclosure } from "@mantine/hooks";
import { setlistsApi } from "../api/setlists.api";
import { notifications } from "@mantine/notifications";
import { IconCheck } from "@tabler/icons-react";
import {
  Button,
  Modal,
  NumberInput,
  Stack,
  Text,
  TextInput,
} from "@mantine/core";
import styles from "./GenerateModalContext.module.scss";
import { DateInput } from "@mantine/dates";
import { GenerateModalContext } from "./GenerateModalContext";
import { useNavigate } from "react-router-dom";

export const GenerateModalProvider = ({
  children,
}: {
  children: ReactNode;
}) => {
  const [opened, { open, close }] = useDisclosure(false);
  const [venue, setVenue] = useState("");
  const [date, setDate] = useState<string | null>(null);
  const [targetDuration, setTargetDuration] = useState<number>(0);
  const [loading, setLoading] = useState(false);
  const onGeneratedRef = useRef<((setlist: SetlistResponse) => void) | null>(
    null,
  );
  const navigate = useNavigate();

  const openGenerateModal = () => {
    setVenue("");
    setDate(null);
    setTargetDuration(0);
    open();
  };

  const registerOnGenerated = (cb: (setlist: SetlistResponse) => void) => {
    console.log("registerOnGenerated called");
    onGeneratedRef.current = cb;
  };

  const handleGenerate = async () => {
    if (!venue || !date || targetDuration === 0) return;
    setLoading(true);
    try {
      const result = await setlistsApi.generate({
        venueName: venue,
        eventDate: date,
        targetDurationSeconds: targetDuration,
      });
      close();
      notifications.show({
        message: "Setlist generated",
        color: "green",
        icon: <IconCheck size={16} />,
      });
      console.log("onGeneratedRef.current:", onGeneratedRef.current);
      navigate("/setlists");
      setTimeout(() => onGeneratedRef.current?.(result), 100);
    } catch {
      notifications.show({
        message: "Failed to generate setlist",
        color: "red",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <GenerateModalContext.Provider
      value={{ openGenerateModal, registerOnGenerated }}
    >
      {children}
      <Modal
        opened={opened}
        onClose={close}
        title="generate setlist"
        centered
        classNames={{ title: styles.modalTitle }}
      >
        <Stack gap="md">
          <TextInput
            label="Venue"
            placeholder="venue name"
            value={venue}
            onChange={(e) => setVenue(e.currentTarget.value)}
            required
          />
          <DateInput
            label="Date"
            placeholder="pick a date"
            value={date}
            onChange={setDate}
            required
          />
          <div>
            <Text size="sm" fw={500} mb={4}>
              Target duration
            </Text>
            <div className={styles.durationInputs}>
              <NumberInput
                description="hours"
                value={Math.floor(targetDuration / 3600)}
                onChange={(v) =>
                  setTargetDuration(Number(v) * 3600 + (targetDuration % 3600))
                }
                min={0}
                max={9}
                w={80}
              />
              <Text c="bordeaux" className={styles.durationSeparator}>
                :
              </Text>
              <NumberInput
                description="min"
                value={Math.floor((targetDuration % 3600) / 60)}
                onChange={(v) =>
                  setTargetDuration(
                    Math.floor(targetDuration / 3600) * 3600 + Number(v) * 60,
                  )
                }
                min={0}
                max={59}
                w={80}
              />
            </div>
          </div>
          <Button
            color="bordeaux"
            loading={loading}
            onClick={handleGenerate}
            disabled={!venue || !date || targetDuration === 0}
            fullWidth
          >
            generate
          </Button>
        </Stack>
      </Modal>
    </GenerateModalContext.Provider>
  );
};
