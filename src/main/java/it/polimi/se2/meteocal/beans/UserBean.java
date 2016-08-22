package it.polimi.se2.meteocal.beans;

import it.polimi.se2.meteocal.control.CalendarManager;
import it.polimi.se2.meteocal.control.UserManager;
import it.polimi.se2.meteocal.entity.User;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
* Bean that provides information about the user currently logged into the system
**/
@Named
@RequestScoped
public class UserBean
{
    @EJB
    UserManager userManager;
    
    @EJB
    CalendarManager calendarManager;
    
    
    /***************** CONSTRUCTORS *****************/
    
    /** Empty constructor **/
    public UserBean()
    {
    }
    
    
    /***************** BEAN METHODS *****************/ 
    
    /**
    * Check if there is the current user has logged in
    * @return true if the current user is authenticated
    **/
    public boolean isLogged()
    {
        return userManager.getLoggedUser()!=null;
    }
    
    /**
    * Getter for the currently logged in user
    * @return name of the logged user
    **/
    public String getName()
    {
        User user = userManager.getLoggedUser();
        if(user==null) return "Error! User not logged in!";
        return user.getFirstName()+" "+user.getLastName();
    }
    
    /**
    * Returns the home page link
    * @return "index" (guest user home) if the user is not logged in, "user/home" otherwise
    **/
    public String getHomePageLink()
    {
        return (this.isLogged()) ? "/user/home" : "index";
    }
}
