package it.polimi.se2.meteocal.beans;

import it.polimi.se2.meteocal.control.UserManager;
import it.polimi.se2.meteocal.entity.User;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
* Bean that provides registration functions: user data verification and creation
**/
@Named
@RequestScoped
public class RegistrationBean
{
    @EJB
    UserManager userManager;

    private User user;

    
    /***************** CONSTRUCTORS *****************/
    
    /** Empty constructor **/
    public RegistrationBean()
    {
    }

    
    /***************** GETTERS AND SETTERS *****************/ 
    
    /**
    * Getter
    * @return the user entity
    **/
    public User getUser()
    {
        if (user == null)
        {
            user = new User();
        }
        return user;
    }
    
    /**
    * Setter
    * @param user: the new user entity
    **/
    public void setUser(User user)
    {
        this.user = user;
    }

    
    /***************** BEAN METHODS *****************/ 
    
    /**
    * Method to perform the registration: checks data, sets group and saves the new user
    * @return web page: back to registration if an error occurs, to the registered user home otherwise
    **/
    public String register()
    {
        // Check if e-mail already in use
        if(!userManager.isEmailValid(user.getEmail()))
        {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage msg = new FacesMessage("E-mail already in use!");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(null, msg);
            return "registration";
        }
        
        // Set user group
        user.setUserGroup(userManager.getCorrectUserGroup());
        
        // Save the user in the database
        userManager.save(user);
        
        // Show registration successful message
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage msg = new FacesMessage("Registration successful!");
        context.addMessage(null, msg);
        context.getExternalContext().getFlash().setKeepMessages(true);
        
        // Go back to the Guest User Home
        return "index?faces-redirect=true";
    }
}