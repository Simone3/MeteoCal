package it.polimi.se2.meteocal.beans;

import it.polimi.se2.meteocal.control.Initializer;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
* Bean that manages the log-in and log-out functions with user-name and password verification
**/
@Named
@RequestScoped
public class LoginBean
{
    private String username;
    private String password;

    @EJB
    Initializer initializer;
    
    /***************** CONSTRUCTORS *****************/
    
    /** Empty constructor **/
    public LoginBean()
    {
    }
    

    /***************** GETTERS AND SETTERS *****************/ 
    
    /**
    * Getter
    * @return the log-in user-name (= e-mail address)
    **/
    public String getUsername()
    {
        return this.username;
    }   
    
    /**
    * Setter
    * @param username: the new log-in user-name (= e-mail address)
    **/   
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    /**
    * Getter
    * @return the user password
    **/
    public String getPassword()
    {
        return this.password;
    }    
    
    /**
    * Setter
    * @param password: the new log-in password
    **/      
    public void setPassword(String password)
    {
        this.password = password;
    }
          

    /***************** BEAN METHODS *****************/ 
    
    /**
    * Method to perform the log-in: checks data and, if correct, signs the user in
    * @return web page: back to guest home if an error occurs, to the registered user home otherwise
    **/
    public String login()
    {
        // Get faces context
        FacesContext context = FacesContext.getCurrentInstance();
        
        // Error if username or password are empty
        if(username==null || "".equals(username))
        {
            FacesMessage msg = new FacesMessage("User-name cannot be empty");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(null, msg);
            return "index";
        }
        if(password==null || "".equals(password))
        {
            FacesMessage msg = new FacesMessage("Password cannot be empty");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(null, msg);
            return "index";
        }
        
        // Get request for login
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        // Try to log the user in
        try
        {
            request.login(this.username, this.password);
        }
        catch (ServletException e)
        {
            // In case of error remain on the index page and show message
            FacesMessage msg = new FacesMessage("Login failed, try again!");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            context.addMessage(null, msg);
            return "index";
        }
        
        // If login successful, call initializer
        initializer.initialize();
        
        // If login successful, redirect to the Registered User Home
        return "/user/home?faces-redirect=true";
    }
    
    /**
    * Method to perform the log-out
    * @return web page: guest home
    **/
    public String logout()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        
        // Try to log the user out
        try
        {
            request.logout();
        }
        catch (ServletException e)
        {
            context.addMessage(null, new FacesMessage("Logout failed"));
        }
        
        // If logout successful, redirect to index
        return "/index?faces-redirect=true";
    }
}