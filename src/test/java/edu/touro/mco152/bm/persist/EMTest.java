package edu.touro.mco152.bm.persist;

import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

class EMTest {

    /**
     * Boundary Condition: EXISTENCE
     * Tests that an entity manager will actually exist
     */
    @Test
    void getEntityManagerNotNullTest()
    {
        //arrange
        //act
        EntityManager testEM = EM.getEntityManager();

        //assert
        assertNotNull(testEM);
    }

    /**
     * Boundary Condition: CARDINALITY
     * Tests that there will ONLY ONE entity manager
     */
    @Test
    void entityManagerIsSingletonTest()
    {
        //arrange
        //act
        EntityManager testEM1 = EM.getEntityManager();
        EntityManager testEM2 = EM.getEntityManager();

        //assert
        assertSame(testEM1, testEM2);
    }

}