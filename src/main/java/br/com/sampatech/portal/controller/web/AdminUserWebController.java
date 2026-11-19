package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.dto.form.UserAdminForm;
import br.com.sampatech.portal.dto.request.AdminUserUpdateRequest;
import br.com.sampatech.portal.dto.response.UserResponse;
import br.com.sampatech.portal.entity.UserType;
import br.com.sampatech.portal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserWebController {

    private final UserService userService;

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "id") Pageable pageable, Model model) {
        model.addAttribute("page", userService.findAll(pageable));
        return "admin/users";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        UserResponse user = userService.findById(id);
        UserAdminForm form = new UserAdminForm();
        form.setUserType(user.userType());
        form.setActive(Boolean.TRUE.equals(user.active()));

        model.addAttribute("user", user);
        model.addAttribute("form", form);
        model.addAttribute("userTypes", UserType.values());
        return "admin/user-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") UserAdminForm form,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails me,
                         Model model,
                         RedirectAttributes redirect) {
        UserResponse user = userService.findById(id);

        if (user.email().equalsIgnoreCase(me.getUsername())) {
            redirect.addFlashAttribute("errorMessage", "Você não pode alterar o tipo nem desativar a própria conta.");
            return "redirect:/admin/users";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("userTypes", UserType.values());
            return "admin/user-form";
        }

        userService.adminUpdate(id, new AdminUserUpdateRequest(form.getUserType(), form.isActive()));
        redirect.addFlashAttribute("success", "Usuário " + user.name() + " atualizado.");
        return "redirect:/admin/users";
    }
}
