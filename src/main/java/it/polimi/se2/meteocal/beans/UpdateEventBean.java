package it.polimi.se2.meteocal.beans;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

/**
* Bean for the existing event update, extending the functions of the AbstractEventBean
**/
@ManagedBean
@ViewScoped
public class UpdateEventBean extends AbstractEventBean
{
    private Date previousDate;
    
    
    /***************** CONSTRUCTORS *****************/
    
    /** Calls the AbstractEventBean constructor and sets the componentId attribute **/
    public UpdateEventBean()
    {
        super();
        componentId = "update";
    }
    
    
    /***************** BEAN METHODS *****************/
    
    /**
    * Method that updates an existing event in the database: persists the event in the database and then gets the updated weather forecast. Note that validation is automatically performed by the bean validators, so this method is called only when the event is correct.
    * @return: web page that will be shown after the form submit: home if everything is OK, update_event if errors occur
    **/
    public String update() 
    {
        // If the date changes, reset bad weather alert sent flag
        if(!previousDate.equals(current.getDay())) current.setBadWeatherAlertSent(false);
        
        // Save event
        eventManager.update(current);
        
        // Set weather forecast
        setWeatherForecast();

        // Redirect to the home page
        return "home?faces-redirect=true";
    }
    
    /**
    * Returns the update event form title
    * @return "Update event EVENT_NAME"
    */
    @Override
    public String getFormTitle()
    {
        return "Update event \""+current.getName()+"\"";
    }
    
    /**
    * In the event update, the submit button calls the update() method
    * @return what update() returns, i.e. the web page shown afterwards
    */
    @Override
    public String formSubmitButton()
    {
        return this.update();
    }
    
    /**
    * In the event update, invitations are NOT allowed
    * @return false
    */
    @Override
    public boolean allowInvitations()
    {
        return false;
    }
    
    /**
    * In the event update, gets the event with the ID given in the request, showing an error if not found or the user is not the organizer
    */
    @Override
    public void init()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        
        // Get event parameter
        Map<String, String> parameterMap = (Map<String, String>) context.getExternalContext().getRequestParameterMap();
        String eventIdParam = parameterMap.get("event");
        
        // If parameter is empty, show error
        if(eventIdParam==null || "".equals(eventIdParam))
        {
            try
            {
                context.getExternalContext().responseSendError(404, "Error! No event specified!");
            }
            catch (IOException ex)
            {
                return;
            }
            context.responseComplete();
        }
        else
        {
            // Search event
            int eventId = Integer.parseInt(eventIdParam);
            current = eventManager.getCurrentUserEventById(eventId);

            // If none found, show error
            if(current==null)
            {
                try
                {
                    context.getExternalContext().responseSendError(404, "Error! Event not found!");
                }
                catch (IOException ex)
                {
                    return;
                }
                context.responseComplete();
            }
            
            // Save previous date
            previousDate = current.getDay();
        }
    }
}
