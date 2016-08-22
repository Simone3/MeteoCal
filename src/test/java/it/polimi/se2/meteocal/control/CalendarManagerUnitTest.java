package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.User;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import static org.mockito.Mockito.*;
@Ignore
public class CalendarManagerUnitTest
{
    private CalendarManager calendarManager;

    @Before
    public void setUp()
    {
        calendarManager = new CalendarManager();
        calendarManager.entityManager = mock(EntityManager.class);
        calendarManager.userManager = mock(UserManager.class);
    }
    
    @After
    public void tearDown()
    {
    }

    @Test
    public void testSaveCalendar() throws Exception
    {
        // Check creation
        Calendar calendar = new Calendar();
        User owner = new User("CorrectFirstName", "CorrectLastName", "correct@email.com", "correctpassword");
        when(calendarManager.userManager.getLoggedUser()).thenReturn(owner);
        calendarManager.save(calendar);
        verify(calendarManager.entityManager,times(1)).persist(calendar);
        
        // Check relationship User-Calendar
        assertEquals(owner, calendar.getOwner());
    }
    
    @Test
    public void testBadWeather() throws Exception
    {
        Calendar calendar = new Calendar();
        calendar.setCloudyIsBad(true);
        calendar.setRainIsBad(true);
        calendar.setSnowIsBad(true);
        
        // Sunny or no forecast
        assertFalse(calendarManager.isBadWeather(calendar, "Sunny all day"));
        assertFalse(calendarManager.isBadWeather(calendar, ""));
        assertFalse(calendarManager.isBadWeather(calendar, null));
        
        // Bad weather
        assertTrue(calendarManager.isBadWeather(calendar, "The weather will be cloudy in the morning"));
        assertTrue(calendarManager.isBadWeather(calendar, "Rain until midnight"));
        assertTrue(calendarManager.isBadWeather(calendar, "It will snow"));
        
        // Not bad weather
        calendar.setCloudyIsBad(false);
        calendar.setRainIsBad(false);
        calendar.setSnowIsBad(false);
        assertFalse(calendarManager.isBadWeather(calendar, "The weather will be cloudy in the morning"));
        assertFalse(calendarManager.isBadWeather(calendar, "Rain until midnight"));
        assertFalse(calendarManager.isBadWeather(calendar, "It will snow"));
    }
}
