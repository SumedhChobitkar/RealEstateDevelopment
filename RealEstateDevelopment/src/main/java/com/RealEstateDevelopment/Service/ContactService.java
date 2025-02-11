package com.RealEstateDevelopment.Service;

import com.RealEstateDevelopment.Entity.Contact;

import java.util.List;
import java.util.Optional;

public interface ContactService {

    public List<Contact> getAllContacts();
    public Contact saveContact(Contact contact);
    public Optional<Contact> getContactById(Long id);
    public Optional<Contact> updateContact(Long id, Contact updatedContact);
    public boolean deleteContact(Long id);
}
