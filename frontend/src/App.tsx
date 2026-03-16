import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/login/LoginPage";
import AppLayout from "./components/layout/AppLayout";
import HomePage from "./pages/home/HomePage";
import SongsPage from "./pages/songs/SongsPage";
import SetlistsPage from "./pages/setlists/SetlistsPage";
import { GenerateModalProvider } from "./context/GenerateModalProvider";
import UsersPage from "./pages/users/UsersPage";

const RequireAuth = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem("token");
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
};

const RequireAdmin = ({ children }: { children: React.ReactNode }) => {
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  if (user.role !== "ADMIN") return <Navigate to="/" replace />;
  return <>{children}</>;
};

const App = () => {
  return (
    <GenerateModalProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          element={
            <RequireAuth>
              <AppLayout />
            </RequireAuth>
          }
        >
          <Route path="/" element={<HomePage />} />
          <Route path="/songs" element={<SongsPage />} />
          <Route path="/setlists" element={<SetlistsPage />} />
          <Route
            path="/admin/users"
            element={
              <RequireAdmin>
                <UsersPage />
              </RequireAdmin>
            }
          />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </GenerateModalProvider>
  );
};

export default App;
