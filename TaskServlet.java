package com.todo;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/TaskServlet")
public class TaskServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // ADD / DELETE / TOGGLE TASK
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try (Connection con = DBconnection.getConnection()) {

            // ADD TASK
            if ("add".equals(action)) {
                String task = request.getParameter("task");
                if (task != null && !task.trim().isEmpty()) {
                    String sql =
                            "INSERT INTO tasks (task, completed) VALUES (?, ?)";
                    try (PreparedStatement ps =
                                 con.prepareStatement(sql)) {
                        ps.setString(1, task.trim());
                        ps.setBoolean(2, false);
                        ps.executeUpdate();
                    } }}
               
                    // DELETE TASK
            else if ("delete".equals(action)) {
                int id = Integer.parseInt(
                        request.getParameter("id") );

                String sql ="DELETE FROM tasks WHERE id = ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }

            // CHECK / UNCHECK TASK
            else if ("toggle".equals(action)) {

                int id = Integer.parseInt(request.getParameter("id") );
                String sql =
                        "UPDATE tasks " +
                        "SET completed = NOT completed " +
                        "WHERE id = ?";

                try (PreparedStatement ps =con.prepareStatement(sql)) {

                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }

            response.sendRedirect("TaskServlet");

        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<h2>Database Error</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
        }
    }

    // DISPLAY TASKS
        @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");

        out.println("<meta charset='UTF-8'>");

        out.println("<meta name='viewport' " +
                "content='width=device-width, initial-scale=1.0'>");

        out.println("<title>My Tasks</title>");

        out.println("<link rel='stylesheet' href='style.css'>");

        out.println("</head>");

        out.println("<body>");

        out.println("<div class='tasks-container'>");


        // =====================================
        // HEADER
        // =====================================

        out.println("<div class='tasks-header'>");

        out.println("<div>");

        out.println("<h1>My Tasks</h1>");

        out.println(
                "<p>Stay organized and get things done.</p>"
        );

        out.println("</div>");

        out.println(
                "<a href='index.html' class='add-new-btn'>" +
                "+ Add Task" +
                "</a>"
        );

        out.println("</div>");


        // =====================================
        // GET TASKS FROM DATABASE
        // =====================================

        try (Connection con = DBconnection.getConnection()) {

            String sql =
                    "SELECT id, task, completed " +
                    "FROM tasks " +
                    "ORDER BY id DESC";

            try (PreparedStatement ps =
                         con.prepareStatement(sql);

                 ResultSet rs = ps.executeQuery()) {

                boolean hasTasks = false;

                while (rs.next()) {

                    hasTasks = true;

                    int id = rs.getInt("id");

                    String task = rs.getString("task");

                    boolean completed =
                            rs.getBoolean("completed");


                    // =================================
                    // TASK CARD
                    // =================================

                    out.println("<div class='task-card'>");


                    // LEFT SIDE

                    out.println("<div class='task-left'>");


                    // CHECK FORM

                    out.println(
                            "<form action='TaskServlet' " +
                            "method='post'>"
                    );

                    out.println(
                            "<input type='hidden' " +
                            "name='action' value='toggle'>"
                    );

                    out.println(
                            "<input type='hidden' " +
                            "name='id' value='" + id + "'>"
                    );


                    if (completed) {

                        out.println(
                                "<button type='submit' " +
                                "class='check-btn checked'>" +
                                "✓" +
                                "</button>"
                        );

                    } else {

                        out.println(
                                "<button type='submit' " +
                                "class='check-btn'>" +
                                "○" +
                                "</button>"
                        );
                    }

                    out.println("</form>");


                    // TASK TEXT

                    if (completed) {

                        out.println(
                                "<span class='completed-task'>" +
                                escapeHtml(task) +
                                "</span>"
                        );

                    } else {

                        out.println(
                                "<span>" +
                                escapeHtml(task) +
                                "</span>"
                        );
                    }

                    out.println("</div>");


                    // =================================
                    // DELETE FORM
                    // =================================

                    out.println(
                            "<form action='TaskServlet' " +
                            "method='post'>"
                    );

                    out.println(
                            "<input type='hidden' " +
                            "name='action' value='delete'>"
                    );

                    out.println(
                            "<input type='hidden' " +
                            "name='id' value='" + id + "'>"
                    );

                    out.println(
                            "<button type='submit' " +
                            "class='delete-btn'>" +
                            "Delete" +
                            "</button>"
                    );

                    out.println("</form>");

                    out.println("</div>");
                }


                // =================================
                // NO TASKS
                // =================================

                if (!hasTasks) {

                    out.println(
                            "<div class='empty'>" +
                            "No tasks yet. Add your first task!" +
                            "</div>"
                    );
                }
            }

        } catch (Exception e) {

            out.println(
                    "<div class='error'>" +
                    "Database Error: " +
                    escapeHtml(e.getMessage()) +
                    "</div>"
            );
        }


        out.println("</div>");

        out.println("</body>");

        out.println("</html>");
    }


    // =========================================
    // PROTECT HTML FROM SPECIAL CHARACTERS
    // =========================================

    private String escapeHtml(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}