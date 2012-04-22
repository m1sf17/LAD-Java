package lad.java;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for trainer battles.
 *
 * @author msflowers
 */
public class TrainerBattleTest
{

    public TrainerBattleTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        System.out.println( "===TrainerBattle===" );
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Tests all methods of class TrainerBattle.
     */
    @Test
    public void testAll()
    {

        Trainer trnr = new Trainer( 1 );
        ArrayList< Modifier > mods = new ArrayList<>( 1 );
        HashMap< ModifierTarget, Double > maps = new HashMap<>( 1 );

        final HashMap< Weapon, Integer > wins = new HashMap<>( 9 );
        for( Weapon w : Weapon.values() )
        {
            wins.put( w, 0 );
        }

        for( int i = 0; i < 2000; i++ )
        {
            ArenaTrainer winner = doArena( trnr, mods );
            Weapon weapon = winner.getWeapon();
            wins.put( weapon, wins.get( weapon ) + 1 );
        }

        Weapon maxweapon = null, minweapon = null;
        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
        System.out.print( "Wins: " );
        for( Weapon w : Weapon.values() )
        {
            int val = wins.get( w );
            System.out.print( val + ", " );
            if( val > max )
            {
                maxweapon = w;
                max = val;
            }
            if( val < min )
            {
                minweapon = w;
                min = val;
            }
        }
        System.out.println( "" );
        System.out.println( "Max: " + maxweapon.toString() + "(" +
                            max + ") Min: " + minweapon.toString() +
                            "(" + min + ") Diff: " + ( max - min ) );
    }

    /**
     * Runs an arena battle
     *
     * @param trnr Dummy trainer to use in battle
     * @param mods Empty mod list to use in battle
     * @return Winner of the battle
     */
    private ArenaTrainer doArena( Trainer trnr, ArrayList< Modifier > mods )
    {
        Weapon weapons[] = Weapon.values();
        Double rand = Math.random() * (double)(weapons.length);
        int index = (int)(Math.floor( rand ) );
        Weapon weapon = weapons[ index ];
        Double rand2 = Math.random() * (double)(weapons.length);
        int index2 = (int)(Math.floor( rand2 ) );
        Weapon weapon2 = weapons[ index2 ];

        TrainerBattle battle = new TrainerBattle( trnr, trnr, weapon, weapon2,
                                                  mods, mods,
                                                  weapon.getAttributes(),
                                                  weapon2.getAttributes() );
        battle.tick( 300 );
        return battle.getWinner();
    }

}
