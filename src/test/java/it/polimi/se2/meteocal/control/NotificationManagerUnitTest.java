package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.Notification;
import it.polimi.se2.meteocal.entity.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import static org.mockito.Mockito.*;

@Ignore
public class NotificationManagerUnitTest
{
    private NotificationManager notificationManager;
    
    private Calendar senderCalendar;
    private Calendar receiverCalendar;
    private User sender;
    private User receiver;
    private Event event;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    @Before
    public void setUp() throws Exception
    {
        // General setup
        notificationManager = new NotificationManager();
        notificationManager.entityManager = mock(EntityManager.class);
        notificationManager.userManager = mock(UserManager.class);
        notificationManager.calendarManager = mock(CalendarManager.class);
        
        // Mock entities
        this.resetMockedEntities();
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void testSend() throws Exception
    {
        // Send notification
        Notification notification = notificationManager.sendNotification("CorrectTitle", "CorrectContent", false, receiver, event);
        
        // Check persistence
        verify(notificationManager.entityManager,times(1)).persist(notification);
        
        // Check correct intialization
        assertFalse(notification.isRead());
        assertEquals(notification.getTitle(), "CorrectTitle");
        assertEquals(notification.getContent(), "CorrectContent");
        assertFalse(notification.isInvitation());
        
        // Check relationship Notification-User
        assertEquals(notification.getReceiver(), receiver);
        assertEquals(receiver.getNotifications().size(), 1);
        assertTrue(receiver.getNotifications().contains(notification));
        
        // Check relationship Notification-Event
        assertEquals(notification.getEvent(), event);
        assertEquals(event.getNotifications().size(), 1);
        assertTrue(event.getNotifications().contains(notification));
    }
    
    public void resetMockedEntities() throws Exception
    {
        // Mock the calendars
        receiverCalendar = new Calendar();
        receiverCalendar.setEvents(new HashSet());
        senderCalendar = new Calendar();
        senderCalendar.setEvents(new HashSet());
        
        // Mock the users
        receiver = new User("Receiver", "Receiver", "receiver@email.com", "receiverpassword");
        receiver.setId((long) 1);
        receiver.setOrganizedEvents(new HashSet());
        receiver.setCalendar(receiverCalendar);
        receiver.setNotifications(new HashSet());
        receiverCalendar.setOwner(receiver);
        when(notificationManager.userManager.getLoggedUser()).thenReturn(receiver);
        sender = new User("Sender", "Sender", "sender@email.com", "senderpassword");
        receiver.setId((long) 2);
        sender.setOrganizedEvents(new HashSet());
        sender.setCalendar(senderCalendar);
        sender.setNotifications(new HashSet());
        senderCalendar.setOwner(sender);
        
        // Mock the event
        event = new Event();
        event.setName("CorrectName");
        event.setCity("CorrectCity");
        event.setDay(new Date());
        event.setStartTime(timeFormat.parse("10:00"));
        event.setEndTime(timeFormat.parse("11:00"));
        event.setNotifications(new HashSet());
        event.setOrganizer(sender);
        senderCalendar.addEvent(event);
    }
    
    @Test
    public void testInvitationChoice() throws Exception
    {
        Notification invitation;
        
        /****** Current user accepts and owns a calendar ******/
        
        // Send and manage choice
        invitation = notificationManager.sendNotification("CorrectTitle", "CorrectContent", true, receiver, event);
        notificationManager.manageInvitationChoice(true, invitation);
        
        // General checks
        assertTrue(invitation.isRead());
        assertEquals(sender.getNotifications().size(), 1);
        
        // Event is added to the receiver calendar
        assertEquals(receiver.getCalendar().getEvents().size(), 1);
        assertTrue(receiver.getCalendar().getEvents().contains(event));
        
        
        /****** Current user declines and owns a calendar ******/
        
        // Send and manage choice
        this.resetMockedEntities();
        invitation = notificationManager.sendNotification("CorrectTitle", "CorrectContent", true, receiver, event);
        notificationManager.manageInvitationChoice(false, invitation);
        
        // General checks
        assertTrue(invitation.isRead());
        assertEquals(sender.getNotifications().size(), 1);
        
        // Event is NOT added to the receiver calendar
        assertEquals(receiver.getCalendar().getEvents().size(), 0);
        assertFalse(receiver.getCalendar().getEvents().contains(event));
        
        
        /****** Current user accepts and DOES NOT own a calendar ******/

        // Send and manage choice
        this.resetMockedEntities();
        receiver.setCalendar(null);
        invitation = notificationManager.sendNotification("CorrectTitle", "CorrectContent", true, receiver, event);
        notificationManager.manageInvitationChoice(true, invitation);
        
        // General checks
        assertTrue(invitation.isRead());
        assertEquals(sender.getNotifications().size(), 1);
        
        // There are no changes in the calendar
        assertEquals(receiver.getCalendar(), null);
    }
}
