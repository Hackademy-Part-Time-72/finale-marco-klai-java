package it.aulab.progetto_finale.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.progetto_finale.dtos.UserDto;
import it.aulab.progetto_finale.models.Role;
import it.aulab.progetto_finale.models.User;
import it.aulab.progetto_finale.repositories.RoleRepository;
import it.aulab.progetto_finale.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUser(UserDto userDto, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) {
        User user = new User();
        
        // Uniamo nome e cognome dal DTO
        user.setUsername(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());
        
        // Criptiamo la password prima di salvarla nel DB
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        
        // Assegniamo il ruolo di default (ROLE_USER)
        Role role = roleRepository.findByName("ROLE_USER");
        user.setRoles(List.of(role));
        
        // Salviamo l'utente
        userRepository.save(user);
    }

   @Override
    public User find(Long id) {
        return userRepository.findById(id).get();
    }
}
