package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.dto.form.PasswordForm;
import br.com.sampatech.portal.dto.form.ProfileForm;
import br.com.sampatech.portal.dto.request.UpdateProfileRequest;
import br.com.sampatech.portal.dto.response.UserResponse;
import br.com.sampatech.portal.service.ChatHistoryService;
import br.com.sampatech.portal.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileWebController {

    private final UserService userService;
    private final ChatHistoryService chatHistoryService;

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetails me, Model model) {
        UserResponse user = userService.getProfile(me.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("profileForm", profileFormOf(user));
        model.addAttribute("passwordForm", new PasswordForm());
        return "profile/index";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal UserDetails me,
                                @Valid @ModelAttribute("profileForm") ProfileForm form,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("user", userService.getProfile(me.getUsername()));
            model.addAttribute("passwordForm", new PasswordForm());
            return "profile/index";
        }
        userService.updateProfile(me.getUsername(), new UpdateProfileRequest(form.getName(), null));
        redirect.addFlashAttribute("success", "Dados atualizados com sucesso.");
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal UserDetails me,
                                 @Valid @ModelAttribute("passwordForm") PasswordForm form,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirect) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "As senhas não conferem");
        }

        UserResponse user = userService.getProfile(me.getUsername());
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("profileForm", profileFormOf(user));
            return "profile/index";
        }

        userService.updateProfile(me.getUsername(), new UpdateProfileRequest(user.name(), form.getPassword()));
        redirect.addFlashAttribute("success", "Senha alterada com sucesso.");
        return "redirect:/profile";
    }

    @PostMapping("/deactivate")
    public String deactivate(@AuthenticationPrincipal UserDetails me,
                             Authentication authentication,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        userService.deactivateOwnAccount(me.getUsername());
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/?deactivated";
    }

    @GetMapping("/chat-history")
    public String chatHistory(@AuthenticationPrincipal UserDetails me,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model) {
        model.addAttribute("page", chatHistoryService.getHistory(me.getUsername(), pageable));
        return "profile/chat-history";
    }

    private ProfileForm profileFormOf(UserResponse user) {
        ProfileForm form = new ProfileForm();
        form.setName(user.name());
        return form;
    }
}
