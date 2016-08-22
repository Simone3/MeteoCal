package it.polimi.se2.meteocal.beans;

import it.polimi.se2.meteocal.control.CalendarManager;
import it.polimi.se2.meteocal.control.UserManager;
import it.polimi.se2.meteocal.entity.Notification;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import it.polimi.se2.meteocal.control.NotificationManager;
import java.util.List;

/**
* Bean that takes care of the notification management, with methods to generate the list of notification and callbacks to perform actions
**/
@Named
@RequestScoped
public class NotificationBean
{
    @EJB
    CalendarManager calendarManager;
    
    @EJB
    UserManager userManager;
    
    @EJB
    NotificationManager notificationManager;

    
    /***************** CONSTRUCTORS *****************/
    
    /** Empty constructor **/
    public NotificationBean()
    {
    }

    
    /***************** BEAN METHODS *****************/ 

    /**
    * Gets unread notifications for the current user from the Notification Manager
    * @return the notification list
    */
    public List<Notification> unreadNotificationList()
    {
        // Get and return unread notifications from the Notification Manager
        List<Notification> unreadNotifications = notificationManager.getCurrentUserUnreadNotifications();
        return unreadNotifications;
    }
    
    /**
    * Sets the notification as read, calling the method in the Notification Manager
    * @param notification: the notification to be set as read
    */
    public void setNotificationAsRead(Notification notification)
    {
        notificationManager.setAsRead(notification);
    }
    
    /**
    * Returns the notification color based on the "invitation" field
    * @param notification: the notification to check
    * @return "green" if normal notification, "blue" if invitation
    */
    public String getNotificationColor(Notification notification)
    {if(notification==null) return "AAAAAAAAAAA";
        return (notification.isInvitation()) ? "blue" : "green";
    }

    /**
    * Calls the Notification Manager method to accept an invitation
    * @param notification: the invitation to be accepted
    */
    public void acceptInvitation(Notification notification)
    {
        notificationManager.manageInvitationChoice(true, notification);
    }
    
    /**
    * Calls the Notification Manager method to decline an invitation
    * @param notification: the invitation to be declined
    */
    public void declineInvitation(Notification notification)
    {
        notificationManager.manageInvitationChoice(false, notification);
    }
}
