package it.polimi.se2.meteocal.control;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.User;
import java.security.Principal;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.json.JSONArray;
import org.json.JSONObject;

/**
* Control class that takes care of the calendar management: creation of Calendar entities and support methods for general calendar behavior, like requesting the weather forecasts
**/
@Stateless
public class CalendarManager
{
    @PersistenceContext(name = "meteocalPU")
    EntityManager entityManager;
    
    @Inject
    Principal principal;
    
    @EJB
    UserManager userManager;

    /**
    * Method to save the calendar in the database
    * @param calendar: the calendar that needs to be saved in the database
    **/
    public void save(Calendar calendar)
    {
        // Set owner equal to the current user
        User owner = userManager.getLoggedUser();
        if(owner==null) return;
        calendar.setOwner(owner);
        owner.setCalendar(calendar);
        
        // Save calendar
        entityManager.persist(calendar);
    }
    
    /**
    * Method to retrieve the weather forecast for the given event via HTTP Request
    * @param event: the event linked with the forecast
    * @return a string containing the weather forecast description
    * @throws com.mashape.unirest.http.exceptions.UnirestException if an error occurs during the HTTP request, like a network problem
    * @throws IllegalStateException if the event is not fit for a forecast (e.g. date or place missing, date too far away, etc.)
    **/
    public String requestWeatherForecast(Event event) throws Exception, UnirestException
    {
        // Get event parameters needed for the weather forecast
        String city = event.getCity();
        Date date = event.getDay();
        
        // Stop if parameters are missing
        if(city==null || "".equals(city) || date==null) throw new Exception("Param err");
        
        // Stop if the event date is too far away
        Date now = new Date();
        long difference = TimeUnit.DAYS.convert(date.getTime()-now.getTime(), TimeUnit.MILLISECONDS);
        if(difference>=7) throw new Exception("Too far away for a weather forecast");
        
        // Get weather forecast as JSON via HTTP Request
        HttpResponse<JsonNode> response = Unirest.get("https://george-vustrey-weather.p.mashape.com/api.php?location="+city)
        .header("X-Mashape-Key", "2U4bcVKdsmmshcolTeVciXb7PQimp1iSBGQjsnO4OBLADJoSpU")
        .asJson();

        // Get the array of nodes
        JSONArray array = response.getBody().getArray();

        // In the array, today is the element 0, tomorrow 1, etc. so let's check if the day we want is present
        int day = (int) difference;
        if(day<0) throw new Exception("Error! Past event!");
        if(day>=array.length()) throw new Exception("No weather forecast available");

        // Return the forecast
        JSONObject dayForecast = array.getJSONObject(day);
        return dayForecast.getString("condition");
    }

    /**
    * Checks if, given a calendar and a weather forecast, it's considered bad weather by the user
    * @param calendar: the calendar, containing the "definition" of bad weather
    * @param weatherForecast: the weather forecast
    * @return true if the weather is bad (e.g. the calendar says that rain is bad and the weather forecast says that it's gonna rain)
    **/
    public boolean isBadWeather(Calendar calendar, String weatherForecast)
    {
        if(weatherForecast==null) return false;
        
        // Check what the weather is
        boolean weatherIsCloudy = weatherForecast.toLowerCase().contains("cloud");
        boolean weatherIsRainy = weatherForecast.toLowerCase().contains("rain");
        boolean weatherIsSnowy = weatherForecast.toLowerCase().contains("snow");
        
        // Return true if the condition is considered bad by the user
        return
            (
                (weatherIsCloudy && calendar.getCloudyIsBad())
                ||
                (weatherIsRainy && calendar.getRainIsBad())
                ||
                (weatherIsSnowy && calendar.getSnowIsBad())
            );
    }
}
