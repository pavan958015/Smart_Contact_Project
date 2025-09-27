package com.scm.controlller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scm.forms.UserForm;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class PageController {

    @RequestMapping("/home")
    public String home(Model model){
        System.out.println("Home Page Handler");

        //sending data to view
        model.addAttribute("name", "Pavan Soni");
        model.addAttribute("Youtube_Channal", "Pavan Soni");
        model.addAttribute("Github", "https://github.com/pavan958015");

        return "home";
    }

    @RequestMapping("/about")
    public String about(){
        System.out.println("About Page Loading");
        return "about";
    }

    @RequestMapping("/services")
    public String services(){
        System.out.println("Services Page Handler");
        return "services";
    }    

    @GetMapping("/contact")
    public String contact() {
        return new String("contact");
    }
    @GetMapping("/login")
    public String login() {
        return new String("login");
    }
    @GetMapping("/register")
    public String register(Model model) {

        UserForm userForm = new UserForm();
        model.addAttribute("userForm", userForm);

        return new String("register");
    }

    // Processing register
    @RequestMapping(value = "/do-register,method = RequestMethod.POST")
    public String prpcessRegister(@ModelAttribute UserForm userForm) {
        System.out.println("Processing registration");

        // fetch form data
        //User Form
        // validate form data
        // TODO:validate userForm
        // save database
        // message = "Successfully registered"
        // redirect: login page
        return "redirect:/register";
    }
    


}
