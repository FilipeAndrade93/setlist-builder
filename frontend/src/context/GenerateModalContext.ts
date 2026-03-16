import { createContext } from "react";
import type { SetlistResponse } from "../types/types";

interface GenerateModalContextType {
  openGenerateModal: VoidFunction;
  registerOnGenerated: (cb: (setlist: SetlistResponse) => void) => void;
}

export const GenerateModalContext =
  createContext<GenerateModalContextType | null>(null);
