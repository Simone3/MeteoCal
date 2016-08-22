package it.polimi.se2.meteocal.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static javax.persistence.CascadeType.ALL;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import static javax.persistence.GenerationType.AUTO;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotEmpty;

/**
* Entity class for a generic event
**/
@Entity
@NamedQueries
({
    @NamedQuery(name = Event.findById, query = "SELECT e FROM Event e WHERE e.id = :id"),
    @NamedQuery(name = Event.findByDate, query = "SELECT DISTINCT e FROM Event e, Calendar c, User u WHERE e.eventDay = :date AND c.owner = :user AND c MEMBER OF e.calendars ORDER BY e.startTime ASC"),
    @NamedQuery(name = Event.findEventsForBadWeatherAlert, query = "SELECT DISTINCT e FROM Event e, Calendar c, User u WHERE e.eventDay = :date AND c.owner = :user AND c MEMBER OF e.calendars AND e.outdoor = true AND e.badWeatherAlertSent = false")
})
public class Event implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /** Identifier of the named query that gets an event by its ID **/
    public static final String findById = "Event.findById";
    
    /** Identifier of the named query that gets all events on a specific day **/
    public static final String findByDate = "Event.findByDate";
    
    /** Identifier of the named query that gets the events for the bad weather alerts **/
    public static final String findEventsForBadWeatherAlert = "Event.findEventsForBadWeatherAlert";

    @Id
    @SequenceGenerator(name="EVENT_SEQ", initialValue=1, allocationSize=100)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="EVENT_SEQ")
    private Long id;

    @NotEmpty(message="You must write a name")
    @Size(min=3, message="The name must be at least 3 characters long")
    private String name;

    @NotEmpty(message="You must select a city")
    @Size(min=3, message="The city must be at least 3 characters long")
    private String city;
    
    private String locationDetails;

    @NotNull
    private boolean outdoor;

    @NotNull(message="You must set a day")
    @Temporal(TemporalType.DATE)
    private Date eventDay;

    @NotNull(message="You must select the starting time")
    @Temporal(TemporalType.TIME)
    private Date startTime;
    
    @NotNull(message="You must select the ending time")
    @Temporal(TemporalType.TIME)
    private Date endTime;
    
    private String weatherForecast;
    
    @NotNull
    private boolean badWeatherAlertSent = false;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name="organizerId", nullable=false, updatable=false)
    private User organizer;
    
    @ManyToMany(mappedBy="events")
    private Set<Calendar> calendars;
    
    @OneToMany(cascade=ALL, mappedBy="event")
    Set<Notification> notifications;
 
    
    /***************** CONSTRUCTORS *****************/
    
    /** Empty constructor **/
    public Event()
    {
        this.calendars = new HashSet<>(0);
    }
    
    
    /***************** GETTERS AND SETTERS *****************/
    
    /** Getter
    * @return event id
    */
    public Long getId()
    {
        return id;
    }

    /** Setter
    * @param id: the new event id
    */
    public void setId(Long id)
    {
        this.id = id;
    }  
    
    /** Getter
    * @return event title
    */   
    public String getName()
    {
        return name;
    }
    
    /** Setter
    * @param name: the new event title
    */   
    public void setName(String name)
    {
        this.name = name;
    }
    
    /** Getter
    * @return the city where the event will take place
    */   
    public String getCity()
    {
        return city;
    }

    /** Setter
    * @param city: the new city where the event will take place
    */  
    public void setCity(String city)
    {
        this.city = city;
    }
    
    /** Getter
    * @return the details of the location of this event (e.g. street name, building, etc.)
    */   
    public String getLocationDetails()
    {
        return locationDetails;
    }

    /** Setter
    * @param locationDetails: the new details of the location of this event (e.g. street name, building, etc.)
    */  
    public void setLocationDetails(String locationDetails)
    {
        this.locationDetails = locationDetails;
    }
    
    /** Getter for the full location details
    * @return the full formatted place where the event takes place (city + location details)
    */   
    public String getFullLocation()
    {
        String temp = getCity();
        String details = getLocationDetails();
        if(details!=null && !"".equals(details)) temp += ", "+details;
        return temp;
    }
    
    /** Getter
    * @return true if event is outdoor
    */   
    public boolean isOutdoor()
    {
        return outdoor;
    }

    /** Setter
    * @param outdoor: the new event location description (true if outdoor)
    */     
    public void setOutdoor(boolean outdoor)
    {
        this.outdoor = outdoor;
    }
    
    /** Getter for the non-formatted event day
    * @return the event day
    */   
    public Date getDay()
    {
        return eventDay;
    }
    
    /** Getter for the formatted event day
    * @return the string representing the event day
    */   
    public String getFormattedDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");      
        return sdf.format(eventDay);
    }
    
    /** Setter
    * @param day: the new event day
    */     
    public void setDay(Date day)
    {
       this.eventDay = day;
    }
    
    /** Getter for the non-formatted start time
    * @return the time in which the event starts
    */    
    public Date getStartTime()
    {
        return this.startTime;
    }
    
    /** 
    * Getter for the formatted start time 
    * @return a string representing the start time
    */    
    public String getFormattedStartTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");     
        return sdf.format(startTime);
    }
    
    /** Setter
    * @param startTime: the new event start time
    */      
    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }
    
    /** Getter for the non-formatted end time
    * @return the time in which the event ends
    */    
    public Date getEndTime()
    {
        return this.endTime;
    }
    
    /** 
    * Getter for the formatted end time 
    * @return a string representing the end time
    */    
    public String getFormattedEndTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");     
        return sdf.format(endTime);
    }
    
    /** Setter
    * @param end: the new event end time
    */      
    public void setEndTime(Date end)
    {
        this.endTime = end;
    }
    
    /** Getter
    * @return the weather forecast for this event
    */    
    public String getWeatherForecast()
    {
        return this.weatherForecast;
    }
    
    /** Setter
    * @param weatherForecast: the new event weather forecast
    */      
    public void setWeatherForecast(String weatherForecast)
    {
        this.weatherForecast = weatherForecast;
    }
    
    /** Getter
    * @return true if the bad weather alert has been sent (to all users that have this event in the calendar)
    */   
    public boolean isBadWeatherAlertSent()
    {
        return badWeatherAlertSent;
    }

    /** Setter
    * @param badWeatherAlertSent: true if the bad weather alert has been sent (to all users that have this event in the calendar)
    */  
    public void setBadWeatherAlertSent(boolean badWeatherAlertSent)
    {
        this.badWeatherAlertSent = badWeatherAlertSent;
    }
    
    /** Getter for the one-to-many relationship event-user
    * @return the user that created the event
    */    
    public User getOrganizer()
    {
        return organizer;
    }
    
    /** Setter for the one-to-many relationship event-user
    * @param organizer: the new user that created the event
    */
    public void setOrganizer(User organizer)
    {
        this.organizer = organizer;
    }
    
    /** Getter for the many-to-many relationship calendar-event
    * @return set of all calendars that contain this event
    */  
    public Set<Calendar> getCalendars()
    {
        return this.calendars;
    }
    
    /** Setter for the many-to-many relationship calendar-event
    * @param calendars: the set containing the calendars linked to this event
    */    
    public void setCalendars(Set<Calendar> calendars)
    {
        this.calendars = calendars;
    }
    
    /** "Adder" for the many-to-many relationship calendar-event
    * @param calendar: the single calendar to be added to the calendars linked to this event
    */ 
    public void addCalendar(Calendar calendar)
    {
        this.calendars.add(calendar);
    }
    
    /** Getter for the one-to-many relationship notification-event
    * @return set of all notifications linked to this event
    */  
    public Set<Notification> getNotifications()
    {
        return this.notifications;
    }
    
    /** Setter for the one-to-many relationship notification-event
    * @param notifications: the set containing the notifications linked to this event
    */    
    public void setNotifications(Set<Notification> notifications)
    {
        this.notifications = notifications;
    }
    
    /** "Adder" for the one-to-many relationship notification-event
    * @param notification: the notification to be added to the notifications linked to this event
    */    
    public void addNotification(Notification notification)
    {
        this.notifications.add(notification);
    }
    
    /******* SUPPORT METHODS ******/
    
    
    /**
    * Simple support method that returns the list of users that have the event in their calendar
    * @return the user list
    **/
    public List<User> getUsersThatHaveTheEventInCalendar()
    {
        List<User> users = new ArrayList<>();
        for(Calendar calendar : this.getCalendars()) users.add(calendar.getOwner());
        return users;
    }
}