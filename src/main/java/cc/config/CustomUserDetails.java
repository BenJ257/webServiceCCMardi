package cc.config;

import cc.modele.FacadeModele;
import cc.modele.data.Utilisateur;
import cc.modele.data.exceptions.UtilisateurInexistantException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomUserDetails implements UserDetailsService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FacadeModele facadeModele;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Utilisateur utilisateur = facadeModele.getUtilisateurByEmail(email);
            return User.builder()
                    .username(utilisateur.getLogin())
                    .password(utilisateur.getPassword())
                    .roles(utilisateur.getRoles())
                    .build();
        } catch (UtilisateurInexistantException e) {
            throw new UsernameNotFoundException("L'email " + email + " n'a pas été trouvé.");
        }
    }
}
