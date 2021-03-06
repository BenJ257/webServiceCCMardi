package cc.modele;

import cc.modele.data.*;
import cc.modele.data.exceptions.*;
import cc.utils.EmailUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class FacadeModele {

    private List<Utilisateur> utilisateurs = new ArrayList<>();
    private List<Projet> projets = new ArrayList<>();

    /**
     * Permet d'enregistrer un utilisateur
     * @param login : login de l'utilisateur (email)
     * @param password : password chiffré de l'utilisateur
     * @return identifiant int de l'utilisateur
     * @throws DonneeManquanteException : une des données est manquante
     * @throws EmailDejaPrisException : l'email donné est déjà pris
     * @throws EmailMalFormeException : l'email donné n'est pas de la bonne forme
     */

    public int enregistrerUtilisateur(String login, String password)
            throws DonneeManquanteException, EmailDejaPrisException, EmailMalFormeException {
        if(password == null || password.isBlank() || login == null || login.isBlank()) {
            throw new DonneeManquanteException();
        }
        else if(utilisateurs.stream().anyMatch(utilisateur -> utilisateur.getLogin().equals(login))) {
            throw new EmailDejaPrisException();
        }
        else if(!EmailUtils.verifier(login)) {
            throw new EmailMalFormeException();
        }
        else {
            Utilisateur utilisateur = new Utilisateur(login, password);
            utilisateurs.add(utilisateur);
            return utilisateur.getId();
        }
    }

    /**
     * Permet de récupérer un utilisateur à partir de son identifiant int
     * @param id
     * @return utilisateur concerné
     * @throws UtilisateurInexistantException : Aucun utilisateur existe avec cet identifiant
     */
    public Utilisateur getUtilisateurByIntId(int id) throws UtilisateurInexistantException {
        return utilisateurs.stream()
                .filter(utilisateur -> utilisateur.getId() == id)
                .findFirst()
                .orElseThrow(UtilisateurInexistantException::new);
    }


    /**
     * Permet de récupérer un utilisateur à partir de son login
     * @param login
     * @return utilisateur concerné
     * @throws UtilisateurInexistantException : Aucun utilisateur existe avec cet email
     */
    public Utilisateur getUtilisateurByEmail(String login) throws UtilisateurInexistantException {
        return utilisateurs.stream()
                .filter(utilisateur -> utilisateur.getLogin().equals(login))
                .findFirst()
                .orElseThrow(UtilisateurInexistantException::new);
    }


    /**
     * Permet de réinitialiser la façade en vidant les structures
     * et en remettant les compteurs identifiants à 0
     */

    public void reInitFacade(){
        utilisateurs.clear();
        Utilisateur.resetID();
    }

    /**
     * Permet de récupérer tous les utilisateurs enregistrés
     * @return
     */
    public Collection<Utilisateur> getAllUtilisateurs() {
        return utilisateurs;
    }

    /**
     * Permet à un utilisateur (un professeur) de créer un projet
     * @param utilisateur : le contrôleur devra s'assurer qu'il s'agit d'un professeur
     * @param nomProjet : le nom du projet ne doit pas être nul ou vide
     * @param nbGroupes : le nombre de groupes doit être > 0
     * @return le projet créé
     * @throws DonneeManquanteException : le nom du projet est incorrect
     * @throws NbGroupesIncorrectException : le nombre de groupes n'est pas > 0
     */
    public Projet creationProjet(Utilisateur utilisateur,String nomProjet, int nbGroupes) throws DonneeManquanteException, NbGroupesIncorrectException {
        if(nomProjet == null || nomProjet.isBlank()) {
            throw new DonneeManquanteException();
        }
        else if(nbGroupes <= 0) {
            throw new NbGroupesIncorrectException();
        }
        else {
            Projet projet = new Projet(nomProjet, utilisateur, nbGroupes);
            projets.add(projet);
            return projet;
        }
    }

    /**
     * Permet de récupérer un projet par son identifiant s'il existe
     * @param idProjet
     * @return
     * @throws ProjetInexistantException
     */
    public Projet getProjetById(String idProjet) throws ProjetInexistantException {
        return projets.stream()
                .filter(projet -> projet.getIdProjet().equals(idProjet))
                .findFirst()
                .orElseThrow(ProjetInexistantException::new);
    }


    /**
     * Permet à utilisateur (normalement un étudiant si springboot fait bien son job)
     * de rejoindre un groupe dans un projet s'il n'est pas déjà inscrit dans un autre
     * groupe du même projet
     * @param utilisateur
     * @param idProjet
     * @param idGroupe
     * @throws ProjetInexistantException
     * @throws MauvaisIdentifiantDeGroupeException
     * @throws EtudiantDejaDansUnGroupeException
     */
    public void rejoindreGroupe(Utilisateur utilisateur, String idProjet,int idGroupe) throws ProjetInexistantException, MauvaisIdentifiantDeGroupeException, EtudiantDejaDansUnGroupeException {
        Projet projet = projets.stream()
                .filter(p -> p.getIdProjet().equals(idProjet))
                .findFirst()
                .orElseThrow(ProjetInexistantException::new);

        projet.rejoindreGroupe(utilisateur, idGroupe);
    }

    /**
     * Permet à utilisateur (normalement un étudiant si springboot fait bien son job)
     * de quitter un groupe dans un projet s'il est bien membre du groupe
     * @param utilisateur
     * @param idProjet
     * @param idGroupe
     * @throws ProjetInexistantException
     * @throws MauvaisIdentifiantDeGroupeException
     * @throws EtudiantPasDansLeGroupeException
     */
    public void quitterGroupe(Utilisateur utilisateur, String idProjet, int idGroupe) throws ProjetInexistantException, MauvaisIdentifiantDeGroupeException, EtudiantPasDansLeGroupeException {
        Projet projet = projets.stream()
                .filter(p -> p.getIdProjet().equals(idProjet))
                .findFirst()
                .orElseThrow(ProjetInexistantException::new);
        projet.quitterGroupe(utilisateur, idGroupe);
    }


    /**
     * Permet de récupérer les groupes d'un projet existant
     * @param idProjet
     * @return
     * @throws ProjetInexistantException
     */
    public Groupe[] getGroupeByIdProjet(String idProjet) throws ProjetInexistantException {

        return projets.stream()
                .filter(projet -> projet.getIdProjet().equals(idProjet))
                .findFirst()
                .orElseThrow(ProjetInexistantException::new)
                .getGroupes();
    }
}
