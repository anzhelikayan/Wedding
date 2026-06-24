package com.example.wedding;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

        List<GuestResponse> responses = guestResponseRepository.findAllByOrderByCreatedAtDesc();
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

        for (GuestResponse guestResponse : guestResponseRepository.findAllByOrderByCreatedAtDesc()) {
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
