
import { useEffect, useState } from "react";
import "./App.css";

function App() {
  const [services, setServices] = useState([]);
  const [history, setHistory] = useState([]);
  const [error, setError] = useState("");
  const [historyError, setHistoryError] = useState("");
  const [loading, setLoading] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch("/api/services");

        if (!response.ok) {
          throw new Error("Failed to fetch services");
        }

        const data = await response.json();
        setServices(data);
        setError("");
        setLastUpdated(new Date().toLocaleTimeString());
      } catch {
        setError("Cannot connect to Spring Boot backend");
      } finally {
        setLoading(false);
      }

      try {
        const response = await fetch("/api/history");

        if (!response.ok) {
          throw new Error("Failed to fetch history");
        }

        const data = await response.json();
        setHistory(data);
        setHistoryError("");
      } catch {
        setHistoryError("Unable to load monitoring history");
      }
    };

    fetchData();

    const interval = setInterval(fetchData, 5000);

    return () => clearInterval(interval);
  }, []);

  const upCount = services.filter(
    (service) => service.status === "UP"
  ).length;

  const downCount = services.filter(
    (service) => service.status === "DOWN"
  ).length;

  const recentHistory = [...history]
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    .slice(0, 20);

  return (
    <div
      style={{
        background: "#0f172a",
        minHeight: "100vh",
        color: "white",
        padding: "40px",
        fontFamily: "Arial, sans-serif"
      }}
    >
      <h1>AegisFlow</h1>
      <p>Microservices Monitoring Dashboard</p>

      <hr style={{ borderColor: "#334155" }} />

      <h2>System Overview</h2>

      <div style={{ display: "flex", gap: "20px", flexWrap: "wrap" }}>
        <div style={summaryStyle}>
          <h3>Total Services</h3>
          <h1>{services.length}</h1>
        </div>

        <div style={summaryStyle}>
          <h3>Healthy Services</h3>
          <h1 style={{ color: "#4ade80" }}>{upCount}</h1>
        </div>

        <div style={summaryStyle}>
          <h3>Failed Services</h3>
          <h1 style={{ color: "#f87171" }}>{downCount}</h1>
        </div>
      </div>

      <h2 style={{ marginTop: "35px" }}>Service Status</h2>

      {loading && <p>Loading services...</p>}

      {error && <p style={{ color: "#f87171" }}>{error}</p>}

      {services.map((service) => (
        <div key={service.name} style={serviceStyle}>
          <h3>{service.name}</h3>
          <p
            style={{
              color: service.status === "UP" ? "#4ade80" : "#f87171",
              fontWeight: "bold"
            }}
          >
            ● {service.status}
          </p>
        </div>
      ))}

      <h2 style={{ marginTop: "40px" }}>Monitoring History</h2>

      <p style={{ color: "#94a3b8" }}>
        Total saved records: {history.length} | Showing latest 20
      </p>

      {historyError && (
        <p style={{ color: "#f87171" }}>{historyError}</p>
      )}

      <div style={{ overflowX: "auto" }}>
        <table
          style={{
            width: "100%",
            borderCollapse: "collapse",
            background: "#1e293b",
            borderRadius: "10px"
          }}
        >
          <thead>
            <tr>
              {["ID", "Service Name", "Status", "Timestamp"].map((heading) => (
                <th key={heading} style={cellStyle}>
                  {heading}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {recentHistory.map((record) => (
              <tr key={record.id}>
                <td style={cellStyle}>{record.id}</td>
                <td style={cellStyle}>{record.serviceName}</td>
                <td
                  style={{
                    ...cellStyle,
                    color: record.status === "UP" ? "#4ade80" : "#f87171",
                    fontWeight: "bold"
                  }}
                >
                  ● {record.status}
                </td>
                <td style={cellStyle}>
                  {new Date(record.timestamp).toLocaleString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <p style={{ marginTop: "30px", color: "#94a3b8" }}>
        Auto-refresh: Every 5 seconds
      </p>

      <p style={{ color: "#94a3b8" }}>
        Last updated: {lastUpdated || "Not yet updated"}
      </p>
    </div>
  );
}

const summaryStyle = {
  background: "#1e293b",
  padding: "20px",
  borderRadius: "10px",
  minWidth: "170px",
  flex: "1"
};

const serviceStyle = {
  background: "#1e293b",
  padding: "20px",
  marginBottom: "15px",
  borderRadius: "10px"
};

const cellStyle = {
  padding: "14px",
  textAlign: "left",
  borderBottom: "1px solid #334155"
};

export default App;
