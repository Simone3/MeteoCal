package it.polimi.se2.meteocal.entity;

import it.polimi.se2.meteocal.control.PasswordEncrypter;
import java.io.Serializable;
import java.util.Set;
import static javax.persistence.CascadeType.ALL;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import static javax.persistence.GenerationType.AUTO;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotEmpty;

/**
* Entity class for a generic user
**/
@Entity
@NamedQueries
({
    @NamedQuery(name = User.findByEmail, query = "SELECT u FROM User u WHERE u.email = :email"),
    @NamedQuery(name = User.findById, query = "SELECT u FROM User u WHERE u.id = :userId"),
    @NamedQuery(name = User.findAllExcept, query = "SELECT u FROM User u WHERE u.id != :exceptId ORDER by u.lastName, u.firstName, u.email ASC")
})
@Table(name="UserTable")
public class User implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /** Identifier for the named query to get a user by email **/
    public static final String findByEmail = "User.findByEmail";
    
    /** Identifier for the named query to get a user by id  **/
    public static final String findById = "User.findById";
    
    /** Identifier of the named query to get all users except one, ordered by name **/
    public static final String findAllExcept = "User.findAllExcept";
    
    @Id
    @SequenceGenerator(name="USER_SEQ", initialValue=1, allocationSize=100)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="USER_SEQ")
    private Long id;

    @NotEmpty(message="You must write your first name")
    @Size(min=3, message="The first name must have at least 3 characters")
    private String firstName;

    @NotEmpty(message="You must write your last name")
    @Size(min=3, message="The last name must have at least 3 characters")
    private String lastName;

    @NotEmpty(message="You must write your e-mail")
    @Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message = "Invalid email")
    private String email;

    @NotEmpty(message="You must write a password")
    @Size(min=8, message="Password must have at least 8 characters")
    private String password;

    @NotEmpty
    private String userGroup;
    
    @OneToOne(optional=true, mappedBy="owner")
    private Calendar calendar;
    
    @OneToMany(cascade=ALL, mappedBy="receiver")
    private Set<Notification> notifications;
    
    @OneToMany(cascade=ALL, mappedBy="organizer")
    private Set<Event> events;
    
    
    /***************** CONSTRUCTORS *****************/
    
    /** Empty constructor **/
    public User()
    {
    }

    /** Constructor taking as parameters the basic data about the user
    * @param firstName: the user first name
    * @param lastName: the user last name
    * @param email: the user e-mail address
    * @param password: the user password in clear text, which will be stored as encrypted
    **/
    public User(String firstName, String lastName, String email, String password)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = PasswordEncrypter.encryptPassword(password);
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
    * @return user first name
    */
    public String getFirstName()
    {
        return firstName;
    }
    
    /** Setter
    * @param firstName: the new user first name
    */    
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    
    /** Getter
    * @return user last name
    */
    public String getLastName()
    {
        return lastName;
    }
    
    /** Setter
    * @param lastName: the new user last name
    */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    
    /** Getter
    * @return user e-mail address
    */
    public String getEmail()
    {
        return email;
    }
    
    /** Setter
    * @param email: the new user e-mail address
    */
    public void setEmail(String email)
    {
        this.email = email;
    }
    
    /** Getter
    * @return user password
    */
    public String getPassword()
    {
        return password;
    }
    
    /** Setter for the password. Receives the password in clear text but stores the encrypted version given by the PasswordEncrypter
    * @param password: the new user password
    */
    public void setPassword(String password)
    {
       this.password = PasswordEncrypter.encryptPassword(password);
    }
    
    /** Getter
    * @return the user's group
    */   
    public String getUserGroup()
    {
        return this.userGroup;
    }
    
    /** Setter
    * @param userGroup: the new user's group
    */   
    public void setUserGroup(String userGroup)
    {
        this.userGroup = userGroup;
    }
    
    /** Getter for the one-to-one relationship user-calendar
    * @return the user's calendar (null if no calendar has been created yet)
    */   
    public Calendar getCalendar()
    {
        return this.calendar;
    }
    
    /** Setter for the one-to-one relationship user-calendar
    * @param calendar: the new user's calendar
    */   
    public void setCalendar(Calendar calendar)
    {
        this.calendar = calendar;
    }
    
    /** Getter for the one-to-many relationship notification-user
    * @return set of all notifications received by this user
    */  
    public Set<Notification> getNotifications()
    {
        return this.notifications;
    }
    
    /** Setter for the one-to-many relationship notification-user
    * @param notifications: the set containing the notifications received by this user
    */    
    public void setNotifications(Set<Notification> notifications)
    {
        this.notifications = notifications;
    }
    
    /** "Adder" for the one-to-many relationship notification-user
    * @param notification: the notification to be added to the notifications received by this user
    */    
    public void addNotitfication(Notification notification)
    {
        this.notifications.add(notification);
    }
    
    /** Getter for the one-to-many relationship event-user
    * @return set of all events organized by this user
    */  
    public Set<Event> getOrganizedEvents()
    {
        return this.events;
    }
    
    /** Setter for the one-to-many relationship event-user
    * @param events: the set containing the events organized by this user
    */    
    public void setOrganizedEvents(Set<Event> events)
    {
        this.events = events;
    }
    
    /** "Adder" for the one-to-many relationship event-user
    * @param event: the event to be added to the events organized by this user
    */    
    public void addOrganizedEvent(Event event)
    {
        this.events.add(event);
    }
    
    /** "Remover" for the one-to-many relationship event-user
    * @param event: the event to be removed from the events organized by this user
    */    
    public void removeOrganizedEvent(Event event)
    {
        this.events.remove(event);
    }
    
    /***************** SUPPORT METHODS *****************/
    
    /**
    * Method that tells if a user owns a calendar
    * @return true if the user owns a calendar
    **/
    public boolean ownsCalendar() 
    {
        return calendar != null;
    } 
}