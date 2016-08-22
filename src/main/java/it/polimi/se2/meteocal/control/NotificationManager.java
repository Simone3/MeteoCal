package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.Notification;
import it.polimi.se2.meteocal.entity.User;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
* Control class that takes care of the notification management: create, delete, update and search Notification entities; management of invitation choice (accept/decline)
**/
@Stateless
public class NotificationManager
{
    @PersistenceContext(name = "meteocalPU")
    EntityManager entityManager;
    
    @Inject
    Principal principal;
    
    @EJB
    UserManager userManager;
    
    @EJB
    CalendarManager calendarManager;

    /**
    * Method to save the notification in the database
    * @param notification: the notification that needs to be saved in the database
    **/
    public void save(Notification notification)
    {
        // Save event
        entityManager.persist(notification);
    }
    
    /**
    * Query to get the unread notifications for the current user
    * @return the list of unread notifications, ordered by date, or null if none found
    **/
    public List<Notification> getCurrentUserUnreadNotifications()
    {
        // Get current user info
        User currentUser = userManager.getLoggedUser();
        if(currentUser==null) return null;
     
        // Get unread notifications
        TypedQuery<Notification> query1 = entityManager.createNamedQuery(Notification.findUnreadNotifications, Notification.class);
        TypedQuery<Notification> query2 = query1.setParameter("receiverId", currentUser.getId());
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
    * Query to get a notification by its ID
    * @param notificationId: the ID of the notification
    * @return the desired notification or null if not found
    **/
    public Notification getNotificationById(int notificationId)
    {
        TypedQuery<Notification> query1 = entityManager.createNamedQuery(Notification.findById, Notification.class);
        TypedQuery<Notification> query2 = query1.setParameter("notificationId", notificationId);
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
    * Sets a notification as read
    * @param notification: the notification that needs to be set as read
    **/
    public void setAsRead(Notification notification)
    {
        notification.setRead(true);
        entityManager.merge(notification);
    }
    
    /**
    * Manage user choice (accept or decline) for an invitation: if the user accepts, the method adds the event to the current user's calendar (if he/she owns one). In both cases it sets this notification as read and sends a response back to the organizer of the event
    * @param accept: true if the user accepts the invitation, false if he/she refuses
    * @param invitation: the invitation received
    **/
    public void manageInvitationChoice(boolean accept, Notification invitation)
    {
        // Get current user info
        User currentUser = userManager.getLoggedUser();
        if(currentUser==null) return;
        
        // Get the invitation's linked event and organizer
        Event event = invitation.getEvent();
        if(event==null) return;
        User organizer = event.getOrganizer();
        if(organizer==null) return;
        
        // Check self-invitations, just to be sure
        if(Objects.equals(currentUser.getId(), organizer.getId())) 
        {
            this.setAsRead(invitation);
            return;
        }
        
        // If the user accepts and owns a calendar...
        if(accept && currentUser.ownsCalendar())
        {
            // Add event to the current user's calendar
            Calendar calendar = currentUser.getCalendar();
            calendar.addEvent(event);
            event.addCalendar(calendar);
        }
        
        // Set invitation as read
        this.setAsRead(invitation);
        
        // Send a notification to the event organizer to inform him/her of the current user choice
        String choice = (accept) ? "accepted" : "declined";
        String currentUserName = currentUser.getFirstName()+" "+currentUser.getLastName();
        this.sendNotification(currentUserName+" "+choice+" your invitation", currentUserName+" "+choice+" your invitation for \""+event.getName()+"\"", false, organizer, event);
    }
    
    /**
    * Sends a notification to the desired user, with the given parameters
    * @param title: the title of the new notification
    * @param content: the content of the new notification
    * @param invitation: true if it's an invitation
    * @param receiver: the user that will receive the notification
    * @param linkedEvent: the event linked with the notification
    * @return the notification object that the method generates
    **/
    public Notification sendNotification(String title, String content, boolean invitation, User receiver, Event linkedEvent)
    {
        // Create notification object
        Notification notification = new Notification(title, content, invitation);
        
        // Set relationships with User and Event
        notification.setReceiver(receiver);
        receiver.addNotitfication(notification);
        notification.setEvent(linkedEvent);
        linkedEvent.addNotification(notification);
        
        // Save notification in the database
        this.save(notification);
        
        // Return the created notification
        return notification;
    }
    
    /**
    * Method to delete a notification from the database
    * @param notification: the notification that needs to be deleted
    **/
    public void delete(Notification notification)
    {
        // Get reference
        notification = entityManager.getReference(Notification.class, notification.getId());
        
        // Delete relationships
        notification.setEvent(null);
        notification.setReceiver(null);
        
        // Delete from database
        entityManager.remove(notification);
    }
}
