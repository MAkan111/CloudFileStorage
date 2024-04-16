package ru.maakan111.cloudfilestorage.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.maakan111.cloudfilestorage.exceptions.UsernameAlreadyExistsException;
import ru.maakan111.cloudfilestorage.models.User;
import ru.maakan111.cloudfilestorage.services.UserService;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);
    private final UserService userService;

    @GetMapping("/login")
    public String getLoginForm() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String getSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signUpUser(@ModelAttribute("user") @Valid User user,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }
        try {
            userService.createUser(user);
            LOGGER.debug("Sign up successful for user \"{}\" (id={})", user.getUsername(), user.getId());
            return "redirect:/login";
        } catch (UsernameAlreadyExistsException exc) {
            LOGGER.debug("Sign up failed for user \"{}\"", user.getUsername(), exc);
            bindingResult.rejectValue("username", "user.alreadyExists", "User with this username already exists");
            return "auth/signup";
        }
    }
}
