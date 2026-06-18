package com.scm.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.scm.entities.User;
import com.scm.forms.UserForm;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class PageController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.scm.repsitories.UserRepo userRepo;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.scm.services.EmailService emailService;

    @Autowired
    private com.scm.helpers.Helper helper;

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @RequestMapping("/home")
    public String home(Model model) {
        System.out.println("Home page handler");
        // sending data to view
        model.addAttribute("name", "Substring Technologies");
        model.addAttribute("youtubeChannel", "Learn Code With Durgesh");
        model.addAttribute("githubRepo", "https://github.com/learncodewithdurgesh/");
        return "home";
    }

    // about route

    @RequestMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("isLogin", true);
        System.out.println("About page loading");
        return "about";
    }

    // services

    @RequestMapping("/services")
    public String servicesPage() {
        System.out.println("services page loading");
        return "services";
    }

    // contact page

    @GetMapping("/contact")
    public String contact() {
        return new String("contact");
    }

    // this is showing login page
    @GetMapping("/login")
    public String login() {
        return new String("login");
    }

    // registration page
    @GetMapping("/register")
    public String register(Model model) {

        UserForm userForm = new UserForm();
        // default data bhi daal sakte hai
        // userForm.setName("Durgesh");
        // userForm.setAbout("This is about : Write something about yourself");
        model.addAttribute("userForm", userForm);

        return "register";
    }

    // processing register

    @RequestMapping(value = "/do-register", method = RequestMethod.POST)
    public String processRegister(@Valid @ModelAttribute UserForm userForm, BindingResult rBindingResult,
            HttpSession session) {
        System.out.println("Processing registration");
        // fetch form data
        // UserForm
        System.out.println(userForm);

        // validate form data
        if (rBindingResult.hasErrors()) {
            return "register";
        }

        // TODO::Validate userForm[Next Video]

        // save to database

        // userservice

        // UserForm--> User
        // User user = User.builder()
        // .name(userForm.getName())
        // .email(userForm.getEmail())
        // .password(userForm.getPassword())
        // .about(userForm.getAbout())
        // .phoneNumber(userForm.getPhoneNumber())
        // .profilePic(
        // "https://www.learncodewithdurgesh.com/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fdurgesh_sir.35c6cb78.webp&w=1920&q=75")
        // .build();

        User user = new User();
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword());
        user.setAbout(userForm.getAbout());
        user.setPhoneNumber(userForm.getPhoneNumber());
        user.setEnabled(false);
        user.setEmailVerified(false);
        String fallbackUrl = "https://ui-avatars.com/api/?name=" + user.getName().replace(" ", "+") + "&background=059669&color=fff";
        try {
            String hash = org.springframework.util.DigestUtils.md5DigestAsHex(user.getEmail().trim().toLowerCase().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String encodedFallback = java.net.URLEncoder.encode(fallbackUrl, "UTF-8");
            user.setProfilePic("https://www.gravatar.com/avatar/" + hash + "?d=" + encodedFallback);
        } catch (Exception e) {
            user.setProfilePic(fallbackUrl);
        }

        User savedUser = userService.saveUser(user);

        System.out.println("user saved :");

        // message = "Registration Successful"

        // add the message:

        Message message = Message.builder().content("Registration Successful").type(MessageType.green).build();

        session.setAttribute("message", message);

        // redirectto login page
        return "redirect:/register";
    }

    // Forgot password view
    @GetMapping("/forgot-password")
    public String forgotPasswordView() {
        return "forgot_password";
    }

    // Process forgot password request
    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
    public String processForgotPassword(
            @org.springframework.web.bind.annotation.RequestParam("email") String email,
            HttpSession session) {

        java.util.Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = java.util.UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            userRepo.save(user);

            // Construct link
            String resetLink = helper.getLinkForEmailVerificatiton(token).replace("/auth/verify-email", "/reset-password");
            try {
                emailService.sendEmail(user.getEmail(), "Reset Password : Smart Contact Manager",
                        "Please click the link below to reset your password:\n\n" + resetLink);
                session.setAttribute("message", Message.builder()
                        .content("Password reset link has been sent to your email ID!")
                        .type(MessageType.green)
                        .build());
            } catch (Exception e) {
                session.setAttribute("message", Message.builder()
                        .content("Failed to send reset email. Please try again.")
                        .type(MessageType.red)
                        .build());
            }
        } else {
            session.setAttribute("message", Message.builder()
                    .content("No account found with this email ID.")
                    .type(MessageType.red)
                    .build());
        }

        return "redirect:/forgot-password";
    }

    // Reset password view
    @GetMapping("/reset-password")
    public String resetPasswordView(@org.springframework.web.bind.annotation.RequestParam("token") String token, Model model, HttpSession session) {
        java.util.Optional<User> userOptional = userRepo.findByPasswordResetToken(token);
        if (userOptional.isEmpty()) {
            session.setAttribute("message", Message.builder()
                    .content("Invalid or expired password reset token.")
                    .type(MessageType.red)
                    .build());
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "reset_password";
    }

    // Process reset password
    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    public String processResetPassword(
            @org.springframework.web.bind.annotation.RequestParam("token") String token,
            @org.springframework.web.bind.annotation.RequestParam("password") String password,
            @org.springframework.web.bind.annotation.RequestParam("confirmPassword") String confirmPassword,
            HttpSession session) {

        if (!password.equals(confirmPassword)) {
            session.setAttribute("message", Message.builder()
                    .content("Passwords do not match.")
                    .type(MessageType.red)
                    .build());
            return "redirect:/reset-password?token=" + token;
        }

        java.util.Optional<User> userOptional = userRepo.findByPasswordResetToken(token);
        if (userOptional.isEmpty()) {
            session.setAttribute("message", Message.builder()
                    .content("Invalid or expired token.")
                    .type(MessageType.red)
                    .build());
            return "redirect:/login";
        }

        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordResetToken(null);
        userRepo.save(user);

        session.setAttribute("message", Message.builder()
                .content("Password reset successful! You can now log in with your new password.")
                .type(MessageType.green)
                .build());

        return "redirect:/login";
    }

}
