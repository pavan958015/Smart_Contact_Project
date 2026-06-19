package com.scm.controllers;

import java.util.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.forms.ContactForm;
import com.scm.forms.ContactSearchForm;
import com.scm.helpers.AppConstants;
import com.scm.helpers.Helper;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.ContactService;
import com.scm.services.ImageService;
import com.scm.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.PrintWriter;

@Controller
@RequestMapping("/user/contacts")
public class ContactController {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @RequestMapping("/add")
    // add contact page: handler
    public String addContactView(Model model) {
        ContactForm contactForm = new ContactForm();

        contactForm.setFavorite(true);
        model.addAttribute("contactForm", contactForm);
        return "user/add_contact";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String saveContact(@Valid @ModelAttribute ContactForm contactForm, BindingResult result,
            Authentication authentication, HttpSession session) {

        // process the form data

        // 1 validate form

        if (result.hasErrors()) {

            result.getAllErrors().forEach(error -> logger.info(error.toString()));

            session.setAttribute("message", Message.builder()
                    .content("Please correct the following errors")
                    .type(MessageType.red)
                    .build());
            return "user/add_contact";
        }

        String username = Helper.getEmailOfLoggedInUser(authentication);
        // form ---> contact

        User user = userService.getUserByEmail(username);
        // 2 process the contact picture

        // image process

        // uplod karne ka code
        Contact contact = new Contact();
        contact.setName(contactForm.getName());
        contact.setFavorite(contactForm.isFavorite());
        contact.setEmail(contactForm.getEmail());
        contact.setPhoneNumber(contactForm.getPhoneNumber());
        contact.setAddress(contactForm.getAddress());
        contact.setDescription(contactForm.getDescription());
        contact.setUser(user);
        contact.setLinkedInLink(contactForm.getLinkedInLink());
        contact.setWebsiteLink(contactForm.getWebsiteLink());

        if (contactForm.getContactImage() != null && !contactForm.getContactImage().isEmpty()) {
            String filename = UUID.randomUUID().toString();
            String fileURL = imageService.uploadImage(contactForm.getContactImage(), filename);
            contact.setPicture(fileURL);
            contact.setCloudinaryImagePublicId(filename);

        }
        contactService.save(contact);
        System.out.println(contactForm);

        // 3 set the contact picture url

        // 4 `set message to be displayed on the view

        session.setAttribute("message",
                Message.builder()
                        .content("You have successfully added a new contact")
                        .type(MessageType.green)
                        .build());

        return "redirect:/user/contacts/add";

    }

    // view contacts

    @RequestMapping
    public String viewContacts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = AppConstants.PAGE_SIZE + "") int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction, Model model,
            Authentication authentication) {

        // load all the user contacts
        String username = Helper.getEmailOfLoggedInUser(authentication);

        User user = userService.getUserByEmail(username);

        Page<Contact> pageContact = contactService.getByUser(user, page, size, sortBy, direction);

        model.addAttribute("pageContact", pageContact);
        model.addAttribute("pageSize", AppConstants.PAGE_SIZE);

        model.addAttribute("contactSearchForm", new ContactSearchForm());

        return "user/contacts";
    }

    // search handler

    @RequestMapping("/search")
    public String searchHandler(

            @ModelAttribute ContactSearchForm contactSearchForm,
            @RequestParam(value = "size", defaultValue = AppConstants.PAGE_SIZE + "") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model,
            Authentication authentication) {

        logger.info("field {} keyword {}", contactSearchForm.getField(), contactSearchForm.getValue());

        var user = userService.getUserByEmail(Helper.getEmailOfLoggedInUser(authentication));

        String field = contactSearchForm.getField();
        String value = contactSearchForm.getValue();

        if (field == null || field.trim().isEmpty()) {
            field = "name";
            contactSearchForm.setField("name");
        }

        Page<Contact> pageContact = Page.empty();
        if (field.equalsIgnoreCase("name")) {
            pageContact = contactService.searchByName(value, size, page, sortBy, direction,
                    user);
        } else if (field.equalsIgnoreCase("email")) {
            pageContact = contactService.searchByEmail(value, size, page, sortBy, direction,
                    user);
        } else if (field.equalsIgnoreCase("phone")) {
            pageContact = contactService.searchByPhoneNumber(value, size, page, sortBy,
                    direction, user);
        }

        logger.info("pageContact {}", pageContact);

        model.addAttribute("contactSearchForm", contactSearchForm);

        model.addAttribute("pageContact", pageContact);

        model.addAttribute("pageSize", AppConstants.PAGE_SIZE);

        return "user/search";
    }

    // detete contact
    @RequestMapping("/delete/{contactId}")
    public String deleteContact(
            @PathVariable("contactId") String contactId,
            HttpSession session) {
        contactService.delete(contactId);
        logger.info("contactId {} deleted", contactId);

        session.setAttribute("message",
                Message.builder()
                        .content("Contact is Deleted successfully !! ")
                        .type(MessageType.green)
                        .build()

        );

        return "redirect:/user/contacts";
    }

    // update contact form view
    @GetMapping("/view/{contactId}")
    public String updateContactFormView(
            @PathVariable("contactId") String contactId,
            Model model) {

        var contact = contactService.getById(contactId);
        ContactForm contactForm = new ContactForm();
        contactForm.setName(contact.getName());
        contactForm.setEmail(contact.getEmail());
        contactForm.setPhoneNumber(contact.getPhoneNumber());
        contactForm.setAddress(contact.getAddress());
        contactForm.setDescription(contact.getDescription());
        contactForm.setFavorite(contact.isFavorite());
        contactForm.setWebsiteLink(contact.getWebsiteLink());
        contactForm.setLinkedInLink(contact.getLinkedInLink());
        contactForm.setPicture(contact.getPicture());
        ;
        model.addAttribute("contactForm", contactForm);
        model.addAttribute("contactId", contactId);

        return "user/update_contact_view";
    }

    @RequestMapping(value = "/update/{contactId}", method = RequestMethod.POST)
    public String updateContact(@PathVariable("contactId") String contactId,
            @Valid @ModelAttribute ContactForm contactForm,
            BindingResult bindingResult,
            Model model) {

        // update the contact
        if (bindingResult.hasErrors()) {
            return "user/update_contact_view";
        }

        var con = contactService.getById(contactId);
        con.setId(contactId);
        con.setName(contactForm.getName());
        con.setEmail(contactForm.getEmail());
        con.setPhoneNumber(contactForm.getPhoneNumber());
        con.setAddress(contactForm.getAddress());
        con.setDescription(contactForm.getDescription());
        con.setFavorite(contactForm.isFavorite());
        con.setWebsiteLink(contactForm.getWebsiteLink());
        con.setLinkedInLink(contactForm.getLinkedInLink());

        // process image:

        if (contactForm.getContactImage() != null && !contactForm.getContactImage().isEmpty()) {
            logger.info("file is not empty");
            String fileName = UUID.randomUUID().toString();
            String imageUrl = imageService.uploadImage(contactForm.getContactImage(), fileName);
            con.setCloudinaryImagePublicId(fileName);
            con.setPicture(imageUrl);
            contactForm.setPicture(imageUrl);

        } else {
            logger.info("file is empty");
        }

        var updateCon = contactService.update(con);
        logger.info("updated contact {}", updateCon);

        model.addAttribute("message", Message.builder().content("Contact Updated !!").type(MessageType.green).build());

        return "redirect:/user/contacts/view/" + contactId;
    }

    @GetMapping("/import-export")
    public String importExportView() {
        return "user/import_export";
    }

    @PostMapping("/import")
    public String importContacts(@RequestParam("file") MultipartFile file, Authentication authentication, HttpSession session) {
        if (file.isEmpty()) {
            session.setAttribute("message", Message.builder()
                    .content("Please select a file to import.")
                    .type(MessageType.red)
                    .build());
            return "redirect:/user/contacts/import-export";
        }

        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);
        String filename = file.getOriginalFilename();
        
        int importedCount = 0;
        try {
            if (filename != null && filename.toLowerCase().endsWith(".csv")) {
                // Parse CSV
                try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    com.opencsv.CSVReader csvReader = new com.opencsv.CSVReader(reader);
                    List<String[]> rows = csvReader.readAll();
                    boolean isHeader = true;
                    for (String[] row : rows) {
                        if (isHeader) {
                            isHeader = false;
                            // Check if the first row is actually a header row
                            if (row.length > 0 && row[0].trim().equalsIgnoreCase("Name")) {
                                continue;
                            }
                        }
                        
                        if (row.length == 0 || (row.length == 1 && row[0].trim().isEmpty())) {
                            continue;
                        }
                        
                        Contact contact = new Contact();
                        // Expected standard headers: Name, Email, Phone, Address, Description, Website, LinkedIn, Favorite
                        if (row.length > 0) contact.setName(row[0].trim());
                        if (row.length > 1) contact.setEmail(row[1].trim());
                        if (row.length > 2) contact.setPhoneNumber(row[2].trim());
                        if (row.length > 3) contact.setAddress(row[3].trim());
                        if (row.length > 4) contact.setDescription(row[4].trim());
                        if (row.length > 5) contact.setWebsiteLink(row[5].trim());
                        if (row.length > 6) contact.setLinkedInLink(row[6].trim());
                        if (row.length > 7) {
                            contact.setFavorite("true".equalsIgnoreCase(row[7].trim()));
                        }
                        
                        if (contact.getName() == null || contact.getName().trim().isEmpty()) {
                            contact.setName("Imported CSV Contact");
                        }
                        
                        contact.setUser(user);
                        contactService.save(contact);
                        importedCount++;
                    }
                }
            } else if (filename != null && (filename.toLowerCase().endsWith(".vcf") || filename.toLowerCase().endsWith(".vcard"))) {
                // Parse vCard
                List<ezvcard.VCard> vcards = ezvcard.Ezvcard.parse(file.getInputStream()).all();
                for (ezvcard.VCard vcard : vcards) {
                    Contact contact = new Contact();
                    
                    // Name
                    if (vcard.getFormattedName() != null) {
                        contact.setName(vcard.getFormattedName().getValue());
                    } else if (vcard.getStructuredName() != null) {
                        String familyName = vcard.getStructuredName().getFamily();
                        String givenName = vcard.getStructuredName().getGiven();
                        contact.setName((givenName != null ? givenName : "") + " " + (familyName != null ? familyName : ""));
                    }
                    
                    if (contact.getName() == null || contact.getName().trim().isEmpty()) {
                        contact.setName("Imported vCard Contact");
                    }
                    
                    // Email
                    if (vcard.getEmails() != null && !vcard.getEmails().isEmpty()) {
                        contact.setEmail(vcard.getEmails().get(0).getValue());
                    }
                    
                    // Phone
                    if (vcard.getTelephoneNumbers() != null && !vcard.getTelephoneNumbers().isEmpty()) {
                        ezvcard.property.Telephone tel = vcard.getTelephoneNumbers().get(0);
                        String phoneVal = tel.getText();
                        if (phoneVal == null || phoneVal.isEmpty()) {
                            if (tel.getUri() != null) {
                                phoneVal = tel.getUri().toString();
                            }
                        }
                        contact.setPhoneNumber(phoneVal);
                    }
                    
                    // Address
                    if (vcard.getAddresses() != null && !vcard.getAddresses().isEmpty()) {
                        ezvcard.property.Address addr = vcard.getAddresses().get(0);
                        if (addr.getLabel() != null) {
                            contact.setAddress(addr.getLabel());
                        } else {
                            StringBuilder sb = new StringBuilder();
                            if (addr.getStreetAddress() != null) sb.append(addr.getStreetAddress()).append(", ");
                            if (addr.getLocality() != null) sb.append(addr.getLocality()).append(", ");
                            if (addr.getRegion() != null) sb.append(addr.getRegion()).append(" ");
                            if (addr.getPostalCode() != null) sb.append(addr.getPostalCode());
                            String fullAddr = sb.toString().trim();
                            if (fullAddr.endsWith(",")) {
                                fullAddr = fullAddr.substring(0, fullAddr.length() - 1).trim();
                            }
                            contact.setAddress(fullAddr.isEmpty() ? null : fullAddr);
                        }
                    }
                    
                    // Description
                    if (vcard.getNotes() != null && !vcard.getNotes().isEmpty()) {
                        contact.setDescription(vcard.getNotes().get(0).getValue());
                    }
                    
                    // Websites and Social
                    if (vcard.getUrls() != null) {
                        for (ezvcard.property.Url url : vcard.getUrls()) {
                            String val = url.getValue();
                            if (val != null) {
                                if (val.contains("linkedin.com")) {
                                    contact.setLinkedInLink(val);
                                } else {
                                    contact.setWebsiteLink(val);
                                }
                            }
                        }
                    }
                    
                    contact.setUser(user);
                    contactService.save(contact);
                    importedCount++;
                }
            } else {
                session.setAttribute("message", Message.builder()
                        .content("Unsupported file format. Please upload a .CSV or .VCF file.")
                        .type(MessageType.red)
                        .build());
                return "redirect:/user/contacts/import-export";
            }
            
            session.setAttribute("message", Message.builder()
                    .content("Successfully imported " + importedCount + " contacts!")
                    .type(MessageType.green)
                    .build());
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", Message.builder()
                    .content("Error importing contacts: " + e.getMessage())
                    .type(MessageType.red)
                    .build());
        }
        
        return "redirect:/user/contacts/import-export";
    }

    @GetMapping("/export/csv")
    public void exportToCSV(Authentication authentication, HttpServletResponse response) {
        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"contacts.csv\"");
        
        try (PrintWriter writer = response.getWriter();
             com.opencsv.CSVWriter csvWriter = new com.opencsv.CSVWriter(writer)) {
            
            String[] header = {"Name", "Email", "Phone", "Address", "Description", "Website", "LinkedIn", "Favorite"};
            csvWriter.writeNext(header);
            
            List<Contact> contacts = contactService.getByUserId(user.getUserId());
            for (Contact contact : contacts) {
                String[] data = {
                    contact.getName() != null ? contact.getName() : "",
                    contact.getEmail() != null ? contact.getEmail() : "",
                    contact.getPhoneNumber() != null ? contact.getPhoneNumber() : "",
                    contact.getAddress() != null ? contact.getAddress() : "",
                    contact.getDescription() != null ? contact.getDescription() : "",
                    contact.getWebsiteLink() != null ? contact.getWebsiteLink() : "",
                    contact.getLinkedInLink() != null ? contact.getLinkedInLink() : "",
                    String.valueOf(contact.isFavorite())
                };
                csvWriter.writeNext(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/export/vcard")
    public void exportToVCard(Authentication authentication, HttpServletResponse response) {
        String username = Helper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);
        
        response.setContentType("text/vcard");
        response.setHeader("Content-Disposition", "attachment; filename=\"contacts.vcf\"");
        
        try (PrintWriter writer = response.getWriter()) {
            List<Contact> contacts = contactService.getByUserId(user.getUserId());
            for (Contact contact : contacts) {
                ezvcard.VCard vcard = new ezvcard.VCard();
                
                vcard.setFormattedName(contact.getName());
                
                if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                    vcard.addEmail(contact.getEmail());
                }
                
                if (contact.getPhoneNumber() != null && !contact.getPhoneNumber().trim().isEmpty()) {
                    vcard.addTelephoneNumber(contact.getPhoneNumber());
                }
                
                if (contact.getAddress() != null && !contact.getAddress().trim().isEmpty()) {
                    ezvcard.property.Address address = new ezvcard.property.Address();
                    address.setStreetAddress(contact.getAddress());
                    vcard.addAddress(address);
                }
                
                if (contact.getDescription() != null && !contact.getDescription().trim().isEmpty()) {
                    vcard.addNote(contact.getDescription());
                }
                
                if (contact.getWebsiteLink() != null && !contact.getWebsiteLink().trim().isEmpty()) {
                    vcard.addUrl(contact.getWebsiteLink());
                }
                
                if (contact.getLinkedInLink() != null && !contact.getLinkedInLink().trim().isEmpty()) {
                    vcard.addUrl(contact.getLinkedInLink());
                }
                
                String vcardStr = ezvcard.Ezvcard.write(vcard).go();
                writer.write(vcardStr);
                writer.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
