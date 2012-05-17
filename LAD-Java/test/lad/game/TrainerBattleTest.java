package lad.game;

import java.util.ArrayList;
import java.util.HashMap;
import lad.data.Modifier;
import lad.data.ModifierTarget;
import lad.data.Trainer;
import lad.data.Weapon;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for trainer battles.
 *
 * @author msflowers
 */
public class TrainerBattleTest
{
    /**
     * Sets up the class before all the tests
     */
    @BeforeClass
    public static void setUpClass()
    {
        System.out.println( "===TrainerBattle===" );
    }

    /**
     * Tests all methods of class TrainerBattle.
     */
    @Test
    public void testAll()
    {
        Trainer trnr = new Trainer( true );
        ArrayList< Modifier > mods = new ArrayList<>( 1 );

        final HashMap< Weapon, Integer > wins = new HashMap<>( 9 );
        for( Weapon w : Weapon.values() )
        {
            wins.put( w, 0 );
        }

        for( int i = 0; i < 1000; i++ )
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
        Weapon weapon = Weapon.getRandom(), weapon2 = Weapon.getRandom();
        HashMap< ModifierTarget, Double > maps = new HashMap<>( 1 );

        TrainerBattle battle = new TrainerBattle( trnr, trnr, weapon, weapon2,
                                                  mods, mods, maps, maps );
        battle.tick( 300 );
        return battle.getWinner();
    }

}
