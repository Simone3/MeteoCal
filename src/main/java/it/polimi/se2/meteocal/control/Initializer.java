package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.User;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
* Control class that takes care of preliminary actions to be performed after log-in
**/
@Stateless
public class Initializer
{
    @EJB
    NotificationManager notificationManager;

    @EJB
    UserManager userManager;
    
    @EJB
    EventManager eventManager;
    
    @EJB
    CalendarManager calendarManager;
     
    /**
    * Main method of the initializer, performs all required actions: in the current implementation just checks if there are bad weather alerts to send
    **/
    public void initialize()
    {
       // Get current user
       User currentUser = userManager.getLoggedUser();
       if(currentUser==null) return;
       
       // Get all events that need a weather alert (tomorrow and outside)
       List<Event> events = eventManager.getEventsForBadWeatherAlerts();
       
       // If no events, return
       if(events==null || events.isEmpty()) return;
       
       // Loop all events
       Set<Calendar> calendars;
       boolean isBadWeather;
       for(Event event : events)
       {
           // Get all calendars that contain this event
           calendars = event.getCalendars();

           // Loop all calendars
           for(Calendar calendar : calendars)
           {
               // Check if the weather condition is considered bad for this user
               isBadWeather = calendarManager.isBadWeather(calendar, event.getWeatherForecast());
               
               // If it's bad, send notification to the calendar owner
               if(isBadWeather)
               {
                    notificationManager.sendNotification("Bad weather tomorrow!", "Tomorrow is going to be a bad weather day and the outdoor event \""+event.getName()+"\" may be at risk!", false, calendar.getOwner(), event);
               }
           }
           
           // Update the event to say that the alerts have been sent
           eventManager.saveBadWeatherAlertSent(event);
       }
    }
}
