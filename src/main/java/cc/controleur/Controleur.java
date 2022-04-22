package cc.controleur;

import cc.modele.*;
import cc.modele.data.*;
import cc.modele.data.exceptions.*;
import cc.utils.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class Controleur {
    final
    FacadeModele facadeModele;

    public Controleur(FacadeModele facadeModele) {
        this.facadeModele = facadeModele;
    }

    @PostMapping("/gestionprojets/utilisateurs")
    public ResponseEntity<Utilisateur> inscription(@RequestBody UtilisateurDTO utilisateurDTO) {
        try {
            int idUtilisateur = facadeModele.enregistrerUtilisateur(utilisateurDTO.getLogin(), utilisateurDTO.getPassword());
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{id}")
                    .buildAndExpand(idUtilisateur).toUri();
            return ResponseEntity.created(location).body(facadeModele.getUtilisateurByIntId(idUtilisateur));
            //return ResponseEntity.created(location).body(utilisateurDTO);
        } catch (DonneeManquanteException | EmailMalFormeException e) {
            return ResponseEntity.status(406).build();
        } catch (EmailDejaPrisException e) {
            return ResponseEntity.status(409).build();
        } catch (UtilisateurInexistantException e) {
            //pas sûr de ouf
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/gestionprojets/utilisateurs/{idUtilisateur}")
    public ResponseEntity<Utilisateur> getUtilisateur(@PathVariable int idUtilisateur, Principal principal) {
        try {
            String email = principal.getName();
            int id = facadeModele.getUtilisateurByEmail(email).getId();
            Utilisateur utilisateurPrincipal = facadeModele.getUtilisateurByEmail(email);

            boolean prof = false;
            for(String role : utilisateurPrincipal.getRoles()) {
                if(role.equals("PROFESSEUR")) {
                    prof = true;
                }
            }


            if(id == idUtilisateur || prof) {
                Utilisateur utilisateur = facadeModele.getUtilisateurByIntId(idUtilisateur);
                return ResponseEntity.ok(utilisateur);
            }
            else {
                return ResponseEntity.status(403).build();
            }
        } catch (UtilisateurInexistantException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/gestionprojets/utilisateurs")
    public ResponseEntity<Collection<Utilisateur>> getTousUtilisateurs() {
        return ResponseEntity.ok(facadeModele.getAllUtilisateurs());
    }

    @PostMapping("/gestionprojets/projets")
    public ResponseEntity<Projet> creerProjet(@RequestParam String nomProjet, @RequestParam int nbGroupes, Principal principal) {
        try {
            Utilisateur utilisateur = facadeModele.getUtilisateurByEmail(principal.getName());
            Projet projet = facadeModele.creationProjet(utilisateur, nomProjet, nbGroupes);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("{id}")
                    .buildAndExpand(projet.getIdProjet()).toUri();
            return ResponseEntity.created(location).body(projet);
        } catch (UtilisateurInexistantException e) {
            return ResponseEntity.status(403).build();
        } catch (DonneeManquanteException | NbGroupesIncorrectException e) {
            return ResponseEntity.status(406).build();
        }
    }

    @GetMapping("/gestionprojets/projets/{idprojet}")
    public ResponseEntity<Projet> getProjet(@PathVariable String idprojet) {
        try {
            Projet projet = facadeModele.getProjetById(idprojet);
            return ResponseEntity.ok(projet);
        } catch (ProjetInexistantException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @GetMapping("/gestionprojets/projets/{idprojet}/groupes")
    public ResponseEntity<Groupe[]> getGroupes(@PathVariable String idprojet) {
        try {
            Groupe[] groupes = facadeModele.getGroupeByIdProjet(idprojet);
            return ResponseEntity.ok(groupes);
        } catch (ProjetInexistantException e) {
            return ResponseEntity.status(404).build();
        }
    }
    
    @PutMapping("/gestionprojets/projets/{idProjet}/groupes/{idGroupe}")
    public ResponseEntity<HttpStatus> rejoindreGroupe(@PathVariable String idProjet, @PathVariable int idGroupe, Principal principal) {
        try {
            String email = principal.getName();
            Utilisateur utilisateur = facadeModele.getUtilisateurByEmail(email);
            facadeModele.rejoindreGroupe(utilisateur, idProjet, idGroupe);
            return ResponseEntity.status(202).build();
        } catch (ProjetInexistantException | UtilisateurInexistantException | MauvaisIdentifiantDeGroupeException e) {
            return ResponseEntity.status(404).build();
        } catch (EtudiantDejaDansUnGroupeException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @DeleteMapping("/gestionprojets/projets/{idProjet}/groupes/{idGroupe}")
    public ResponseEntity<String> quitterGroupe(@PathVariable String idProjet, @PathVariable int idGroupe, Principal principal) {
        try {
            Utilisateur utilisateur = facadeModele.getUtilisateurByEmail(principal.getName());
            facadeModele.quitterGroupe(utilisateur, idProjet, idGroupe);
            return ResponseEntity.status(202).build();
        } catch (UtilisateurInexistantException | MauvaisIdentifiantDeGroupeException | ProjetInexistantException e) {
            return ResponseEntity.status(404).build();
        } catch (EtudiantPasDansLeGroupeException e) {
            return ResponseEntity.status(406).build();
        }
    }
}
