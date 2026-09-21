package communicationmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Refills the monster pool when it runs dry.
 *
 * Recovered by decompiling the Steam Workshop build of CommunicationMod 1.2.1,
 * which ships this class although it exists in no public commit of the upstream
 * repository. Building the mod from upstream source alone silently drops it,
 * which is why it is carried here explicitly.
 *
 * NOTE: loc is an absolute source line in AbstractDungeon, so this is pinned to
 * the 11-30-2020 game build. If ModTheSpire starts failing to patch after a game
 * update, this is the first thing to check.
 */
@SpirePatch(
        clz = AbstractDungeon.class,
        method = "getMonsterForRoomCreation"
)
public class MonsterGenerationBugPatch {

    @SpireInsertPatch(
            loc = 2342
    )
    public static void patch(AbstractDungeon __instance) {
        if (AbstractDungeon.monsterList.size() == 1) {
            try {
                Method populateMonsters = AbstractDungeon.class.getDeclaredMethod("generateStrongEnemies", Integer.TYPE);
                populateMonsters.setAccessible(true);
                populateMonsters.invoke(__instance, 12);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
