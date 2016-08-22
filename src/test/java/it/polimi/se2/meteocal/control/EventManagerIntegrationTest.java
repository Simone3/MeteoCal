package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Group;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.Ignore;
@Ignore
@RunWith(Arquillian.class)
public class EventManagerIntegrationTest
{
    @EJB
    EventManager eventManager;
    
    @EJB
    UserManager userManager;
        
    @EJB
    CalendarManager calendarManager;
    
    @PersistenceContext(name="testMeteocalPU")
    EntityManager entityManager;
    
    @Ignore
    @Deployment
    public static WebArchive createArchiveAndDeploy()
    {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(Event.class.getPackage())
                
                .addClass(UserManager.class)
                .addClass(EventManager.class)
                .addClass(PasswordEncrypter.class)
                .addClass(CalendarManager.class)
                .addClass(NotificationManager.class)
                
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
   
    @Test
    public void EventManagerShouldBeInjected()
    {
        assertNotNull(eventManager);
    }
    
    @Test
    public void EntityManagerShouldBeInjected()
    {
        assertNotNull(entityManager);
    }
    
    private Event saveEvent(Date date, boolean outdoor, User organizer, boolean alertSent)
    {
        // Create entity
        Event event = new Event();
        event.setName("Event");
        event.setCity("Milano");
        event.setDay(date);
        event.setOutdoor(outdoor);
        event.setBadWeatherAlertSent(alertSent);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        try
        {
            event.setStartTime(timeFormat.parse("10:00"));
            event.setEndTime(timeFormat.parse("11:00"));
        }
        catch(Exception e)
        {
            fail("error event creation");
        }
        
        // Setup relationships
        event.setOrganizer(organizer);
        organizer.addOrganizedEvent(event);
        event.addCalendar(organizer.getCalendar());
        organizer.getCalendar().addEvent(event);
        
        // Save event
        eventManager.save(event);
        
        // Return event
        return event;
    }

    // Ignored, need to find a way to log-in in Arquillian...
    @Ignore
    @Test
    public void testGetEventsForBadWeatherAlerts()
    {
        // Create users
        User firstUser = new User("First", "First", "first@first.com", "password");
        firstUser.setUserGroup(Group.USER);
        userManager.save(firstUser);
        User secondUser = new User("Second", "Second", "second@second.com", "password");
        secondUser.setUserGroup(Group.USER);
        userManager.save(secondUser);
        
        // Create calendars
        Calendar firstCalendar = new Calendar();
        firstCalendar.setOwner(firstUser);
        firstUser.setCalendar(firstCalendar);
        firstCalendar.setEvents(new HashSet<>());
        firstUser.setOrganizedEvents(new HashSet<>());
        calendarManager.save(firstCalendar);
        Calendar secondCalendar = new Calendar();
        secondCalendar.setOwner(secondUser);
        secondUser.setCalendar(secondCalendar);
        secondCalendar.setEvents(new HashSet<>());
        secondUser.setOrganizedEvents(new HashSet<>());
        calendarManager.save(secondCalendar);
       
        //
        // TODO need to login firstUser here, otherwise the query fails

        // Get dates
        Date today = new Date();
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(today);
        c.add(java.util.Calendar.DATE, 1);
        Date tomorrow = c.getTime();
        c.add(java.util.Calendar.DATE, 1);
        Date dayAfterTomorrow = c.getTime();
        
        // Insert some events
        Event correctEvent = saveEvent(tomorrow, true, firstUser, false);
        saveEvent(tomorrow, true, firstUser, true);
        saveEvent(tomorrow, false, firstUser, false);
        saveEvent(today, true, firstUser, false);
        saveEvent(dayAfterTomorrow, true, firstUser, false);
        saveEvent(tomorrow, false, secondUser, false);
        saveEvent(tomorrow, true, secondUser, false);
        
        // Query
        List<Event> events = eventManager.getEventsForBadWeatherAlerts();
        
        // Check
        assertNotNull(events);
        assertEquals(events.size(), 1);
        assertEquals(events.iterator().next(), correctEvent);
    }
}
