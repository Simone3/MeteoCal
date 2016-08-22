package it.polimi.se2.meteocal.entity;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import static javax.persistence.GenerationType.AUTO;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

/**
* Entity class for a generic calendar
**/
@Entity
public class Calendar implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    @Id
    @SequenceGenerator(name="CAL_SEQ", initialValue=1, allocationSize=100)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="CAL_SEQ")
    private Long id;

    @NotNull
    @OneToOne(optional=false)
    @JoinColumn(name="ownerId", nullable=false, updatable=false)
    private User owner;
    
    @NotNull
    private boolean rainIsBad;
    
    @NotNull
    private boolean cloudyIsBad;
        
    @NotNull
    private boolean snowIsBad;

    @ManyToMany
    @JoinTable
    (
        name = "Calendar_has_Events",
        joinColumns = {@JoinColumn(name = "calendarId", nullable = false, updatable = false)}, 
        inverseJoinColumns = {@JoinColumn(name = "eventId", nullable = false, updatable = false)}
    )
    private Set<Event> events;
    
    
    /***************** CONSTRUCTORS *****************/
   
    /** Empty constructor **/
    public Calendar()
    {
    }

    
    /***************** GETTERS AND SETTERS *****************/
    
    /** Getter
    * @return calendar id
    */
    public Long getId()
    {
        return id;
    }

    /** Setter
    * @param id: the new calendar id
    */
    public void setId(Long id)
    {
        this.id = id;
    }   
    
    /** Getter for the one-to-one relationship user-calendar
    * @return the user that owns the calendar
    */    
    public User getOwner()
    {
        return owner;
    }
    
    /** Setter for the one-to-one relationship user-calendar
    * @param owner: the new user that owns the calendar
    */
    public void setOwner(User owner)
    {
        this.owner = owner;
    }
    
    /** Getter
    * @return true if the user considers raining to be bad weather
    */    
    public boolean getRainIsBad()
    {
        return rainIsBad;
    }
    
    /** Setter
    * @param rainIsBad: true if the user considers raining to be bad weather
    */
    public void setRainIsBad(boolean rainIsBad)
    {
        this.rainIsBad = rainIsBad;
    }
    
    /** Getter
    * @return true if the user considers cloudy to be bad weather
    */    
    public boolean getCloudyIsBad()
    {
        return cloudyIsBad;
    }
    
    /** Setter
    * @param cloudyIsBad: true if the user considers cloudy to be bad weather
    */
    public void setCloudyIsBad(boolean cloudyIsBad)
    {
        this.cloudyIsBad = cloudyIsBad;
    }
    
    /** Getter
    * @return true if the user considers snowing to be bad weather
    */    
    public boolean getSnowIsBad()
    {
        return snowIsBad;
    }
    
    /** Setter
    * @param snowIsBad: true if the user considers snowing to be bad weather
    */
    public void setSnowIsBad(boolean snowIsBad)
    {
        this.snowIsBad = snowIsBad;
    }
    
    /** Getter for the many-to-many relationship calendar-event
    * @return set of all events linked to this calendar
    */  
    public Set<Event> getEvents()
    {
        return this.events;
    }
    
    /** Setter for the many-to-many relationship calendar-event
    * @param events: the set containing the events linked to this calendar
    */  
    public void setEvents(Set<Event> events)
    {
        this.events = events;
    }  
    
    /** "Adder" for the many-to-many relationship calendar-event
    * @param event: the single event to be added to the events linked to this calendar
    */ 
    public void addEvent(Event event)
    {
        this.events.add(event);
    }
    
    /** "Remover" for the many-to-many relationship calendar-event
    * @param event: the single event to be removed from the events linked to this calendar
    */ 
    public void removeEvent(Event event)
    {
        this.events.remove(event);
    }  
}