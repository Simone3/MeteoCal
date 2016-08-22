package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import static org.mockito.Mockito.*;
@Ignore
public class InitializerUnitTest
{
    private Initializer initializer;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private User user;
    private Calendar calendar;
    private List<Event> queryResult;
    
    @Before
    public void setUp() throws Exception
    {
        // General setup
        initializer = new Initializer();
        initializer.eventManager = spy(EventManager.class);
        initializer.notificationManager = mock(NotificationManager.class);
        initializer.userManager = initializer.eventManager.userManager = mock(UserManager.class);
        initializer.calendarManager = spy(CalendarManager.class);
        initializer.eventManager.entityManager = mock(EntityManager.class);
        
        // Mock entities
        this.resetMockedEntities();
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void testInitialize() throws Exception
    {
        /****** No events for alerts ******/
        
        // Setup
        this.resetMockedEntities();
        queryResult = new ArrayList<>();
        doReturn(queryResult).when(initializer.eventManager).getEventsForBadWeatherAlerts();
        initializer.initialize();
        
        // No alerts are sent
        verify(initializer.notificationManager,times(0)).sendNotification(anyObject(), anyObject(), anyBoolean(), anyObject(), anyObject());

        
        /****** Some events but user does not consider anything as bad weather ******/
        
        // Setup
        this.resetMockedEntities();
        queryResult = new ArrayList<>();
        this.addEventToResults("First", "Rain");
        this.addEventToResults("Second", "Snow");
        this.addEventToResults("Third", "Sunny");
        doReturn(queryResult).when(initializer.eventManager).getEventsForBadWeatherAlerts();
        initializer.initialize();
        
        // No alerts are sent
        verify(initializer.notificationManager,times(0)).sendNotification(anyObject(), anyObject(), anyBoolean(), anyObject(), anyObject());

        // All events are set as checked
        for(Event event : queryResult) assertTrue(event.isBadWeatherAlertSent());
        
        
        /****** Some events, everything is bad weather but all forecasts are good ******/
        
        // Setup
        this.resetMockedEntities();
        queryResult = new ArrayList<>();
        this.addEventToResults("First", "Clear in the morning");
        this.addEventToResults("Second", null);
        this.addEventToResults("Third", "Sunny");
        calendar.setRainIsBad(true);
        calendar.setCloudyIsBad(true);
        calendar.setSnowIsBad(true);
        doReturn(queryResult).when(initializer.eventManager).getEventsForBadWeatherAlerts();
        initializer.initialize();
        
        // No alerts are sent
        verify(initializer.notificationManager,times(0)).sendNotification(anyObject(), anyObject(), anyBoolean(), anyObject(), anyObject());

        // All events are set as checked
        for(Event event : queryResult) assertTrue(event.isBadWeatherAlertSent());
        
        
        /****** Some events and rain is bad weather ******/
        
        // Setup
        this.resetMockedEntities();
        queryResult = new ArrayList<>();
        this.addEventToResults("First", "Rain");
        this.addEventToResults("Second", "Snow");
        this.addEventToResults("Third", "Sunny");
        calendar.setRainIsBad(true);
        doReturn(queryResult).when(initializer.eventManager).getEventsForBadWeatherAlerts();
        initializer.initialize();
        
        // One alert is sent
        verify(initializer.notificationManager,times(1)).sendNotification(anyObject(), anyObject(), eq(false), anyObject(), anyObject());
        
        // All events are set as checked
        for(Event event : queryResult) assertTrue(event.isBadWeatherAlertSent());
    }

    private void resetMockedEntities() throws Exception
    {
        // Mock calendar
        calendar = new Calendar();
        calendar.setEvents(new HashSet());
        
        // Mock user
        user = new User("CorrectFirstName", "CorrectLastName", "correct@email.com", "correctpassword");
        user.setId((long)1);
        user.setOrganizedEvents(new HashSet());
        user.setCalendar(calendar);
        user.setNotifications(new HashSet());
        calendar.setOwner(user);
        when(initializer.userManager.getLoggedUser()).thenReturn(user);
        when(initializer.eventManager.userManager.getLoggedUser()).thenReturn(user);
    }
    
    private void addEventToResults(String name, String forecast) throws Exception
    {
        Event event = new Event();
        event.setName(name);
        event.setCity(name);
        event.setDay(new Date());
        event.setStartTime(timeFormat.parse("10:00"));
        event.setEndTime(timeFormat.parse("11:00"));
        event.setWeatherForecast(forecast);
        Set<Calendar> calendars = new HashSet<>();
        calendars.add(calendar);
        event.setCalendars(calendars);
        event.setOrganizer(user);
        queryResult.add(event);
    }
}
