package it.polimi.se2.meteocal.beans;

import it.polimi.se2.meteocal.control.CalendarManager;
import it.polimi.se2.meteocal.control.EventManager;
import it.polimi.se2.meteocal.control.NotificationManager;
import it.polimi.se2.meteocal.control.UserManager;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.User;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import org.primefaces.model.DualListModel;

/**
* Abstract representation of the bean that takes care of the event form actions, containing common methods for both creation and update of events
**/
public abstract class AbstractEventBean
{
    @EJB
    EventManager eventManager;
    
    @EJB
    UserManager userManager;
    
    @EJB
    NotificationManager notificationManager;
    
    @EJB
    CalendarManager calendarManager;
    
    Event current;
    
    String componentId;
    
    DualListModel<User> usersToInvite;
    
    /***************** CONSTRUCTORS *****************/
    
    /** Empty constructor **/
    public AbstractEventBean()
    {
    }
    
    
    /***************** GETTERS AND SETTERS *****************/
    
    /**
    * Getter for the current Event instance
    * @return current event instance
    */
    public Event getCurrent()
    {
        return current;
    }
    
    /**
    * Setter for the current Event instance
    * @param event: the new current event instance
    */
    public void setCurrent(Event event)
    {
        this.current = event;
    }

    /**
    * Returns the DualListModel used for the invitation pickList
    * @return the DualListModel for the pickList
    */
    public DualListModel<User> getUsersToInvite()
    {
        if(usersToInvite==null) this.initializeInvitationList();
        return usersToInvite;
    }
    
    /**
    * Sets the DualListModel used for the invitation pickList
    * @param usersToInvite: the DualListModel for the pickList
    */
    public void setUsersToInvite(DualListModel<User> usersToInvite)
    {
        this.usersToInvite = usersToInvite;
    }
    
    
    /***************** BEAN METHODS *****************/

    /**
    * Abstract method that, in the sub-classes, will return the view title
    * @return the view title, like "Create new event" 
    */
    public abstract String getFormTitle();
    
    /**
    * Abstract method that, in the sub-classes, will manage the form submit
    * @return the web page to show to the user afterwards 
    */
    public abstract String formSubmitButton();
    
    /**
    * Abstract method that, in the sub-classes, will tell if invitations are allowed in the form
    * @return true if invitations are allowed
    */
    public abstract boolean allowInvitations();
    
    /**
    * Abstract method that, in the sub-classes, will manage the bean initialization before the view is rendered
    */
    public abstract void init();
    
    /**
    * Validator called by the view listener to validate the whole event form. Checks general constraints like end time after start time or invitation consistency. Also updates the field "invitedUsersSet" with the invited users set, used later to send the actual invitations.
    * @param systemEvent: parameter passed automatically by the listener
    */      
    public void validateEventData(ComponentSystemEvent systemEvent)
    {
        // Get context and components
        FacesContext fc = FacesContext.getCurrentInstance();
        UIComponent components = systemEvent.getComponent();

        
        /********************* DATES *********************/
        
        // Get start input
        UIInput uiInputStart = (UIInput) components.findComponent("startTime");
        if(uiInputStart==null) return;
        String start = uiInputStart.getLocalValue() == null ? "" : uiInputStart.getLocalValue().toString();
        String startId = uiInputStart.getClientId();

        // Get end input
        UIInput uiInputEnd = (UIInput) components.findComponent("endTime");
        if(uiInputEnd==null) return;
        String end = uiInputEnd.getLocalValue() == null ? "" : uiInputEnd.getLocalValue().toString();
        
        // Parse input fields as dates
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        Date startTime;
        Date endTime;
        try
        {
            startTime = sdf.parse(start);
            endTime = sdf.parse(end);
        }
        catch (ParseException ex)
        {
            return;
        }

        // Error if the start time is after or equal to the end time
        if(startTime.after(endTime) || startTime.equals(endTime))
        {
            FacesMessage msg = new FacesMessage("The starting time must be before the ending time!");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fc.addMessage(null, msg);
            fc.renderResponse();
            return;
        }
    }
    
    /**
    * Gets the weather forecast from the Calendar Manager for the current event and then saves it in the database
    */
    void setWeatherForecast()
    {
        // Get weather forecast from the Calendar Manager
        String weatherForecast;
        try
        {
            weatherForecast = calendarManager.requestWeatherForecast(current);
        }
        catch(Exception ex)
        {
            weatherForecast = "";
        }
        
        // Save weather forecast
        eventManager.saveWeatherForecast(weatherForecast, current);
    }
    
    /**
    * Initializes the DualListModel used for the invitation pickList
    */
    private void initializeInvitationList()
    {
        // Initialize the empty list of the chosen users
        List<User> invitationTarget = new ArrayList<>();
        
        // Initialize the list of the avaiable users
        List<User> invitationSource = userManager.findAllExceptCurrent();
        
        // Initialize dual list for the picker
        usersToInvite = new DualListModel<>(invitationSource, invitationTarget);
    }
}
