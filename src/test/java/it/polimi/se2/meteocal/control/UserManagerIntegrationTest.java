package it.polimi.se2.meteocal.control;

import it.polimi.se2.meteocal.entity.Group;
import it.polimi.se2.meteocal.entity.User;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;
import static org.hamcrest.CoreMatchers.is;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;
@Ignore
@RunWith(Arquillian.class)
public class UserManagerIntegrationTest
{
    @EJB
    UserManager userManager;

    @PersistenceContext(name="testMeteocalPU")
    EntityManager entityManager;
    
    @Deployment
    public static WebArchive createArchiveAndDeploy()
    {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(PasswordEncrypter.class)
                .addClass(UserManager.class)
                .addPackage(User.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
   
    @Test
    public void UserManagerShouldBeInjected()
    {
        assertNotNull(userManager);
    }
    
    @Test
    public void EntityManagerShouldBeInjected()
    {
        assertNotNull(entityManager);
    }

    @Test
    public void cannotSaveUserWithInvalidEmail()
    {
        String invalidEmail = "invalidemail";
        User newUser = new User("FirstName", "LastName", invalidEmail, "password");
        
        try
        {
            userManager.save(newUser);
        }
        catch (Exception e)
        {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
        }
        assertNull(userManager.findUserByEmail(invalidEmail));
    }
    
    @Test
    public void validUserShouldBeSaved()
    {
        String clearPassword = "correctpassword";
        String email = "correct@email.com";
        User newUser = new User("CorrectFirstName", "CorrectLastName", email, clearPassword);
        newUser.setUserGroup(Group.USER);
        userManager.save(newUser);
        User foundUser = userManager.findUserByEmail(email);
        
        // Check presence
        assertNotNull(foundUser);
        
        // Check that password is encrypted
        assertNotNull(foundUser.getPassword());
        assertThat(foundUser.getPassword(),is(PasswordEncrypter.encryptPassword(clearPassword)));
    }

    // Ignored, need to find a way to log-in in Arquillian...
    @Ignore
    @Test
    public void testLogin()
    {
        User newUser = new User("CorrectFirstName", "CorrectLastName", "email@email.email", "password");
        newUser.setUserGroup(Group.USER);
        userManager.save(newUser);

        /*//HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try
        {
            request.login("email@email.email", "password");
        }
        catch (ServletException e)
        {
            fail("Login error");
        }

        assertNotNull(userManager.getLoggedUser());
        assertEquals(userManager.getLoggedUser().getEmail(), "email@email.email");*/
    }
}
