import { createTheme, NavLink, type MantineColorsTuple } from "@mantine/core";
import navLinkClasses from "./NavLink.module.scss";

const bordeaux: MantineColorsTuple = [
  "#f9ecee",
  "#f0d0d4",
  "#e0a0a8",
  "#cf6d78",
  "#c04150",
  "#b82738",
  "#b21a2c",
  "#8C1C2C",
  "#7a1526",
  "#690c1f",
];

const gold: MantineColorsTuple = [
  "#fdf8e1",
  "#faefc3",
  "#f5de88",
  "#f0cc4a",
  "#E8C84A",
  "#e4bc1e",
  "#e3b80e",
  "#c9a100",
  "#b38e00",
  "#9b7900",
];

export const theme = createTheme({
  primaryColor: "bordeaux",
  colors: { bordeaux, gold },

  black: "#1a1a1a",
  white: "#f5f0e8",

  fontFamily: "'Inter', system-ui, sans-serif",
  fontFamilyMonospace: "'Inter', monospace",
  headings: {
    fontFamily: "'Playfair Display', Georgia, serif",
    fontWeight: "400",
  },

  defaultRadius: "sm",

  components: {
    NavLink: NavLink.extend({
      classNames: navLinkClasses,
    }),
  },
});
