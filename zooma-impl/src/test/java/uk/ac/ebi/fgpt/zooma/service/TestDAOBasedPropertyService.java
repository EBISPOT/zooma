package uk.ac.ebi.fgpt.zooma.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestDAOBasedPropertyService {
    private PropertyDAO dao;
    private DAOBasedPropertyService service;

    private URI uri;

    @Before
    public void setUp() {
        dao = mock(PropertyDAO.class);
        service = new DAOBasedPropertyService();
        service.setPropertyDAO(dao);
        uri = URI.create("http://test.org/uri");
    }

    @After
    public void tearDown() {
        service = null;
        dao = null;
    }

    @Test
    public void testGetProperties() {
        service.getProperties();
        verify(dao).read();
    }

    @Test
    public void testGetPropertiesLimited() {
        int limit = 20;
        int start = 10;
        service.getProperties(limit, start);
        verify(dao).read(limit, start);
    }

    @Test
    public void testGetProperty() {
        service.getProperty(uri);
        verify(dao).read(uri);
    }
}
