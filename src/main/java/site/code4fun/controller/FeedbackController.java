package site.code4fun.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.model.dto.ContactUs;
import site.code4fun.service.email.EmailService;

@RestController
@RequestMapping("contact-us")
@RequiredArgsConstructor
@Lazy
public class FeedbackController {
    private final EmailService emailService;
    @PostMapping
    public void contactUs(@RequestBody ContactUs body){
        emailService.sendEmail("trungtrandb@gmail.com", body.subject(), body.toString());
    }
}