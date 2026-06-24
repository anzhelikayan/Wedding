package com.example.wedding;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public String saveRsvp(
            @RequestParam String fullName,
            @RequestParam String attendance,
            @RequestParam(required = false) String partnerName,
            @RequestParam(value = "drinks", required = false) List<String> drinks,
            @RequestParam(required = false) String allergy,
            @RequestParam(required = false) String meal,
            @RequestParam(required = false) String wishes
    ) {
        GuestResponse response = new GuestResponse();
        response.setFullName(fullName);
        response.setAttendance(attendance);
        response.setPartnerName(partnerName);
        response.setDrinks(drinks == null ? "" : String.join(", ", drinks));
        response.setAllergy(allergy);
        response.setMeal(meal);
        response.setWishes(wishes);
        guestResponseRepository.save(response);

        return "redirect:/?sent=true#countdown";
    }
}
