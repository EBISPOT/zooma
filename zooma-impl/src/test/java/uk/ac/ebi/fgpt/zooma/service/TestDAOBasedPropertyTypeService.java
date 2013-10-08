package uk.ac.ebi.fgpt.zooma.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestDAOBasedPropertyTypeService {
    private PropertyDAO dao;
    private DAOBasedPropertyTypeService service;

    @Before
    public void setUp() {
        dao = mock(PropertyDAO.class);
        service = new DAOBasedPropertyTypeService();
        service.setPropertyDAO(dao);
    }

    @After
    public void tearDown() {
        service = null;
        dao = null;
    }

    @Test
    public void testGetPropertyTypes() {
        service.getPropertyTypes();
        verify(dao).readTypes();
    }

    @Test
    public void testGetPropertyTypesLimited() {
        int limit = 20;
        int start = 10;
        service.getPropertyTypes(limit, start);
        verify(dao).readTypes(limit, start);
    }
}
