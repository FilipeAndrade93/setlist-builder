import { useDisclosure } from "@mantine/hooks";
import type React from "react";
import { useAuth } from "../../hooks/useAuth";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  IconHome,
  IconLogout,
  IconMusic,
  IconPlaylist,
  IconUsers,
  IconWand,
} from "@tabler/icons-react";
import styles from "./AppLayout.module.scss";
import { AppShell, Burger, Group, NavLink, Text } from "@mantine/core";
import { useGenerateModal } from "../../hooks/useGenerateModal";

interface NavItem {
  label: string;
  icon: React.ElementType;
  section: string;
  path?: string;
  onClick?: VoidFunction;
}

const AppLayout = () => {
  const [mobileOpened, { toggle: toggleMobile }] = useDisclosure();
  const { logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { openGenerateModal } = useGenerateModal();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const homeItem: NavItem = {
    label: "home",
    path: "/",
    icon: IconHome,
    section: "home",
  };

  const navItems: NavItem[] = [
    { label: "songs", path: "/songs", icon: IconMusic, section: "library" },
    {
      label: "setlists",
      path: "/setlists",
      icon: IconPlaylist,
      section: "library",
    },
    {
      label: "generate",
      icon: IconWand,
      section: "tools",
      onClick: openGenerateModal,
    },
  ];

  const adminItems: NavItem[] = [
    {
      label: "users",
      path: "/admin/users",
      icon: IconUsers,
      section: "admin",
    },
  ];

  const renderSection = (label: string, items: NavItem[]) => (
    <>
      <Text className={styles.sectionLabel}>{label}</Text>
      {items.map((item) => (
        <NavLink
          key={item.label}
          label={item.label}
          leftSection={<item.icon size={16} />}
          active={item.path ? location.pathname === item.path : false}
          onClick={() => {
            if (item.path) {
              navigate(item.path);
            } else if (item.onClick) {
              item.onClick();
            }
            if (mobileOpened) toggleMobile();
          }}
        />
      ))}
    </>
  );

  return (
    <AppShell
      header={{ height: { base: 48, sm: 0 } }}
      navbar={{
        width: 200,
        breakpoint: "sm",
        collapsed: { mobile: !mobileOpened },
      }}
      padding="md"
      classNames={{
        navbar: styles.navbar,
        header: styles.header,
        main: styles.main,
      }}
    >
      <AppShell.Header hiddenFrom="sm">
        <Group h="100%" px="md" justify="space-between">
          <Burger
            opened={mobileOpened}
            onClick={toggleMobile}
            size="sm"
            color="#f5f0e8"
          />
          <Text className={styles.headerLogo}>bombazine</Text>
          <div style={{ width: 24 }} />
        </Group>
      </AppShell.Header>

      <AppShell.Navbar>
        <div className={styles.navbarContent}>
          <div>
            <div className={styles.logoWrapper}>
              <Text className={styles.logo}>bombazine</Text>
            </div>

            <NavLink
              label={homeItem.label}
              leftSection={<homeItem.icon size={16} />}
              active={location.pathname === homeItem.path}
              onClick={() => {
                navigate(homeItem.path!);
                if (mobileOpened) toggleMobile();
              }}
            />

            {renderSection(
              "library",
              navItems.filter((item) => item.section === "library"),
            )}
            {renderSection(
              "tools",
              navItems.filter((item) => item.section === "tools"),
            )}
            {isAdmin && renderSection("admin", adminItems)}
          </div>

          <NavLink
            label="logout"
            leftSection={<IconLogout size={16} />}
            onClick={() => {
              handleLogout();
              if (mobileOpened) toggleMobile();
            }}
            className={styles.logoutItem}
          />
        </div>
      </AppShell.Navbar>

      <AppShell.Main>
        <Outlet />
      </AppShell.Main>
    </AppShell>
  );
};

export default AppLayout;
