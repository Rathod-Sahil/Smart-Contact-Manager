package com.smart.smartcontactmanager.controller;

import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.User;
import com.smart.smartcontactmanager.helper.Message;
import com.smart.smartcontactmanager.service.EmailService;
import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    Random random = new Random(1000);

    //email id form handler
    @RequestMapping("/forgot")
    public String openEmailForm(){
        return "forgot_email_form";
    }

     @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email,HttpSession session){
        System.out.println("EMAIL "+email);

         User user = this.userRepository.getUserByUserName(email);

            if(user == null){
                //send error message
                 session.setAttribute("message", new Message("You are not registered !!" ,"alert-danger"));
                 return "forgot_email_form";

            }

        //Generating OTP of 6 digit

       int otp = random.nextInt(999999);

       System.out.println("OTP "+otp);


       //write code for sending OTP to email
       String subject = "OTP from SCM";
       String message =""
       +"<div style='border:1px solid #e2e2e2; padding:20px'>"
       +"<h1>"
       +"OTP is "
       +"<b>"+otp
       +"</b>"
       +"</h1>"
       +"</div>";
       String to = email;

        this.emailService.sendEmail(subject, message , to);

       
            session.setAttribute("myotp",otp);
            session.setAttribute("email", email);
            return "verify_otp";
        
    }

    //verify otp
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") int otp,HttpSession session){
        int myotp = (int)session.getAttribute("myotp");

        if(myotp == otp){

                //send change password form

            return "password_change_form";
        }
        else{
            session.setAttribute("message", new Message("You have entered wrong otp !!" ,"alert-danger"));
            return "verify_otp";
        }
       
    }


    // change password
    @PostMapping("/change-password")
    public String  changePassword(@RequestParam("newPassword") String newPassword,HttpSession session){
        String email = (String)session.getAttribute("email");
        User user = this.userRepository.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
        this.userRepository.save(user);

        return "redirect:/signin?change=password changed successfully...";
    }
}
