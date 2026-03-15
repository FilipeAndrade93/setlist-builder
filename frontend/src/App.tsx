import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/login/LoginPage";
import AppLayout from "./components/layout/AppLayout";
import HomePage from "./pages/home/HomePage";

const ComingSoon = ({ page }: { page: string }) => (
  <div
    style={{
      padding: "2rem",
      fontFamily: "'Inter', sans-serif",
      color: "#888",
    }}
  >
    {page} — coming soon
  </div>
);

const RequireAuth = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem("token");
  if (!token) return <Navigate to="/login" replace />;
  return <>{children}</>;
};

const App = () => {
  return (
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
      </Route>
    </Routes>
  );
};

export default App;
