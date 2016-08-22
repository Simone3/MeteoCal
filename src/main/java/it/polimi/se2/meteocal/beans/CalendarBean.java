package it.polimi.se2.meteocal.beans;

import it.polimi.se2.meteocal.control.CalendarManager;
import it.polimi.se2.meteocal.control.EventManager;
import it.polimi.se2.meteocal.control.UserManager;
import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.User;
import javax.ejb.EJB;
import it.polimi.se2.meteocal.control.NotificationManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
* Bean that takes care of the calendar management, allowing to load the calendar table and update the event list for the selected day
**/
@ManagedBean
@ViewScoped
public class CalendarBean
{
    @EJB
    CalendarManager calendarManager;
    
    @EJB
    UserManager userManager;
    
    @EJB
    NotificationManager notificationManager;
    
    private Calendar current;
    
    @EJB
    EventManager eventManager;
    
    private final String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final java.util.Calendar calendar;
    private int fistWeekDayOfMonth;
    private int lastWeekDayOfMonth;
    
    private List<Event> eventList = null;
            
    
    /***************** CONSTRUCTORS *****************/
    
    /** Initializes the calendar to the current day **/
    public CalendarBean()
    {
        // Initialize calendar to the current date
        calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        
        // Intialize the first week day field
        this.updateFirstAndLastDaysOfMonth();
    }
    
    /**
    * Method to initialize the first day event list after the bean creation
    */
    @PostConstruct
    private void loadFirstDayEvents()
    {
        this.loadEventDetails(this.getCurrentDay());
    }
    
    /***************** GETTERS AND SETTERS *****************/
    
    /**
    * Getter
    * @return current calendar instance
    */
    public Calendar getCurrent()
    {
        if (current == null)
        {
            current = new Calendar();
        }
        return current;
    }
    
    /**
    * Setter
    * @param calendar: the new current calendar instance
    */
    public void setCurrent(Calendar calendar)
    {
        this.current = calendar;
    }
    
    /**
    * Getter
    * @return the array containing the day names (Mon, Tue, etc.)
    */
    public String[] getDayNames()
    {
        return dayNames;
    }  
    
    /**
    * Getter
    * @return the current day
    */
    public int getCurrentDay()
    {
        return calendar.get(java.util.Calendar.DAY_OF_MONTH);
    }
    
