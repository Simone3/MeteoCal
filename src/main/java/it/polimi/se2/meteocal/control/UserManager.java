package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Group;
import it.polimi.se2.meteocal.entity.User;
import java.security.Principal;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
* Control class that takes care of the user management: creation and search of User entities
**/
@Stateless
public class UserManager
{
    @PersistenceContext(name = "meteocalPU")
    EntityManager entityManager;
    
    @Inject
    Principal principal;

    /**
    * Method to save the user in the database
    * @param user: the user that needs to be saved in the database
    **/
    public void save(User user)
    {
        entityManager.persist(user);
    }

    /**
    * Method to retrieve information about the user that is currently logged into the system
    * @return data about the current user 
    **/
    public User getLoggedUser()
    {
        return this.findUserByEmail(principal.getName());
    }
    
    /**
    * Method to find a user by its ID
    * @param userId: the user ID
    * @return data about the desired user or null if not found
    **/
    public User findUserById(int userId)
    {
        TypedQuery<User> query1 = entityManager.createNamedQuery(User.findById, User.class);
        TypedQuery<User> query2 = query1.setParameter("userId", userId);
        try
        {
            return query2.getSingleResult();
        }
        catch(NoResultException e)
        {
            return null;
        }
    }
    
    /**
    * Method to find a user by its e-mail address
    * @param email: the user e-mail address
    * @return data about the desired user or null if not found
    **/
    public User findUserByEmail(String email)
    {
        TypedQuery<User> query1 = entityManager.createNamedQuery(User.findByEmail, User.class);
        TypedQuery<User> query2 = query1.setParameter("email", email);
        try
        {
            return query2.getSingleResult();
        }
        catch(NoResultException e)
        {
            return null;
        }
    }
    
    /**
    * Method to perform some verification on the given e-mail
    * @param email: the email to check
    * @return true if the e-mail is correct
    **/
    public boolean isEmailValid(String email)
    {
        // Check if email already in use
        return this.findUserByEmail(email)==null;
    }

    /**
    * Method to get the correct user group
    * @return the group "User" in the current implementation
    **/
    public String getCorrectUserGroup()
    {
        return Group.USER;
    }
    
    /**
    * Method to find all users except the currently logged one
    * @return list of all users except the current one
    **/
    public List<User> findAllExceptCurrent()
    {
        User currentUser = this.getLoggedUser();
        if(currentUser==null) return null;
        TypedQuery<User> query1 = entityManager.createNamedQuery(User.findAllExcept, User.class);
        TypedQuery<User> query2 = query1.setParameter("exceptId", currentUser.getId());
        try
        {
            return query2.getResultList();
        }
        catch(NoResultException e)
        {
            return null;
        }
    }
}
