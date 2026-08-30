
package it.aulab.progetto_finale.services;

import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.List;

public interface CrudService<ReadDto, WriteModel, KeyType> {
    List<ReadDto> readAll();
    ReadDto read(KeyType key);
    ReadDto create(WriteModel model, Principal principal, MultipartFile file);
    ReadDto update(KeyType key, WriteModel model, MultipartFile file);
    void delete(KeyType key);
}