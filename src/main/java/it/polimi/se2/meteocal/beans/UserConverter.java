package it.polimi.se2.meteocal.beans;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
* Generic converter for the invitation pickList during event creation
**/
@FacesConverter(value = "userConverter")
public class UserConverter implements Converter
{
    private static final Map<Object, String> entities = new WeakHashMap<>();

    /**
    * Convert the specified model object value, which is associated with the specified UIComponent, into a String that is suitable for being included in the response generated during the Render Response phase of the request processing lifeycle.
    * @param context: FacesContext for the request being processed
    * @param component: UIComponent with which this model object value is associated
    * @param entity: Model object value to be converted (may be null) 
    * @return a zero-length String if value is null, otherwise the result of the conversion
    */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object entity)
    {
        synchronized (entities)
        {
            if(!entities.containsKey(entity))
            {
                String uuid = UUID.randomUUID().toString();
                entities.put(entity, uuid);
                return uuid;
            }
            else
            {
                return entities.get(entity);
            }
        }
    }

    /** Convert the specified string value, which is associated with the specified UIComponent, into a model data object that is appropriate for being stored during the Apply Request Values phase of the request processing lifecycle.
    * @param context: FacesContext for the request being processed
    * @param component: UIComponent with which this model object value is associated
    * @param uuid: String value to be converted (may be null) 
    * @return null if the value to convert is null, otherwise the result of the conversion 
    * */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String uuid)
    {
        for (Entry<Object, String> entry : entities.entrySet())
        {
            if (entry.getValue().equals(uuid))
            {
                return entry.getKey();
            }
        }
        return null;
    }
}
