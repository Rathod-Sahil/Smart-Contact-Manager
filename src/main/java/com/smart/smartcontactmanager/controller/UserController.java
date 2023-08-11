package com.smart.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.smartcontactmanager.dao.ContactRepository;
import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.Contact;
import com.smart.smartcontactmanager.entities.User;
import com.smart.smartcontactmanager.helper.Message;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    //method to adding common data
    @ModelAttribute
    public void addCommonData(Model m,Principal principal){
        String userName = principal.getName();
       System.out.println("USERNAME "+userName);

       User user = userRepository.getUserByUserName(userName);

       System.out.println("USER "+user);

       m.addAttribute("user",user);
    }


    //dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model,Principal principal){
       model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }



    //open add form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    //Processing add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
    @RequestParam("ProfileImage") MultipartFile file ,
    Principal principal,HttpSession session){

      try {
         String name= principal.getName();
       User user = this.userRepository.getUserByUserName(name);

       //processing and uploading file

       if(file.isEmpty()){
        //if the file is empty then try our message
        System.out.println("Image file is empty");
        contact.setImage("contact.jpg");
       }else{
        //File hte file to folder and update the name to contact
        contact.setImage(file.getOriginalFilename());

       File saveFile =  new ClassPathResource("/static/img").getFile();

       Files.copy(file.getInputStream(),Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename()),StandardCopyOption.REPLACE_EXISTING);

       System.out.println("Image is uploaded");
       }
       contact.setUser(user);


       user.getContacts().add(contact);
       this.userRepository.save(user);
       System.out.println("Added to data base");

      session.setAttribute("message", new Message("Contact saved Successfully !!", "alert-success"));



        System.out.println("DATA "+contact);
      } catch (Exception e) {

      session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
       System.out.println("ERROR "+e.getMessage());
       e.printStackTrace();
      }
      return "normal/add_contact_form";
    }

    //show contacts handler
    //per page = 5[n]
    //Current page = 0[page]
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model model,Principal principal){
       model.addAttribute("title", "Show Contact");

       String userName = principal.getName();
       User user = this.userRepository.getUserByUserName(userName);

        //currentPage -page
        //Contact per page - 5
      Pageable pageable = PageRequest.of(page, 5);
       Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(),pageable);
       model.addAttribute("contacts", contacts);
       model.addAttribute("currentPage", page);
       model.addAttribute("totalPages", contacts.getTotalPages());
      return "normal/show_contacts";
    }

    @GetMapping("/{cId}/contact")
    // Showing specific contact details
    public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal){
      System.out.println("CID "+ cId);

      Contact contact = this.contactRepository.findById(cId).get();

      //
      String userName = principal.getName();
      User user = this.userRepository.getUserByUserName(userName);

      if(user.getId() == contact.getUser().getId()){
        model.addAttribute("contact", contact);
        model.addAttribute("title",contact.getName());
      }

      return "normal/contact_detail";
    }

    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cId,Model model,Principal principal,HttpSession session){

   try {
     Contact contact = this.contactRepository.findById(cId).get();

     String userName = principal.getName();
     User user = this.userRepository.getUserByUserName(userName);

     if(user.getId() == contact.getUser().getId()){
       
      //Delete image
       File deleteFile =  new ClassPathResource("/static/img").getFile();
          File file = new File(deleteFile,contact.getImage());
          if(!contact.getImage().equals("contact.jpg")){
              file.delete();
          }
          file.delete();

       user.getContacts().remove(contact);
       this.userRepository.save(user);
       session.setAttribute("message", new Message("Contact Deleted Successfully", "alert-success"));
     }
   } catch (Exception e) {
     e.printStackTrace();
   }

      return "redirect:/user/show-contacts/0";
    }

    
    //Open Update form handler
    @GetMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid") Integer cid, Model model){
      model.addAttribute("title", "Update Contact");
      Contact contact = this.contactRepository.findById(cid).get();
      model.addAttribute("contact", contact);
      return "normal/update_form";
    }

    //Update contact handler
    @PostMapping("/process-update")
    public String updateHandler(Model model,@ModelAttribute Contact contact,@RequestParam("ProfileImage") MultipartFile file,HttpSession session,Principal principal){
      try{

        //old contact detail
       Contact oldContactDetail= this.contactRepository.findById(contact.getcId()).get();
        //image...
        if(!file.isEmpty()){
          //file rewrite
          //delete old photo
          File deleteFile =  new ClassPathResource("/static/img").getFile();
          File file1 = new File(deleteFile,oldContactDetail.getImage());
          if(!oldContactDetail.getImage().equals("contact.jpg")){
              file1.delete();
          }
          

          //update new photo

           File saveFile =  new ClassPathResource("/static/img").getFile();

          Files.copy(file.getInputStream(),Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename()),StandardCopyOption.REPLACE_EXISTING);
          contact.setImage(file.getOriginalFilename());

        }else{
          contact.setImage(oldContactDetail.getImage());
        }

        User user = this.userRepository.getUserByUserName(principal.getName());

       contact.setUser(user);
       this.contactRepository.save(contact);
       session.setAttribute("message", new Message("Contact Updated Successfully", "alert-success"));
      }
      catch (Exception e){
        e.printStackTrace();
      }
      return "redirect:/user/"+contact.getcId()+"/contact";
    }

    //Your profile
    @GetMapping("/profile")
    public String yourProfile(Model model){
      model.addAttribute("title","Profile page");
      return "normal/profile";
    }

    //open settings handler

    @GetMapping("/settings")
    public String openSettings(){
      return "normal/settings";
    }

    //change password handler

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session){
      
      System.out.println("OLD PASSWORD "+oldPassword);
       System.out.println("NEW PASSWORD "+newPassword);

      String userName = principal.getName();
     User currentUser = userRepository.getUserByUserName(userName);

     if(bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())){
       //change
       currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
       userRepository.save(currentUser);
       session.setAttribute("message", new Message("Your password is successfully changed...", "alert-success"));

        return "redirect:/user/index";
     }else{
        //error...
       session.setAttribute("message", new Message("Your old password is wrong...", "alert-danger"));
         return "redirect:/user/settings";
     }
    }
}

