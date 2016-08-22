package it.polimi.se2.meteocal.beans;

import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.User;
import java.util.HashSet;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
* Bean for the new event creation form, extending the functions of the AbstractEventBean
**/
@ManagedBean
@ViewScoped
public class NewEventBean extends AbstractEventBean
{
    /***************** CONSTRUCTORS *****************/
    
    /** Calls the AbstractEventBean constructor and sets the componentId attribute **/
    public NewEventBean()
    {
        super();
        componentId = "new";
    }
    

    /***************** BEAN METHODS *****************/
    
    /**
    * Method that saves a new event in the database: persists the event in the database, then gets the weather forecast and finally manages the invitations, if any. Note that validation is automatically performed by the bean validators, so this method is called only when the event is correct.
    * @return: web page that will be shown after the form submit: home
    **/
    public String create() 
    {
        // Save event (consistency has already been verified by the bean validators)
        eventManager.save(current);
        
        // Set weather forecast
        setWeatherForecast();
        
        // Call Event Manager to send invitations (if any)
        Set<User> invitedUsers = new HashSet<>();
        invitedUsers.addAll(usersToInvite.getTarget());
        if(invitedUsers.size()>0) eventManager.sendInvitations(current, invitedUsers);

        // Redirect to the home page
        return "home?faces-redirect=true";
    }

    /**
    * Returns the new event form title
    * @return "Create new event"
    */
    @Override
    public String getFormTitle()
    {
        return "Create new event";
    }
    
    /**
    * In the new event creation the submit button calls the create() method
    * @return what create() returns, i.e. the web page shown afterwards
    */
    @Override
    public String formSubmitButton()
    {
        return this.create();
    }
    
    /**
    * In the new event creation invitations are allowed
    * @return true
    */
    @Override
    public boolean allowInvitations()
    {
        return true;
    }
    
    /**
    * In the new event creation just creates a new "empty" event
    */
    @Override
    public void init()
    {
        // Create event
        current = new Event();
    }
}
