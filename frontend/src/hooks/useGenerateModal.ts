import { useContext } from "react";
import { GenerateModalContext } from "../context/GenerateModalContext";

export const useGenerateModal = () => {
  const context = useContext(GenerateModalContext);
  if (!context)
    throw new Error("useGeneratedModal must be within GenerateModalProvider");
  return context;
};
