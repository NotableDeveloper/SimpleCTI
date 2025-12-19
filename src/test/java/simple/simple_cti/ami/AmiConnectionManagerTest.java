package simple.simple_cti.ami;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmiConnectionManagerTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ManagerConnection managerConnection;

    private AmiConnectionManager amiConnectionManager;

    @BeforeEach
    void setUp() {
        amiConnectionManager = new AmiConnectionManager(applicationContext);
        ReflectionTestUtils.setField(amiConnectionManager, "host", "localhost");
        ReflectionTestUtils.setField(amiConnectionManager, "username", "admin");
        ReflectionTestUtils.setField(amiConnectionManager, "password", "secret");
    }

    @Test
    void testConnectSuccess() {
        // Arrange
        try (MockedConstruction<ManagerConnectionFactory> mockedFactory = Mockito.mockConstruction(ManagerConnectionFactory.class,
                (mock, context) -> when(mock.createManagerConnection()).thenReturn(managerConnection))) {

            // Act
            amiConnectionManager.connect();

            // Assert
            assertEquals(1, mockedFactory.constructed().size());
            verify(managerConnection).login(); // Verify login was called
            assertNotNull(amiConnectionManager.getManagerConnection());
        } catch (Exception e) {
             throw new RuntimeException(e);
        }
    }

    @Test
    void testConnectFailure() throws Exception {
        // Arrange
        // Simulate IOException during login
        doThrow(new IOException("Connection failed")).when(managerConnection).login();

        try (MockedConstruction<ManagerConnectionFactory> mockedFactory = Mockito.mockConstruction(ManagerConnectionFactory.class,
                (mock, context) -> when(mock.createManagerConnection()).thenReturn(managerConnection));
             MockedStatic<SpringApplication> mockedSpringApplication = Mockito.mockStatic(SpringApplication.class)) {

            // Act
            amiConnectionManager.connect();

            // Assert
            assertEquals(1, mockedFactory.constructed().size());
            // Verify SpringApplication.exit was called
            mockedSpringApplication.verify(() -> SpringApplication.exit(eq(applicationContext), any()));
        }
    }
}