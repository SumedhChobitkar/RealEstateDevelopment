package com.RealEstateDevelopment.ServiceImpl;

import com.RealEstateDevelopment.CommanUtil.ValidationClass;
import com.RealEstateDevelopment.Entity.Contact;
import com.RealEstateDevelopment.Repository.ContactRepository;
import com.RealEstateDevelopment.Service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactServiceImpl implements ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactServiceImpl.class);
    @Autowired
    private ContactRepository contactRepository;
    @Override
    public List<Contact> getAllContacts() {
        logger.info("Fetching all contacts from the database");
        List<Contact> contacts = contactRepository.findAll();
        logger.info("Total contacts fetched: {}", contacts.size());
        return contacts;
    }
    @Override
    public Contact saveContact(Contact contact) {
        logger.info("Attempting to save contact: {}", contact);

        if (contact.getEmail() == null || !ValidationClass.EMAIL_PATTERN.matcher(contact.getEmail()).matches()) {
            logger.error("Invalid email: {}", contact.getEmail());
            throw new IllegalArgumentException("Email is not valid.");
        }
        if (contact.getMobileNo() == null || !ValidationClass.PHONE_PATTERN.matcher(contact.getMobileNo()).matches()) {
            logger.error("Invalid mobile number: {}", contact.getMobileNo());
            throw new IllegalArgumentException("Mobile number should be 10 digits.");
        }

        Contact savedContact = contactRepository.save(contact);
        logger.info("Contact saved successfully with ID: {}", savedContact.getId());
        return savedContact;
    }
    @Override
    public Optional<Contact> getContactById(Long id) {
        return contactRepository.findById(id);
    }
    @Override
    public Optional<Contact> updateContact(Long id, Contact updatedContact) {
        Optional<Contact> existingContact = contactRepository.findById(id);

        if (existingContact.isPresent()) {
            Contact contact = existingContact.get();
            contact.setMobileNo(updatedContact.getMobileNo());
            contact.setEmail(updatedContact.getEmail());
            contact.setDescription(updatedContact.getDescription());
            contactRepository.save(contact);
            return Optional.of(contact);
        } else {
            return Optional.empty();
        }
    }
    @Override
    public boolean deleteContact(Long id) {
        if (contactRepository.existsById(id)) {
            contactRepository.deleteById(id);
            return true;
        }
        return false;
    }
}



