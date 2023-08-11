package com.smart.smartcontactmanager.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.User;
import com.smart.smartcontactmanager.helper.Message;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class HomeController {


    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    Random random = new Random(1000);

    @RequestMapping("/")
    public String home(Model model){
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }
   
    @RequestMapping("/signup")
    public String signup(Model model){
        model.addAttribute("title", "Register - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

     // handler for rigistering user
    @PostMapping("/do_register")
    public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result,@RequestParam(value = "agreement",defaultValue = "false") boolean agreement,Model model,HttpSession session){
      
       try {

         if(!agreement){
            System.out.println("You have not agreed terms and conditions");
            throw new Exception("You have not agreed terms and conditions");
        }

        if(result.hasErrors()){
            System.out.println("ERROR " + result.toString());
            model.addAttribute("user", user);
            return "signup";
        }
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setImageUrl("default.png");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

      
        System.out.println("Agreement "+agreement);
        System.out.println("USER "+user);

        this.userRepository.save(user);

        model.addAttribute("user", new User());
        session.setAttribute("message", new Message("Successfully registered !!", "alert-success"));
        
        return "signup";

       } catch (Exception e) {
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
            return "signup";
       }

    }

    @PostMapping("/do_signup")
    public String signupUser(@Valid @ModelAttribute("user") User user,BindingResult result,@RequestParam(value = "agreement",defaultValue = "false") boolean agreement,Model model,HttpSession session){
       
       try {

            if(!agreement){
                System.out.println("You have not agreed terms and conditions");
                throw new Exception("You have not agreed terms and conditions");
        }

            if(result.hasErrors()){
                    System.out.println("ERROR " + result.toString());
                    model.addAttribute("user", user);
                    return "signup";
            }
            

                user.setRole("ROLE_USER");
                user.setEnabled(true);
                user.setImageUrl("default.png");
                user.setPassword(passwordEncoder.encode(user.getPassword()));

      
                System.out.println("Agreement "+agreement);
                System.out.println("USER "+user);

                this.userRepository.save(user);

                model.addAttribute("user", new User());
                session.setAttribute("message", new Message("Successfully registered !!", "alert-success"));   
           
             return "signup";

       } catch (Exception e) {
                model.addAttribute("user", user);
                session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
                return "signup";
       }

    }

    //handler for custom login
    @GetMapping("/signin")
    public String customLogin(Model model){
        model.addAttribute("title", "Login page");
        return "login";
    }

}
