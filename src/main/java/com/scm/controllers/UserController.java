package com.scm.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scm.services.UserService;
import com.scm.services.ImageService;
import com.scm.entities.User;
import com.scm.forms.UserProfileForm;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.helpers.Helper;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Controller
@RequestMapping("/user")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    // user dashbaord page

    @RequestMapping(value = "/dashboard")
    public String userDashboard() {
        System.out.println("User dashboard");
        return "user/dashboard";
    }

    // user profile page

    @RequestMapping(value = "/profile")
    public String userProfile(Model model, Authentication authentication) {

        return "user/profile";
    }

    // edit profile views

    @RequestMapping(value = "/profile/edit", method = RequestMethod.GET)
    public String editProfile(Model model, Authentication authentication) {
        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        UserProfileForm userProfileForm = UserProfileForm.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .about(user.getAbout())
                .profilePic(user.getProfilePic())
                .build();

        model.addAttribute("userProfileForm", userProfileForm);
        model.addAttribute("userProvider", user.getProvider().toString());
        return "user/profile_edit";
    }

    @RequestMapping(value = "/profile/edit", method = RequestMethod.POST)
    public String updateProfile(@Valid @ModelAttribute UserProfileForm userProfileForm,
            BindingResult result,
            Authentication authentication,
            HttpSession session,
            Model model) {

        String username = Helper.getEmailOfLoggedInUser(authentication);
        User dbUser = userService.getUserByEmail(username);

        if (result.hasErrors()) {
            model.addAttribute("userProvider", dbUser.getProvider().toString());
            return "user/profile_edit";
        }

        // Check if email was updated and if it is already in use by someone else
        String newEmail = userProfileForm.getEmail().trim();
        if (!dbUser.getEmail().equalsIgnoreCase(newEmail)) {
            // Only allow changing email if the provider is SELF
            if (dbUser.getProvider() != com.scm.entities.Providers.SELF) {
                session.setAttribute("message", Message.builder()
                        .content("OAuth account emails cannot be changed.")
                        .type(MessageType.red)
                        .build());
                return "redirect:/user/profile/edit";
            }

            if (userService.isUserExistByEmail(newEmail)) {
                result.rejectValue("email", "email.exists", "This email address is already in use.");
                model.addAttribute("userProvider", dbUser.getProvider().toString());
                return "user/profile_edit";
            }
        }

        // Process profile picture upload if any
        if (userProfileForm.getProfileImage() != null && !userProfileForm.getProfileImage().isEmpty()) {
            String filename = java.util.UUID.randomUUID().toString();
            String fileURL = imageService.uploadImage(userProfileForm.getProfileImage(), filename);
            dbUser.setProfilePic(fileURL);
        }

        // Update database user
        dbUser.setName(userProfileForm.getName());
        dbUser.setPhoneNumber(userProfileForm.getPhoneNumber());
        dbUser.setAbout(userProfileForm.getAbout());

        boolean emailChanged = !dbUser.getEmail().equalsIgnoreCase(newEmail);
        if (emailChanged) {
            dbUser.setEmail(newEmail);
        }

        userService.updateUser(dbUser);

        // If email changed, programmatically update the Spring Security authentication context
        if (emailChanged) {
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                    dbUser,
                    authentication.getCredentials(),
                    authentication.getAuthorities()
                )
            );
        }

        session.setAttribute("message", Message.builder()
                .content("Profile updated successfully!")
                .type(MessageType.green)
                .build());

        return "redirect:/user/profile";
    }

    // Phone verification GET
    @RequestMapping(value = "/profile/verify-phone", method = RequestMethod.GET)
    public String verifyPhoneView(Model model, Authentication authentication, HttpSession session) {
        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        // Check if phone number exists
        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            session.setAttribute("message", Message.builder()
                    .content("Please configure a phone number in edit profile first.")
                    .type(MessageType.red)
                    .build());
            return "redirect:/user/profile";
        }

        // If already verified
        if (user.isPhoneVerified()) {
            session.setAttribute("message", Message.builder()
                    .content("Phone number is already verified.")
                    .type(MessageType.green)
                    .build());
            return "redirect:/user/profile";
        }

        // Generate OTP if not present in session
        String otp = (String) session.getAttribute("phoneOtp");
        Long otpTime = (Long) session.getAttribute("phoneOtpTime");
        long currentTime = System.currentTimeMillis();

        if (otp == null || otpTime == null || (currentTime - otpTime > 5 * 60 * 1000)) {
            // Generate 6-digit OTP
            otp = String.format("%06d", new java.util.Random().nextInt(1000000));
            session.setAttribute("phoneOtp", otp);
            session.setAttribute("phoneOtpTime", currentTime);

            // Log/Print OTP to Console for local verification
            System.out.println("\n==============================================");
            System.out.println("   [SMS simulation] OTP to verify phone: " + otp);
            System.out.println("==============================================\n");
        }

        // Mask phone number for security
        String rawPhone = user.getPhoneNumber().trim();
        String maskedPhone = rawPhone.length() > 4 
            ? "******" + rawPhone.substring(rawPhone.length() - 4)
            : rawPhone;

        model.addAttribute("maskedPhone", maskedPhone);
        return "user/verify_phone";
    }

    // Phone verification POST
    @RequestMapping(value = "/profile/verify-phone", method = RequestMethod.POST)
    public String processVerifyPhone(@org.springframework.web.bind.annotation.RequestParam("otp") String submittedOtp,
            Authentication authentication,
            HttpSession session,
            Model model) {

        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);

        String sessionOtp = (String) session.getAttribute("phoneOtp");
        Long otpTime = (Long) session.getAttribute("phoneOtpTime");
        long currentTime = System.currentTimeMillis();

        // Mask phone number for security/re-rendering
        String rawPhone = user.getPhoneNumber() != null ? user.getPhoneNumber().trim() : "";
        String maskedPhone = rawPhone.length() > 4 
            ? "******" + rawPhone.substring(rawPhone.length() - 4)
            : rawPhone;
        model.addAttribute("maskedPhone", maskedPhone);

        if (sessionOtp == null || otpTime == null) {
            session.setAttribute("message", Message.builder()
                    .content("No active OTP request. Please request a new code.")
                    .type(MessageType.red)
                    .build());
            return "user/verify_phone";
        }

        // Check expiry (5 minutes)
        if (currentTime - otpTime > 5 * 60 * 1000) {
            session.setAttribute("message", Message.builder()
                    .content("OTP code has expired. Please request a new one.")
                    .type(MessageType.red)
                    .build());
            return "user/verify_phone";
        }

        // Verify OTP
        if (!sessionOtp.equals(submittedOtp.trim())) {
            session.setAttribute("message", Message.builder()
                    .content("Invalid verification code. Please try again.")
                    .type(MessageType.red)
                    .build());
            return "user/verify_phone";
        }

        // OTP matched, set verified
        user.setPhoneVerified(true);
        userService.updateUser(user);

        // Clear session OTP
        session.removeAttribute("phoneOtp");
        session.removeAttribute("phoneOtpTime");

        session.setAttribute("message", Message.builder()
                .content("Phone number verified successfully!")
                .type(MessageType.green)
                .build());

        return "redirect:/user/profile";
    }

    // Phone verification Resend
    @RequestMapping(value = "/profile/verify-phone/resend", method = RequestMethod.GET)
    public String resendPhoneOtp(Authentication authentication, HttpSession session) {
        // Clear existing OTP
        session.removeAttribute("phoneOtp");
        session.removeAttribute("phoneOtpTime");

        session.setAttribute("message", Message.builder()
                .content("A new verification code has been generated. Check console logs.")
                .type(MessageType.green)
                .build());

        return "redirect:/user/profile/verify-phone";
    }

    // user add contacts page

    // user view contacts

    // user edit contact

    // user delete contact

}
