/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author carles
 */
public class PathFinderTest {
    
    public PathFinderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    

    /**
     * Test of solveMaze method, of class PathFinder.
     */
    @Test
    public void testSolveMaze() {
        System.out.println("solveMaze");
        int r1 = 16;
        int c1 = 21;
        int r2 = 18;
        int c2 = 18;
        GameSettings game = InitialGameSettings.load("game.settings");
        PathFinder instance = new PathFinder(game.getMap());
        List<String> path = instance.getPath("16;21", "18;18");
        List<String> path2 = instance.getPath2("16;21", "18;18");
        //boolean result = instance.solveMaze(r2, c2, r1, c1);
        
        assertEquals(path,path2);
    }    
}
