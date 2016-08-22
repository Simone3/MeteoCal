package it.polimi.se2.meteocal.entity;

import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.User;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2015-02-13T12:27:29")
@StaticMetamodel(Notification.class)
public class Notification_ { 

    public static volatile SingularAttribute<Notification, User> receiver;
    public static volatile SingularAttribute<Notification, Date> sendDate;
    public static volatile SingularAttribute<Notification, Boolean> invitation;
    public static volatile SingularAttribute<Notification, Boolean> isRead;
    public static volatile SingularAttribute<Notification, Long> id;
    public static volatile SingularAttribute<Notification, String> title;
    public static volatile SingularAttribute<Notification, Event> event;
    public static volatile SingularAttribute<Notification, String> content;

}