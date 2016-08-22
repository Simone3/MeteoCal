package it.polimi.se2.meteocal.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import static javax.persistence.GenerationType.AUTO;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

/**
* Entity class for a generic notification
**/
@Entity
@NamedQueries
({
    @NamedQuery(name = Notification.findUnreadNotifications, query = "SELECT n FROM Notification n WHERE n.isRead = false AND n.receiver.id = :receiverId ORDER BY n.sendDate DESC"),
    @NamedQuery(name = Notification.findById, query = "SELECT n FROM Notification n WHERE n.id = :notificationId")
})
public class Notification implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /** Identifier of the named query **/
    public static final String findUnreadNotifications = "Notification.findUnreadNotifications";
    
    /** Identifier of the named query **/
    public static final String findById = "Notification.findById";
    
    @Id
    @SequenceGenerator(name="NOT_SEQ", initialValue=1, allocationSize=100)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="NOT_SEQ")
    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date sendDate;

    @NotNull
    private boolean invitation;

    @NotNull
    private boolean isRead;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name="eventId", nullable=false, updatable=false)
    private Event event;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name="receiverId", nullable=false, updatable=false)
    private User receiver;
    
    
    /***************** CONSTRUCTORS *****************/
    
    /** Constructor that simply initiates the date as the current one. **/
    public Notification()
    {
        this.sendDate = new Date();
    }

    /** Constructor taking as parameters the basic data about the notification. The date is initialized as the current one.
     * @param title: the notification title
     * @param content: the notification title
     * @param invitation: true if the invitation is an invitation
     **/
    public Notification(String title, String content, boolean invitation)
    {
        this();
        
        this.title = title;
        this.content = content;
        this.invitation = invitation;
    }
    
    /***************** GETTERS AND SETTERS *****************/
    
    /** Getter
    * @return notification id
    */
    public Long getId()
    {
        return id;
    }
    
    /** Setter
    * @param id: the new notification id
    */
    public void setId(Long id)
    {
        this.id = id;
    }  
   
    /** Getter
    * @return notification title
    */
    public String getTitle()
    {
        return title;
    }
    
     /** Setter
    * @param title: the new title of the notification
    */   
    public void setTitle(String title)
    {
        this.title = title;
    }

    /** Getter
    * @return notification content
    */
    public String getContent()
    {
        return content;
    }
    
    /** Setter
    * @param content: the new content of the notification
    */
    public void setContent(String content)
    {
        this.content = content;
    }
    
    /** Getter for the non-formatted notification date
    * @return notification date (day + time)
    */
    public Date getSendDate()
    {
        return sendDate;
    }
    
    /** Getter for the formatted notification date
    * @return the string representing the notification day
    */
    public String getFormattedSendDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy, HH:mm");      
        return sdf.format(sendDate);
    }
    
    /** Setter
    * @param sendDate: the new date (day + time) of the notification
    */
    public void setSendDate(Date sendDate)
    {
        this.sendDate = sendDate;
    }
    
    /** Getter
    * @return true if the notification is an invitation
    */
    public boolean isInvitation()
    {
        return invitation;
    }
    
    /** Setter
    * @param invitation: true if the notification is an invitation
    */
    public void setInvitation(boolean invitation)
    {
       this.invitation = invitation;
    }
    /** Getter
    * @return true if the notification has been read by the receiver
    */
    public boolean isRead()
    {
        return isRead;
    }
    
    /** Setter
    * @param isRead: true if the notification has been read by the receiver
    */
    public void setRead(boolean isRead)
    {
       this.isRead = isRead;
    }
    
    /** Getter for the one-to-many relationship notification-event
    * @return the linked event
    */
    public Event getEvent()
    {
        return this.event;
    }
    
    /** Setter for the one-to-many relationship notification-event
    * @param event: the new linked event
    */
    public void setEvent(Event event)
    {
        this.event = event;
    }

    
    /** Getter for the one-to-many relationship notification-user
    * @return the user that receives the notification
    */
    public User getReceiver()
    {
        return this.receiver;
    }
    
    /** Setter for the one-to-many relationship notification-user
    * @param receiver: the new user that receives the notification
    */
    public void setReceiver(User receiver)
    {
        this.receiver = receiver;
    }
}