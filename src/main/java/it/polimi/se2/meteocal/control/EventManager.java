package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.Notification;
import it.polimi.se2.meteocal.entity.User;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
* Control class that takes care of the event management: create, delete, update and search Event entities
**/
@Stateless
public class EventManager
{
    @PersistenceContext(name = "meteocalPU")
    EntityManager entityManager;
    
    @Inject
    Principal principal;
    
    @EJB
    UserManager userManager;
    
    @EJB
    CalendarManager calendarManager;
    
    @EJB
    NotificationManager notificationManager;

    /**
    * Method to save the event in the database
    * @param event: the event that needs to be saved in the database
    **/
    public void save(Event event)
    {
        // Get current user info
        User currentUser = userManager.getLoggedUser();
        if(currentUser==null) return;
        
        // The event organizer is the current user
        event.setOrganizer(currentUser);
        currentUser.addOrganizedEvent(event);
        
        // The event belongs to the current user's calendar
        Calendar calendar = currentUser.getCalendar();
        if(calendar==null) return;
        calendar.addEvent(event);
        event.addCalendar(calendar);
          
        // Save event
        entityManager.persist(event);
    }
    
    /**
    * Method to update an event already in the database
    * @param event: the event that needs to be updated
    **/
    public void update(Event event)
    {
        // Check again that the current user can update the event, just to be sure
        if(!this.canCurrentUserAlterEvent(event)) return;
        
        // Update event
        entityManager.merge(event);
    }

    /**
    * Method to delete an event from the database, together with all its notifications
    * @param event: the event that needs to be deleted
    **/
    public void delete(Event event)
    {
        // Check again that the current user can delete the event, just to be sure
        if(!this.canCurrentUserAlterEvent(event)) return;
        
        // Get event reference
        event = entityManager.getReference(Event.class, event.getId());
        
        // Remove relationship with calendar entities
        for(Calendar calendar : event.getCalendars())
        {
            calendar.removeEvent(event);
        }
        event.setCalendars(null);
        
        // Remove relationship with user entity
        event.getOrganizer().removeOrganizedEvent(event);
        event.setOrganizer(null);
        
        // Remove relationship and delete notification entities
        Set<Notification> notifications = event.getNotifications();
        event.setNotifications(null);
        for(Notification notification : notifications)
        {
            notificationManager.delete(notification);
        }
        
        // Delete the event entity
        entityManager.remove(event);
    }
    
    /**
    * Sends invitations regarding the given event to the given users
    * @param event: the event linked to the notifications
    * @param invitedUsersList: the list of the users that need to receive the invitation
    **/
    public void sendInvitations(Event event, Set<User> invitedUsersList)
    {
        // Get current user info
        User currentUser = userManager.getLoggedUser();
        if(currentUser==null) return;
        String currentUserName = currentUser.getFirstName()+" "+currentUser.getLastName();

        // Prepare notification data
        String title = currentUserName+" invited you!";
        String content = currentUserName+" invited you to \""+event.getName()+"\" on "+event.getFormattedDate()+", from "+event.getFormattedStartTime()+" to "+event.getFormattedEndTime()+", in "+event.getFullLocation()+".";
            
        // Loop invited users
        for(User user : invitedUsersList)
        {
            // Send notification
            notificationManager.sendNotification(title, content, true, user, event);
        }
    }
    
    /**
    * Method to update a persisted Event entity with a weather forecast
    * @param forecast: the String containing the event forecast description
    * @param event: the event that needs to be updated
    **/
    public void saveWeatherForecast(String forecast, Event event)
    {
        event.setWeatherForecast(forecast);
        this.update(event);
    }
    
    /**
    * Method to update a persisted Event entity setting the boolean "badWeatherAlertSent" to true
    * @param event: the event that needs to be updated
    **/
    public void saveBadWeatherAlertSent(Event event)
    {
        event.setBadWeatherAlertSent(true);
        this.update(event);
    }
    
    /**
    * Query to get all the events at the given date for the current user
    * @param date: the day in which the events take place
    * @return the list of the events at the given date; null if none found
    **/
    public List<Event> getEventsByDate(Date date)
    {
        // Get current user info
        User currentUser = userManager.getLoggedUser();
        if(currentUser==null) return null;
        
        // Get events
        TypedQuery<Event> query1 = entityManager.createNamedQuery(Event.findByDate, Event.class);
        TypedQuery<Event> query2 = query1.setParameter("date", date).setParameter("user", currentUser);
        try
        {
            return query2.getResultList();
        }
        catch(NoResultException e)
        {
            return null;
        }
    }
    
    /**
    * Query that gets an event by its ID
    * @param id: the ID of the event
    * @return the event with the given ID if found, null otherwise
    **/
    public Event getEventById(int id)
    {
        // Get event
        TypedQuery<Event> query1 = entityManager.createNamedQuery(Event.findById, Event.class);
        TypedQuery<Event> query2 = query1.setParameter("id", id);
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
    * Similar to "getEventById" but also checks if the organizer of the event is the current user
    * @param id: the ID of the event
    * @return the event with the given ID if found and the organizer is the current user, null otherwise
    **/
    public Event getCurrentUserEventById(int id)
    {
        // Get current user
        User currentUser = userManager.getLoggedUser();
        if(currentUser==null) return null;
        
        // Get event
        Event event = this.getEventById(id);
        if(event==null) return null;
        
        // Check organizer
        if(Objects.equals(event.getOrganizer().getId(), currentUser.getId())) return event;
        else return null;
    }
    
    /**
    * Checks if the current user is the organizer of the given event
    * @param event: the event to check
    * @return true if the current user is the organizer
    **/
    public boolean isEventOrganizedByCurrentUser(Event event)
    {
        // Get current user info
        User currentUser = userManager.getLoggedUser();
        if(currentUser==null) return false;
        
        // Check if the current user is the organizer
        return (Objects.equals(currentUser.getId(), event.getOrganizer().getId()));
    }
    
    /**
    * Checks if the current user can alter (update or delete) the given event
    * @param event: the event to check
    * @return the value returned by isEventOrganizedByCurrentUser(event)
    **/
    public boolean canCurrentUserAlterEvent(Event event)
    {
        // Check if the current user is the organizer
        return this.isEventOrganizedByCurrentUser(event);
    }
    
    /**
    * Query to get all the events for bad weather alerts: finds all outdoor events in the current user's calendar that take place tomorrow
    * @return the list of the events for bad weather alerts, possibly null
    **/
    public List<Event> getEventsForBadWeatherAlerts()
    {
        // Get current user info
        User currentUser = userManager.getLoggedUser();
        if(currentUser==null) return null;
        
        // Date is tomorrow
        Date today = new Date();
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(today);
        c.add(java.util.Calendar.DATE, 1);
        Date tomorrow = c.getTime();
        
        // Get outdoor events in the current user's calendar (with no distinction about organizer or not)
        TypedQuery<Event> query1 = entityManager.createNamedQuery(Event.findEventsForBadWeatherAlert, Event.class);
        TypedQuery<Event> query2 = query1.setParameter("date", tomorrow).setParameter("user", currentUser);
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