    /**
    * Getter
    * @return the current month name
    */
    public String getCurrentMonth()
    {
        return (new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calendar.getTime()));
    }
    
    /**
    * Getter
    * @return the current year
    */
    public int getCurrentYear()
    {
        return calendar.get(java.util.Calendar.YEAR);
    }   
    
    /**
    * Getter for the number of empty cells to print at the month table beginning (e.g. if the 1st day of the month is on Wednesday, it will return 2 since the first Monday and Tuesday cells must remain empty)
    * @return the starting empty cells
    */
    public int getEmptyStartingCells()
    {
        return (fistWeekDayOfMonth==java.util.Calendar.SUNDAY) ? 6 : fistWeekDayOfMonth-2;
    }
    
    /**
    * Getter for the number of empty cells to print at the month table ending (e.g. if the last day of the month is on Wednesday, it will return 4 since the last Thursday, Friday, Saturday and Sunday must remain empty)
    * @return the starting empty cells
    */
    public int getEmptyEndingCells()
    {
        return (lastWeekDayOfMonth==java.util.Calendar.SUNDAY) ? 0 : 8-lastWeekDayOfMonth;
    }
    
    /**
    * Getter
    * @return the number of days of the current month (28, 29, 30 or 31)
    */
    public int getCurrentMonthDays()
    {
        return calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
    }
    
    /**
    * Getter
    * @return the event list that is currently saved
    */   
    public List<Event> getEventList()
    {
        return eventList;
    }
    
    
    /***************** BEAN METHODS *****************/ 

    /**
    * Calls the Calendar Manager to persist the current calendar into the database
    * @return: web page that will be shown after the form submit (home)
    **/
    public String create() 
    {
        calendarManager.save(current);
        return "home?faces-redirect=true";
    }
      
    /**
    * Checks if the current user owns a calendar
    * @return true if the current user owns a calendar
    **/
    public boolean ownsCalendar() 
    {
        User owner = userManager.getLoggedUser();
        if(owner==null) return false;
        return owner.ownsCalendar();
    }   

    /**
    * Sets the current month to the next one (e.g. from March to April)
    */  
    public void nextMonth()
    {
        // Add one month
        calendar.add(java.util.Calendar.MONTH, 1);
        
        // Set current day to the 1st one
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        
        // Reload first and last week day of the month
        this.updateFirstAndLastDaysOfMonth();
        
        // Update event list
        loadEventDetails(1);
    }
    
    /**
    * Sets the current month to the previous one (e.g. from March to February)
    */ 
    public void previousMonth()
    {
        // Subtract one month
        calendar.add(java.util.Calendar.MONTH, -1);
        
        // Set current day to the 1st one
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        
        // Reload first and last week day of the month
        this.updateFirstAndLastDaysOfMonth();
        
        // Update event list
        loadEventDetails(1);
    }
    
    /**
    * Loads the event details, calling the Event Manager, and sets the given day as the current one
    * @param day: the day clicked by the user (month and year are the current ones, saved in the calendar field)
    */  
    public final void loadEventDetails(int day)
    {
        // Set current day
        calendar.set(java.util.Calendar.DAY_OF_MONTH, day);
        
        // Get the events on the selected date
        eventList = null;
        String sourceDate = day+"-"+(calendar.get(java.util.Calendar.MONTH)+1)+"-"+calendar.get(java.util.Calendar.YEAR);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Date date;
        try
        {
            date = format.parse(sourceDate);
        }
        catch (ParseException ex)
        {
            return;
        }
        eventList = eventManager.getEventsByDate(date);
    }
    
    /**
    * Gets the color of an event based on whether the user is the organizer or not
    * @param event: the event to check
    * @return the CSS color for the event
    */  
    public String getEventColor(Event event)
    {
        return (eventManager.isEventOrganizedByCurrentUser(event)) ? "black" : "gray";
    }
    
    /**
    * Checks if the user can alter (update or delete) an event
    * @param event: the event to check
    * @return true if the current user can alter the event (i.e. if it's the organizer)
    */  
    public boolean canAlterEvent(Event event)
    {
        return eventManager.canCurrentUserAlterEvent(event);
    }
    
    /**
    * Method to delete an event, just calls the delete() method in the Event Manager and updates the event details field
    * @param event: the event that needs to be deleted
    */ 
    public void deleteEvent(Event event)
    {
        // Delete event
        eventManager.delete(event);
        
        // Update event details
        this.loadEventDetails(this.getCurrentDay());
    }
    
    /**
    * Support method to update the week day for the first and the last day of the month
    */ 
    private void updateFirstAndLastDaysOfMonth()
    {
        // Backup current day
        int dayBackup = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        
        // Get week day for the first day of the current month
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        fistWeekDayOfMonth = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        
        // Get week day for the last day of the current month
        calendar.set(java.util.Calendar.DAY_OF_MONTH, this.getCurrentMonthDays());
        lastWeekDayOfMonth = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        
        // Reset current day
        calendar.set(java.util.Calendar.DAY_OF_MONTH, dayBackup);
    }
    
    /**
    * 
    * @param event: 
    * @return 
    */  
    public String getInvitedUsersList(Event event)
    {
        // Get users
        List<User> users = event.getUsersThatHaveTheEventInCalendar();
        
        // If only one user, it means that there are no invitations
        if(users.size()==1) return "Just you!";
        
        // Otherwise, print list
        else
        {
            String temp = "";
            for(int i=0; i<users.size(); i++)
            {
                if(i!=0) temp += ", ";
                temp += users.get(i).getFirstName()+" "+users.get(i).getLastName();
            }
            return temp;
        }
    }
    
    /**
    * Returns the formatted event name
    * @param event: the event to consider
    * @return the event name if the current user is the organizer, the name + by [organizer_name] otherwise
    */  
    public String getFormattedEventName(Event event)
    {
        String eventName = event.getName();
        
        if(eventManager.isEventOrganizedByCurrentUser(event))
        {
            return eventName;
        }
        else
        {
            User organizer = event.getOrganizer();
            String organizerName = organizer.getFirstName()+" "+organizer.getLastName();
            
            return eventName+" [by "+organizerName+"]";
        }
    }
}
