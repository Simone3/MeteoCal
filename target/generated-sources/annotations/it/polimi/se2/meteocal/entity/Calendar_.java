package it.polimi.se2.meteocal.entity;

import it.polimi.se2.meteocal.entity.Event;
import it.polimi.se2.meteocal.entity.User;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2015-02-13T12:27:29")
@StaticMetamodel(Calendar.class)
public class Calendar_ { 

    public static volatile SingularAttribute<Calendar, User> owner;
    public static volatile SingularAttribute<Calendar, Boolean> cloudyIsBad;
    public static volatile SingularAttribute<Calendar, Boolean> snowIsBad;
    public static volatile SingularAttribute<Calendar, Long> id;
    public static volatile SetAttribute<Calendar, Event> events;
    public static volatile SingularAttribute<Calendar, Boolean> rainIsBad;

}