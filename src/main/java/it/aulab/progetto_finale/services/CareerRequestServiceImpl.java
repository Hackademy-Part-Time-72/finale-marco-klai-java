package it.aulab.progetto_finale.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.aulab.progetto_finale.models.CareerRequest;
import it.aulab.progetto_finale.models.Role;
import it.aulab.progetto_finale.models.User;
import it.aulab.progetto_finale.repositories.CareerRequestRepository;
import it.aulab.progetto_finale.repositories.RoleRepository;
import it.aulab.progetto_finale.repositories.UserRepository;

@Service
public class CareerRequestServiceImpl implements CareerRequestService {

    @Autowired
    private CareerRequestRepository careerRequestRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public boolean isRoleAlreadyAssigned(User user, CareerRequest careerRequest) {
        List<Long> allUserIds = careerRequestRepository.findAllUserIds();
        
        if (!allUserIds.contains(user.getId())) {
            return false;
        }
        
        List<Long> requestIds = careerRequestRepository.findByUserId(user.getId());
        return requestIds.stream().anyMatch(roleId -> roleId.equals(careerRequest.getRole().getId()));
    }

    @Override
    public void save(CareerRequest careerRequest, User user) {
        careerRequest.setUser(user);
        careerRequest.setIsChecked(false);
        careerRequestRepository.save(careerRequest);
        
        emailService.sendSimpleEmail("admin@aulab.it", 
            "Richiesta per ruolo: " + careerRequest.getRole().getName(), 
            "C'è una nuova richiesta di collaborazione da parte di " + user.getUsername());
    }

    @Override
    @Transactional
    public void careerAccept(Long requestId) {
        // Recupero la richiesta
        CareerRequest request = careerRequestRepository.findById(requestId).get();
        
        // Dalla richiesta estraggo l'utente richiedente ed il ruolo richiesto
        User user = request.getUser();
        Role role = request.getRole();
        
        // Recupero tutti i ruoli che l'utente già possiede ed aggiungo quello nuovo
        List<Role> rolesUser = user.getRoles();
        Role newRole = roleRepository.findByName(role.getName());
        rolesUser.add(newRole);
        
        // Salvo tutte le nuove modifiche
        user.setRoles(rolesUser);
        userRepository.save(user);
        request.setIsChecked(true);
        careerRequestRepository.save(request);
        
        emailService.sendSimpleEmail(user.getEmail(), 
            "Ruolo abilitato", 
            "Ciao, la tua richiesta di collaborazione è stata accettata dalla nostra amministrazione");
    }

    @Override
    public CareerRequest find(Long id) {
        return careerRequestRepository.findById(id).get();
    }
}