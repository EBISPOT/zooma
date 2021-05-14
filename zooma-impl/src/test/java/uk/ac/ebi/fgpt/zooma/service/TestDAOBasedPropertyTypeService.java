package uk.ac.ebi.fgpt.zooma.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestDAOBasedPropertyTypeService {
    private PropertyDAO dao;
    private DAOBasedPropertyTypeService service;

    @BeforeEach
    public void setUp() {
        dao = mock(PropertyDAO.class);
        service = new DAOBasedPropertyTypeService();
        service.setPropertyDAO(dao);
    }

    @AfterEach
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
