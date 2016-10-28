package br.com.simplepass.cadevan;

import org.junit.Test;

import br.com.simplepass.cadevan.mock.Calculator;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getFive_isCorrect() throws Exception{
        Calculator calculator = new Calculator();
        assertEquals(5, calculator.getFive());
    }
}