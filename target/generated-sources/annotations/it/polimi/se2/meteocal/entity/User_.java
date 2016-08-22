package it.polimi.se2.meteocal.entity;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.Notification;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2015-02-13T12:27:29")
@StaticMetamodel(User.class)
public class User_ { 

    public static volatile SingularAttribute<User, Calendar> calendar;
    public static volatile SingularAttribute<User, String> lastName;
    public static volatile SingularAttribute<User, String> firstName;
    public static volatile SingularAttribute<User, String> password;
    public static volatile SingularAttribute<User, Long> id;
    public static volatile SingularAttribute<User, String> userGroup;
    public static volatile SingularAttribute<User, String> email;
    public static volatile SetAttribute<User, Notification> notifications;
    public static volatile SetAttribute<User, Event> events;

}