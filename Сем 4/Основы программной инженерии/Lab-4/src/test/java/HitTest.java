import org.junit.Test;

import static io.github.Lab3.model.AreaResultChecker.getResult;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class HitTest {
    @Test
    public void hitArea() {
        assertTrue(getResult(1, -1, 2));
        assertTrue(getResult(1, -0.5, 2));
        assertFalse(getResult(1, -1, 0.1));
        assertFalse(getResult(1, -5, 2));
    }

    @Test
    public void hitTriangle() {
        // Test cases where the attempt should hit within the triangle
        assertTrue(getResult(-1, -1, 3));
        assertTrue(getResult(-1, -0.5, 3));
        assertTrue(getResult(-5, 0, 5));

        // Test cases where the attempt should miss within the triangle
        assertFalse(getResult(1, -5, 2));
        assertFalse(getResult(-4, -1, 1));
    }

    @Test
    public void hitRectangle() {
        // Test cases where the attempt should hit within the rectangle
        assertTrue(getResult(1, 1, 2));
        assertTrue(getResult(2, 1, 3));
        assertTrue(getResult(5, 2.5, 5));

        // Test cases where the attempt should miss within the rectangle
        assertFalse(getResult(1, 4, 2));
        assertFalse(getResult(4, 1, 1));
    }

    @Test
    public void hitSector() {
        // Test cases where the attempt should hit within the sector
        assertTrue(getResult(1, -0.2, 4));
        assertTrue(getResult(2, -1, 3));
        assertTrue(getResult(0.1, -4, 5));

        // Test cases where the attempt should miss within the sector
        assertFalse(getResult(1, -3, 2));
        assertFalse(getResult(5, -1, 3));
    }
}