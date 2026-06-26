package com.example.wedding;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class AdminController {

    private static final String ADMIN_SESSION_KEY = "adminAuthorized";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GuestResponseRepository guestResponseRepository;
    private final String adminPassword;

    public AdminController(
            GuestResponseRepository guestResponseRepository,
            @Value("${app.admin.password:admin123}") String adminPassword
    ) {
        this.guestResponseRepository = guestResponseRepository;
        this.adminPassword = adminPassword;
    }

    @GetMapping("/admin")
    public String admin(
            @RequestParam(value = "key", required = false) String key,
            HttpSession session,
            Model model
    ) {
        if (isPasswordValid(key)) {
            session.setAttribute(ADMIN_SESSION_KEY, true);
        }

        if (!isAuthorized(session)) {
            model.addAttribute("error", key != null);
            return "admin-login";
        }

        List<GuestResponse> responses = safeUniqueLatestResponses(model);
        model.addAttribute("responses", responses);
        model.addAttribute("total", responses.size());
        model.addAttribute("attending", responses.stream()
                .filter(response -> response.getAttendance() != null && response.getAttendance().startsWith("Так"))
                .count());
        model.addAttribute("notAttending", responses.stream()
                .filter(response -> response.getAttendance() != null && response.getAttendance().startsWith("На жаль"))
                .count());
        return "admin";
    }

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(ADMIN_SESSION_KEY);
        return "redirect:/admin";
    }

    @GetMapping("/admin/responses.csv")
    public void exportCsv(
            @RequestParam(value = "key", required = false) String key,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {
        if (isPasswordValid(key)) {
            session.setAttribute(ADMIN_SESSION_KEY, true);
        }

        if (!isAuthorized(session)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"wedding-rsvp.csv\"");

        response.getWriter().write('\ufeff');
        response.getWriter().println("Дата;Ім'я;Присутність;Пара;Напої;Алергії;Гаряче;Побажання");

        for (GuestResponse guestResponse : uniqueLatestResponses()) {
            response.getWriter().println(String.join(";",
                    csv(guestResponse.getCreatedAt() == null ? "" : guestResponse.getCreatedAt().format(DATE_FORMAT)),
                    csv(guestResponse.getFullName()),
                    csv(guestResponse.getAttendance()),
                    csv(guestResponse.getPartnerName()),
                    csv(guestResponse.getDrinks()),
                    csv(guestResponse.getAllergy()),
                    csv(guestResponse.getMeal()),
                    csv(guestResponse.getWishes())
            ));
        }
    }

    private List<GuestResponse> safeUniqueLatestResponses(Model model) {
        try {
            return uniqueLatestResponses();
        } catch (DataAccessException exception) {
            model.addAttribute("databaseError", true);
            return List.of();
        }
    }

    private List<GuestResponse> uniqueLatestResponses() {
        Map<String, GuestResponse> responsesByName = new LinkedHashMap<>();
        for (GuestResponse response : guestResponseRepository.findAllByOrderByCreatedAtDesc()) {
            String key = response.getFullName() == null || response.getFullName().isBlank()
                    ? "id:" + response.getId()
                    : response.getFullName().trim().toLowerCase(Locale.ROOT);
            responsesByName.putIfAbsent(key, response);
        }
        return new ArrayList<>(responsesByName.values());
    }

    private boolean isAuthorized(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(ADMIN_SESSION_KEY));
    }

    private boolean isPasswordValid(String key) {
        return key != null && !adminPassword.isBlank() && adminPassword.equals(key);
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }
}
