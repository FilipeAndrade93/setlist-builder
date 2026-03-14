import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/login/LoginPage";

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
        path="/"
        element={
          <RequireAuth>
            <ComingSoon page="Home" />
          </RequireAuth>
        }
      />
    </Routes>
  );
};

export default App;
