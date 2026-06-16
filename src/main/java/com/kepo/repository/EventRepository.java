package com.kepo.repository;

import com.kepo.config.DatabaseConfig;
import com.kepo.model.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventRepository {

    private final DatabaseConfig dbConfig;

    public EventRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT event_id, name, location, status, description, shelter_count, created_at FROM events ORDER BY created_at DESC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToEvent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Event findById(int eventId) {
        String sql = "SELECT event_id, name, location, status, description, shelter_count, created_at FROM events WHERE event_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvent(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Event event) {
        if (event.getEventId() > 0) {
            String sql = "UPDATE events SET name = ?, location = ?, status = ?, description = ?, shelter_count = ? WHERE event_id = ?";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, event.getName());
                ps.setString(2, event.getLocation());
                ps.setString(3, event.getStatus());
                ps.setString(4, event.getDescription());
                ps.setInt(5, event.getShelterCount());
                ps.setInt(6, event.getEventId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "INSERT INTO events (name, location, status, description, shelter_count) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, event.getName());
                ps.setString(2, event.getLocation());
                ps.setString(3, event.getStatus());
                ps.setString(4, event.getDescription());
                ps.setInt(5, event.getShelterCount());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            event.setEventId(rs.getInt(1));
                        }
                    }
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean delete(int eventId) {
        String sql = "DELETE FROM events WHERE event_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        Event ev = new Event();
        ev.setEventId(rs.getInt("event_id"));
        ev.setName(rs.getString("name"));
        ev.setLocation(rs.getString("location"));
        ev.setStatus(rs.getString("status"));
        ev.setDescription(rs.getString("description"));
        ev.setShelterCount(rs.getInt("shelter_count"));
        ev.setCreatedAt(rs.getTimestamp("created_at"));
        return ev;
    }
}
