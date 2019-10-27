package edu.touro.mco152.bm;

import static edu.touro.mco152.bm.Util.displayString;
import static edu.touro.mco152.bm.Util.randInt;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.stream.Stream;

class UtilTest {

    /**
     * Boundary Conditions: Range
     * tests return of method stays in range of specified min and max
     *
     * @param min
     * @param max
     */
    @ParameterizedTest
    @MethodSource("provideIntPairs")
    void testRandIntWithinBounds(int min, int max)
    {
        //Arrange;
        //Act
        int rand = randInt(min, max);

        //assert
        assertTrue((min <= rand) && (rand <= max));
    }
    private static Stream<Arguments> provideIntPairs() {
        return Stream.of(
                Arguments.of(0, 50),
                Arguments.of(20, 30),
                Arguments.of(-20, 30),
                Arguments.of(Integer.MIN_VALUE, -20),
                Arguments.of(20, Integer.MAX_VALUE),
                Arguments.of(0,0)
        );
    }

    /**
     * Error Conditions: method will throw an exception if (max < min)
     */
    @Test
    void forceIllegalArgError()
    {
        //Act
        //Assert
        assertThrows(IllegalArgumentException.class, () -> randInt(10, 0));
    }

    /**
     * Right
     * Tests the rand produces different results each time
     */
    @org.junit.jupiter.api.Test
    void testRandIntIsRand()
    {
        //Arrange
        int min = 10;
        int max = 150;

        //Act
        //randInt should not produce the same result 2 times in a row, twice.
        boolean same = (randInt(min, max) == randInt(min, max) && randInt(min, max) == randInt(min, max));

        //assert
        assertFalse(same);
    }

    /**
     * This tests Performance
     * It's goal is to make sure it doesn't take
     * too long to get system info
     */
    @Test
    void sysStatsPerformanceTest()
    {
        //arrange
        long startTime = System.currentTimeMillis();
        long duration;

        //act
        Util.sysStats();
        duration = System.currentTimeMillis() - startTime;

        //assert
        assertTrue((duration > 20) && (duration < 1000));
    }


    /**
     * Cross Check
     * I wrote my own formatter which should work identically for doubles with 2 digits after the point
     *
     * @param toFormat
     */
    @ParameterizedTest
    @ValueSource(doubles = { 32.317, 3245.224, 65.967})
    void displayStringTest(double toFormat)
    {
        //Arrange
        String crossChecker = String.format("%.2f", toFormat);

        //Act
        String result = displayString(toFormat);

        //Assert
        assertEquals(crossChecker, result);
    }
}






