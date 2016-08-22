package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.Notification;
import it.polimi.se2.meteocal.entity.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import static org.mockito.Mockito.*;
@Ignore
public class EventManagerUnitTest
{
    private EventManager eventManager;
    private static Validator validator;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    private Calendar calendar;
    private User user;
    
    @Before
    public void setUp()
    {
        // General setup
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        eventManager = new EventManager();
        eventManager.entityManager = mock(EntityManager.class);
        eventManager.userManager = mock(UserManager.class);
        eventManager.calendarManager = mock(CalendarManager.class);
        eventManager.notificationManager = mock(NotificationManager.class);
        
        // Mock the current user
        calendar = new Calendar();
        calendar.setEvents(new HashSet());
        user = new User("CorrectFirstName", "CorrectLastName", "correct@email.com", "correctpassword");
        user.setOrganizedEvents(new HashSet());
        user.setCalendar(calendar);
        calendar.setOwner(user);
        when(eventManager.userManager.getLoggedUser()).thenReturn(user);
    }
    
    @After
    public void tearDown()
    {
    }
    
    public Event getCorrectEvent() throws Exception
    {
        Event event = new Event();
        event.setName("CorrectName");
        event.setCity("CorrectCity");
        event.setDay(new Date());
        event.setStartTime(timeFormat.parse("10:00"));
        event.setEndTime(timeFormat.parse("11:00"));
        event.setNotifications(new HashSet<>());
        return event;
    }

    @Test
    public void testCorrectEventSave() throws Exception
    {
        Set<ConstraintViolation<Event>> constraintViolations;
        
        // Create event
        Event event = getCorrectEvent();
        
        // Check persistence
        eventManager.save(event);
        constraintViolations = validator.validate(event);
        assertEquals(0, constraintViolations.size());
        verify(eventManager.entityManager,times(1)).persist(event);
        
        // Check relationship User-Event
        assertEquals(event.getOrganizer(), user);
        assertEquals(user.getOrganizedEvents().size(), 1);
        assertTrue(user.getOrganizedEvents().contains(event));
        
        // Check relationship Calendar-Event
        assertEquals(calendar.getEvents().size(), 1);
        assertTrue(calendar.getEvents().contains(event));
        assertEquals(event.getCalendars().size(), 1);
        assertTrue(event.getCalendars().contains(calendar));
    }
    
    @Test
    public void testWrongEventBeanValidation() throws Exception
    {
        Event event;
        Set<ConstraintViolation<Event>> constraintViolations;
        
        // No name
        event = getCorrectEvent();
        eventManager.save(event);
        event.setName(null);
        constraintViolations = validator.validate(event);
        assertEquals(1, constraintViolations.size());
        assertEquals("You must write a name", constraintViolations.iterator().next().getMessage());
        
        // City name too short
        event = getCorrectEvent();
        eventManager.save(event);
        event.setCity("A");
        constraintViolations = validator.validate(event);
        assertEquals(1, constraintViolations.size());
        assertEquals("The city must be at least 3 characters long", constraintViolations.iterator().next().getMessage());
        
        // No date
        event = getCorrectEvent();
        eventManager.save(event);
        event.setDay(null);
        constraintViolations = validator.validate(event);
        assertEquals(1, constraintViolations.size());
        assertEquals("You must set a day", constraintViolations.iterator().next().getMessage());
        
        // No organizer
        event = getCorrectEvent();
        constraintViolations = validator.validate(event);
        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }
    
    @Test
    public void testDeleteEvent() throws Exception
    {
        // Create events
        Set<Event> events = new HashSet<>();
        Event event1 = getCorrectEvent();
        event1.setCalendars(new HashSet<>());
        events.add(event1);
        Event event2 = getCorrectEvent();
        event1.setCalendars(new HashSet<>());
        events.add(event2);
        Event event3 = getCorrectEvent();
        event1.setCalendars(new HashSet<>());
        events.add(event3);
        
        // Create some notifications
        Notification notification1 = new Notification("Title", "Content", false);
        notification1.setReceiver(user);
        notification1.setEvent(event1);
        event1.addNotification(notification1);
        Notification notification2 = new Notification("Title", "Content", true);
        notification2.setReceiver(new User());
        notification2.setEvent(event2);
        event2.addNotification(notification2);
        Notification notification3 = new Notification("Title", "Content", false);
        notification3.setReceiver(user);
        notification3.setEvent(event2);
        event2.addNotification(notification3);
        
        // Set relationships
        user.setOrganizedEvents(events);
        calendar.setEvents(events);
        for(Event event : events)
        {
            event.setOrganizer(user);
            event.addCalendar(calendar);
        }
        
        // Delete second event
        event2.setId((long)1);
        when(eventManager.entityManager.getReference(Event.class, event2.getId())).thenReturn(event2);
        eventManager.delete(event2);
        
        // Check EntityManager call
        verify(eventManager.entityManager,times(1)).remove(event2);
        
        // Check that Event-Calendar and Event-User relationships have been updated
        assertEquals(user.getOrganizedEvents().size(), 2);
        for(Event event : user.getOrganizedEvents())
        {
            if(event.equals(event2)) fail();
            else assertTrue(events.contains(event));
        }
        assertEquals(calendar.getEvents().size(), 2);
        for(Event event : calendar.getEvents())
        {
            if(event.equals(event2)) fail();
            else assertTrue(events.contains(event));
        }
        
        // Check that all notifications for event2 have been deleted
        verify(eventManager.notificationManager,times(2)).delete(any(Notification.class));
    }
    
    @Test
    public void testSendInvitations() throws Exception
    {
        // Save event
        Event event = getCorrectEvent();
        eventManager.save(event);
        
        // Create receivers
        Set<User> receivers = new HashSet<>();
        receivers.add(new User("Receiver1", "Receiver1", "rec1@a.a", "password"));
        receivers.add(new User("Receiver2", "Receiver2", "rec2@a.a", "password"));
        eventManager.sendInvitations(event, receivers);
       
        // Check invitations
        verify(eventManager.notificationManager,times(2)).sendNotification(any(String.class), any(String.class), anyBoolean() , any(User.class), any(Event.class));
    }
}
