package com.RealEstateDevelopment.Controller;
import com.RealEstateDevelopment.Entity.Contact;
import com.RealEstateDevelopment.Service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/contacts")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    @Autowired
    private ContactService contactService;

    @GetMapping("/getAllContact")
    public ResponseEntity<List<Contact>> getAllContacts() {
        logger.info("Received request to fetch all contacts");
        List<Contact> contacts = contactService.getAllContacts();
        logger.info("Returning {} contacts", contacts.size());
        return ResponseEntity.ok(contacts);
    }

    @PostMapping("/contactSave")
    public ResponseEntity<?> submitContact(@RequestBody Contact contact) {
        logger.info("Received request to save contact: {}", contact);
        try {
            Contact savedContact = contactService.saveContact(contact);
            logger.info("Contact saved successfully with ID: {}", savedContact.getId());
            return ResponseEntity.ok(savedContact);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred while saving contact", e);
            return ResponseEntity.internalServerError().body("An error occurred while processing your request.");
        }
    }
        @GetMapping("/contactById/{id}")
        public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
            logger.info("Fetching contact request with ID: {}", id);
            Optional<Contact> contact = contactService.getContactById(id);

            if (contact.isPresent()) {
                return ResponseEntity.ok(contact.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        @PutMapping("/updateContact/{id}")
        public ResponseEntity<Contact> updateContact(@PathVariable Long id, @RequestBody Contact updatedContact) {
            logger.info("Updating contact request with ID: {}", id);
            Optional<Contact> contact = contactService.updateContact(id, updatedContact);

            if (contact.isPresent()) {
                return ResponseEntity.ok(contact.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        @DeleteMapping("/deleteContact/{id}")
        public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
            logger.info("Deleting contact request with ID: {}", id);
            boolean deleted = contactService.deleteContact(id);

            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }

    }
}

