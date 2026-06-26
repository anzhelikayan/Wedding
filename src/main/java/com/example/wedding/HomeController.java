package com.example.wedding;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    private final GuestResponseRepository guestResponseRepository;

    public HomeController(GuestResponseRepository guestResponseRepository) {
        this.guestResponseRepository = guestResponseRepository;
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "sent", defaultValue = "false") boolean sent, Model model) {
        model.addAttribute("sent", sent);
        return "index";
    }

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "ok";
    }

    @PostMapping("/rsvp")
    @Transactional
    public String saveRsvp(
            @RequestParam String fullName,
            @RequestParam String attendance,
            @RequestParam(required = false) String partnerName,
            @RequestParam(value = "drinks", required = false) List<String> drinks,
            @RequestParam(required = false) String allergy,
            @RequestParam(required = false) String meal,
            @RequestParam(required = false) String wishes
    ) {
        String cleanedFullName = clean(fullName);
        if (cleanedFullName.isBlank()) {
            return "redirect:/#rsvp";
        }

        List<GuestResponse> existingResponses =
                guestResponseRepository.findByFullNameIgnoreCaseOrderByCreatedAtDesc(cleanedFullName);
        GuestResponse response = existingResponses.isEmpty() ? new GuestResponse() : existingResponses.get(0);
        response.setFullName(cleanedFullName);
        response.setAttendance(clean(attendance));
        response.setPartnerName(clean(partnerName));
        response.setDrinks(drinks == null ? "" : String.join(", ", drinks));
        response.setAllergy(clean(allergy));
        response.setMeal(clean(meal));
        response.setWishes(clean(wishes));
        response.setCreatedAt(LocalDateTime.now());
        guestResponseRepository.save(response);

        if (existingResponses.size() > 1) {
            guestResponseRepository.deleteAll(existingResponses.subList(1, existingResponses.size()));
        }

        return "redirect:/?sent=true#countdown";
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
