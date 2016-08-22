package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.User;
import java.security.Principal;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import static org.mockito.Mockito.*;
@Ignore
public class UserManagerUnitTest
{
    private UserManager userManager;
    private static Validator validator;
    private final transient TypedQuery<User> query = mock(TypedQuery.class);
    
    @Before
    public void setUp()
    {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        userManager = new UserManager();
        userManager.entityManager = mock(EntityManager.class);
        userManager.principal = mock(Principal.class);
    }
    
    @After
    public void tearDown()
    {
    }


    @Test
    public void testCorrectUserSave() throws Exception
    {
        Set<ConstraintViolation<User>> constraintViolations;
        
        User user = new User("CorrectFirstName", "CorrectLastName", "correct@email.com", "correctpassword");
        user.setUserGroup(userManager.getCorrectUserGroup());
        constraintViolations = validator.validate(user);
        assertEquals(0, constraintViolations.size());
        userManager.save(user);
        verify(userManager.entityManager,times(1)).persist(user);
    }
    
    @Test
    public void testWrongUserValidation() throws Exception
    {
        User user;
        Set<ConstraintViolation<User>> constraintViolations;
        
        // Invalid email
        user = new User("CorrectFirstName", "CorrectLastName", "wrongemail", "correctpassword");
        user.setUserGroup(userManager.getCorrectUserGroup());
        constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("Invalid email", constraintViolations.iterator().next().getMessage());

        // Missing group
        user = new User("CorrectFirstName", "CorrectLastName", "correct@email.com", "correctpassword");
        constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("may not be empty", constraintViolations.iterator().next().getMessage());
        
        // Empty last name
        user = new User("CorrectFirstName", null, "correct@email.com", "correctpassword");
        user.setUserGroup(userManager.getCorrectUserGroup());
        constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("You must write your last name", constraintViolations.iterator().next().getMessage());

        // Short first name
        user = new User("A", "CorrectLastName", "correct@email.com", "correctpassword");
        user.setUserGroup(userManager.getCorrectUserGroup());
        constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());
        assertEquals("The first name must have at least 3 characters", constraintViolations.iterator().next().getMessage());
    }
    
    @Test
    public void testInvalidEmail() throws Exception
    {
        User user;
        
        // Valid e-mail
        user = new User("NewFirstName", "NewLastName", "correct@email.com", "newpassword");
        user.setUserGroup(userManager.getCorrectUserGroup());
        when(userManager.entityManager.createNamedQuery(User.findByEmail, User.class)).thenReturn(query);
        when(query.setParameter("email", user.getEmail())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(null);
        assertTrue(userManager.isEmailValid(user.getEmail()));
        
        // Email already in use
        User oldUser = new User("CorrectFirstName", "CorrectLastName", "correct@email.com", "correctpassword");
        oldUser.setUserGroup(userManager.getCorrectUserGroup());
        user = new User("NewFirstName", "NewLastName", "correct@email.com", "newpassword");
        user.setUserGroup(userManager.getCorrectUserGroup());
        when(userManager.entityManager.createNamedQuery(User.findByEmail, User.class)).thenReturn(query);
        when(query.setParameter("email", user.getEmail())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(oldUser);
        assertFalse(userManager.isEmailValid(user.getEmail()));
    }
}
