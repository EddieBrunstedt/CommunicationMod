# Building this fork

Upstream has no build instructions and its toolchain no longer works unmodified.
This is what actually builds it.

## Toolchain

The game ships a JRE, not a JDK, so the machine needs its own. Neither of these
needs root; both live outside the repo:

    ~/.local/share/sts-modkit/jdk     Temurin JDK 8  (8u504-b01)
    ~/.local/share/sts-modkit/maven   Apache Maven   (3.9.9)

## Game jars

`pom.xml` expects three system-scoped jars in `../lib`, relative to the repo.
Symlinks are fine, and preferable — `desktop-1.0.jar` is 365MB:

    mkdir -p ../lib && cd ../lib
    ln -s ~/.local/share/Steam/steamapps/common/SlayTheSpire/desktop-1.0.jar
    ln -s ~/.local/share/Steam/steamapps/workshop/content/646570/1605060445/ModTheSpire.jar
    ln -s ~/.local/share/Steam/steamapps/workshop/content/646570/1605833019/BaseMod.jar

## Build

    JAVA_HOME=~/.local/share/sts-modkit/jdk \
      ~/.local/share/sts-modkit/maven/bin/mvn -B package

Output is `target/CommunicationMod.jar` (~356KB, gson shaded into
`com.autoplay.gson`).

## Divergence from upstream

- **maven-shade-plugin 2.4.2 -> 3.6.0.** 2.4.2 dates from 2015; its bundled ASM
  cannot parse modern class files and it fails with a bare
  `IllegalArgumentException` under current Maven.

- **`MonsterGenerationBugPatch` carried explicitly.** The Steam Workshop build of
  1.2.1 ships this class, but it exists in no public commit. Building from
  upstream source alone silently drops it. Recovered by decompiling the Workshop
  jar; see the class javadoc.

- **`MapRoomNodePatch` deliberately not carried.** The Workshop build also ships
  this one, and it too is in no public commit — but it is a genuine no-op: it
  reads `animWaitTimer` into a local through reflection and discards it. Nothing
  observable depends on it, and it would add another `loc`-pinned patch to break
  on the next game update.

Both Workshop-only patches use `@SpireInsertPatch(loc = N)`, where `loc` is an
absolute source line in the target class. They are pinned to the 11-30-2020 game
build. If ModTheSpire starts failing to apply patches after a game update, look
there first.

## Verifying a build

The class list should match the Workshop jar exactly, except for the
deliberately-dropped `MapRoomNodePatch`:

    unzip -l <workshop>/CommunicationMod.jar | awk '{print $4}' \
      | grep '^communicationmod/.*\.class$' | sort > /tmp/ws.txt
    unzip -l target/CommunicationMod.jar | awk '{print $4}' \
      | grep '^communicationmod/.*\.class$' | sort > /tmp/fk.txt
    diff /tmp/ws.txt /tmp/fk.txt
