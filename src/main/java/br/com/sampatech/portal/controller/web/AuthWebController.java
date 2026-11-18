package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.dto.form.RegisterForm;
import br.com.sampatech.portal.dto.request.RegisterRequest;
import br.com.sampatech.portal.exception.BusinessException;
import br.com.sampatech.portal.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthService authService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            authService.register(new RegisterRequest(form.getName(), form.getEmail(), form.getPassword()));
        } catch (BusinessException e) {
            result.rejectValue("email", "duplicate", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}