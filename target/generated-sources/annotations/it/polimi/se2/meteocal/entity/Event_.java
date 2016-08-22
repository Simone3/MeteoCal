package it.polimi.se2.meteocal.entity;

import it.polimi.se2.meteocal.entity.Calendar;
import it.polimi.se2.meteocal.entity.Notification;
import it.polimi.se2.meteocal.entity.User;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2015-02-13T12:27:29")
@StaticMetamodel(Event.class)
public class Event_ { 

    public static volatile SingularAttribute<Event, Boolean> badWeatherAlertSent;
    public static volatile SingularAttribute<Event, String> weatherForecast;
    public static volatile SingularAttribute<Event, String> city;
    public static volatile SingularAttribute<Event, Boolean> outdoor;
    public static volatile SingularAttribute<Event, Date> eventDay;
    public static volatile SetAttribute<Event, Calendar> calendars;
    public static volatile SingularAttribute<Event, User> organizer;
    public static volatile SingularAttribute<Event, String> name;
    public static volatile SingularAttribute<Event, String> locationDetails;
    public static volatile SingularAttribute<Event, Date> startTime;
    public static volatile SingularAttribute<Event, Long> id;
    public static volatile SingularAttribute<Event, Date> endTime;
    public static volatile SetAttribute<Event, Notification> notifications;

}