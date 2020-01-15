/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.utils.bukkit.world;

import com.google.gson.JsonObject;

/**
 * Helper class which aids in the creation of the Minecraft world generation custom settings string.
 * The world type must be set to {@link org.bukkit.WorldType#CUSTOMIZED} in order for these options
 * to be applied.
 *
 * @author Austin Mayes
 */
public class GeneratorSettingsBuilder {

    // World Height Limiting Options
    private double coordinateScale, heightScale, lowerLimitScale, upperLimitScale;
    // Depth Noise Options
    private double depthNoiseScaleX, depthNoiseScaleZ, depthNoiseScaleExponent;
    // Main Noise Options
    private double mainNoiseScaleX, mainNoiseScaleY, mainNoiseScaleZ;
    // World Stretch Options
    private double baseSize, stretchY;
    // Biome Options
    private double biomeDepthWeight, biomeDepthOffset, biomeScaleWeight, biomeScaleOffset;
    // Sea Options
    private int seaLevel;
    // Underground Generation Toggles
    private boolean useCaves, useDungeons, useStrongholds, useMineShafts, useRavines;
    // Liquid Generation Toggles
    private boolean useWaterLakes, useLavaLakes, useLavaOceans;
    // Above ground Generation Toggles
    private boolean useVillages, useTemples, useMonuments;
    // Dungeon Options
    private int dungeonChance;
    // Lake Options
    private int waterLakeChance, lavaLakeChance;
    // Set Biome
    private int fixedBiome;
    // Biome Options
    private int biomeSize, riverSize;
    // Dirt Options
    private int dirtSize, dirtCount, dirtMinHeight, dirtMaxHeight;
    // Gravel Options
    private int gravelSize, gravelCount, gravelMinHeight, gravelMaxHeight;
    // Granite Options
    private int graniteSize, graniteCount, graniteMinHeight, graniteMaxHeight;
    // Diorite Options
    private int dioriteSize, dioriteCount, dioriteMinHeight, dioriteMaxHeight;
    // Andesite Options
    private int andesiteSize, andesiteCount, andesiteMinHeight, andesiteMaxHeight;
    // Coal Options
    private int coalSize, coalCount, coalMinHeight, coalMaxHeight;
    // Iron Options
    private int ironSize, ironCount, ironMinHeight, ironMaxHeight;
    // Gold Options
    private int goldSize, goldCount, goldMinHeight, goldMaxHeight;
    // Redstone Options
    private int redstoneSize, redstoneCount, redstoneMinHeight, redstoneMaxHeight;
    // Diamond Options
    private int diamondSize, diamondCount, diamondMinHeight, diamondMaxHeight;
    // Lapis Options
    private int lapisSize, lapisCount, lapisCenterHeight, lapisSpread;

    /**
     * Constructor which creates the builder instance with all of the default world generation values
     * defined in Minecraft 1.8.1
     */
    public GeneratorSettingsBuilder() {
        this.coordinateScale = 684.412;
        this.heightScale = 684.412;
        this.lowerLimitScale = 512.0;
        this.upperLimitScale = 512.0;
        this.depthNoiseScaleX = 200.0;
        this.depthNoiseScaleZ = 200.0;
        this.depthNoiseScaleExponent = 0.5;
        this.mainNoiseScaleX = 80.0;
        this.mainNoiseScaleY = 160.0;
        this.mainNoiseScaleZ = 80.0;
        this.baseSize = 8.5;
        this.stretchY = 12.0;
        this.biomeDepthWeight = 1.0;
        this.biomeDepthOffset = 0.0;
        this.biomeScaleWeight = 1.0;
        this.biomeScaleOffset = 0.0;
        this.seaLevel = 63;
        this.useCaves = true;
        this.useDungeons = true;
        this.dungeonChance = 8;
        this.useStrongholds = true;
        this.useVillages = true;
        this.useMineShafts = true;
        this.useTemples = true;
        this.useMonuments = true;
        this.useRavines = true;
        this.useWaterLakes = true;
        this.waterLakeChance = 4;
        this.useLavaLakes = true;
        this.lavaLakeChance = 80;
        this.useLavaOceans = false;
        this.fixedBiome = -1;
        this.biomeSize = 4;
        this.riverSize = 4;
        this.dirtSize = 33;
        this.dirtCount = 10;
        this.dirtMinHeight = 0;
        this.dirtMaxHeight = 256;
        this.gravelSize = 33;
        this.gravelCount = 8;
        this.gravelMinHeight = 0;
        this.gravelMaxHeight = 256;
        this.graniteSize = 33;
        this.graniteCount = 10;
        this.graniteMinHeight = 0;
        this.graniteMaxHeight = 80;
        this.dioriteSize = 33;
        this.dioriteCount = 10;
        this.dioriteMinHeight = 0;
        this.dioriteMaxHeight = 80;
        this.andesiteSize = 33;
        this.andesiteCount = 10;
        this.andesiteMinHeight = 0;
        this.andesiteMaxHeight = 80;
        this.coalSize = 17;
        this.coalCount = 20;
        this.coalMinHeight = 0;
        this.coalMaxHeight = 128;
        this.ironSize = 9;
        this.ironCount = 20;
        this.ironMinHeight = 0;
        this.ironMaxHeight = 64;
        this.goldSize = 9;
        this.goldCount = 2;
        this.goldMinHeight = 0;
        this.goldMaxHeight = 32;
        this.redstoneSize = 8;
        this.redstoneCount = 8;
        this.redstoneMinHeight = 0;
        this.redstoneMaxHeight = 16;
        this.diamondSize = 8;
        this.diamondCount = 1;
        this.diamondMinHeight = 0;
        this.diamondMaxHeight = 16;
        this.lapisSize = 7;
        this.lapisCount = 1;
        this.lapisCenterHeight = 16;
        this.lapisSpread = 16;
    }

    /**
     * Horizontal stretch of the main world. This effectively handles "sharpness", which makes
     * differences in the terrain more distinct. This applies to the first run of hills/mountains the
     * game makes, but not to further changes. Below default: Mountains are drawn in width, their
     * peaks and chasms disappear, they are flat although their height hardly changes. The positions
     * of mountains and valleys remain the same. Above default: Mountains and hills are sharper and
     * steeper with narrow peaks. Mountain ranges no longer exist. The valleys are covered with small
     * hills, there are hardly any flat areas.
     */
    public GeneratorSettingsBuilder coordinateScale(double coordinateScale) {
        this.coordinateScale = coordinateScale;
        return this;
    }

    /**
     * Vertical stretch of the main world. This applies to the first run of hills/mountains the game
     * makes, but not to further changes. Below default: Mountains are rounded, but only slightly
     * lower. Above default: Relatively flat areas are higher and steepen, although the mountains are
     * fairly low. If high enough (using a customly pasted preset), the Far Lands appears in the sky.
     */
    public GeneratorSettingsBuilder heightScale(double heightScale) {
        this.heightScale = heightScale;
        return this;
    }

    /**
     * Make terrain more solid/riddled with holes depending on how close the values are to the upper
     * limit scale values. The further apart the values, the more holes there are in the
     * landscape.[verify] Note however, that it is used in cave/cavern generation, and so setting them
     * far apart with caves/caverns turned off doesn't give you holed terrain. This setting has the
     * same effect as the Upper Limit Scale, except that it affects parts of the landscape which the
     * Upper Limit Scale does not affect.
     */
    public GeneratorSettingsBuilder lowerLimitScale(double lowerLimitScale) {
        this.lowerLimitScale = lowerLimitScale;
        return this;
    }

    /**
     * Make terrain more solid/riddled with holes depending on how close the values are to the lower
     * limit scale values. Note however, that it is used in cave/cavern generation, and so setting
     * them far apart with caves/caverns turned off doesn't give you holed terrain. Below default: The
     * lower the value, the longer it takes to generate the landscape. This raises the heights of some
     * mountains and parts of the entire landscape sharply without changing their positions, while
     * other parts of the landscape remain unchanged. The increase in height is nonlinear. Values
     * close to the minimum already have an enormous impact, but the larger values have a lower the
     * impact. At the minimum value, the surface is calculated well above the maximum height and
     * capped at level 256. This leads to completely flat planes. There are huge cavities where the
     * walls are not rounded, but have rather smooth, blocky edges. Above default: The same mountains
     * that extend when lowering the value in the height will always flatter while raising the value,
     * while the mountains of the other parts of the landscape remain unchanged. With the maximum
     * value mountains are almost gone.
     */
    public GeneratorSettingsBuilder upperLimitScale(double upperLimitScale) {
        this.upperLimitScale = upperLimitScale;
        return this;
    }

    /**
     * Creates more variations and abrupt changes in the height of the terrain along the x-axis. Below
     * default: Only minimal differences. Above default: Longer edges in Z-direction.
     */
    public GeneratorSettingsBuilder depthNoiseScaleX(double depthNoiseScaleX) {
        this.depthNoiseScaleX = depthNoiseScaleX;
        return this;
    }

    /**
     * Creates more variations and abrupt changes in the height of the terrain along the z-axis. Below
     * default: Only minimal differences. Above default: Longer edges in X-direction.
     */
    public GeneratorSettingsBuilder depthNoiseScaleZ(double depthNoiseScaleZ) {
        this.depthNoiseScaleZ = depthNoiseScaleZ;
        return this;
    }

    /**
     * Customizes the size of random shapes that appear in world generation Below default: Only
     * minimal differences. Above default: Only minor differences.
     */
    public GeneratorSettingsBuilder depthNoiseScaleExponent(double depthNoiseScaleExponent) {
        this.depthNoiseScaleExponent = depthNoiseScaleExponent;
        return this;
    }

    /**
     * Stretches the terrain along the x-axis Below default: Mountains are cut in Z-direction in
     * slices. Levels have a long strip of land in the Z-direction. Above default: Mountains are
     * smoother and wider in the Z-direction, more compact and have long, narrow edges in X-direction.
     * Levels have a long strip of land in the X-direction.
     */
    public GeneratorSettingsBuilder mainNoiseScaleX(double mainNoiseScaleX) {
        this.mainNoiseScaleX = mainNoiseScaleX;
        return this;
    }

    /**
     * Stretches the terrain along the y (height) axis. Below default: Mountains are being eroded in
     * the lower areas and make large, shallow overhangs. Above default: Mountains are more compact,
     * higher and less rugged.
     */
    public GeneratorSettingsBuilder mainNoiseScaleY(double mainNoiseScaleY) {
        this.mainNoiseScaleY = mainNoiseScaleY;
        return this;
    }

    /**
     * Stretches the terrain along the z-axis. Below default: Mountains are cut in the X-direction in
     * slices. Levels have a long strip of land in the X-direction. Above default: Mountains have
     * narrow slices in the Z-direction and are slightly compressed in the X-direction. Levels have a
     * long strip of land in the Z-direction.
     */
    public GeneratorSettingsBuilder mainNoiseScaleZ(double mainNoiseScaleZ) {
        this.mainNoiseScaleZ = mainNoiseScaleZ;
        return this;
    }

    /**
     * Decides at which height the surface is generated, before anything else is manipulated. But
     * unlike a stretch effect, this gives more ground to be manipulated. Any change in the value of 1
     * corresponds to a variation of the base height by 8 levels. The value 0 cannot be set because of
     * the base amount at least still the bedrock level must be generated. The default value of 8.5
     * corresponds to the base height of 68, which is slightly above sea level (level 63). Below
     * default: The base amount is below sea level. If this is not adjusted, the area is flooded. If
     * the value is set to "1", this corresponds to the base height 8, which is below the lava level
     * (height 11). This lava flows from the cave openings, because all caves are automatically filled
     * to the lava level with lava. The lava lakes, which are normally in the underground, emerge
     * openly, because there are only a few layers underground. Rivers have dried up, when the sea
     * level is lower. Structures are generated, they stand on pillars (villages) or float in the air
     * (abandoned mineshafts). Above default: The higher the value, the longer it takes to generate
     * the landscape. At the maximum value, the base height is 200. River valleys are deeply incised
     * and yet dried up, because they do not reach the sea. Mountains are higher and more above the
     * base height than normal. It is appropriate to adjust the distribution of the ore to the base
     * height.
     */
    public GeneratorSettingsBuilder baseSize(double baseSize) {
        this.baseSize = baseSize;
        return this;
    }

    /**
     * Everything gets stretched or crushed more along the y-axis. It pulls terrain upward. Below
     * default: The lower the value, the longer it takes to generate the landscape. Lower values cause
     * more extreme stretching, where the minimum value has the surface above level 250. It is not
     * smooth, but still forms a flat profile. The sinks in the landscape lead into canyons of
     * dizzying depth with giant cavities. Above default: All heights are upset, mountains and valleys
     * are flat.
     */
    public GeneratorSettingsBuilder stretchY(double stretchY) {
        this.stretchY = stretchY;
        return this;
    }

    /**
     * Changes the height of the biomes. With increasing value the heights of the biomes are
     * increased, without affecting the rest of the landscape. A maximum setting of 20 results in cone
     * mountains that would exceed the maximum height of 256 meters and cap there. Large valleys of
     * normal landscape generate between the mountains. The middle screenshot shows the value of 3.
     */
    public GeneratorSettingsBuilder biomeDepthWeight(double biomeDepthWeight) {
        this.biomeDepthWeight = biomeDepthWeight;
        return this;
    }

    /**
     * Determines the surface level of the biome. The surface level of the biome is raised, but not
     * the height of the features of the biome (e.g. hills). For example, in a tundra biome the
     * surface at a setting of 0 is near the ocean level as normal. When set to 2, the surface is
     * already at 100 meters, at 5 it is 150 meters and 10 at 250 meters. A further increase of the
     * value leads to surfaces that would exceed the maximum height of 256 meters. The landscape is
     * then capped. Therefore, at the maximum value 20 only a smooth, green sectional area at the
     * level of 256 is visible. If set to -3.75 through a custom preset, the would-be sea level will
     * approximately coincide with layer 0.
     */
    public GeneratorSettingsBuilder biomeDepthOffset(double biomeDepthOffset) {
        this.biomeDepthOffset = biomeDepthOffset;
        return this;
    }

    /**
     * Determines the weight of biome specific characteristics. With increasing values, these
     * characteristics are weighted more heavily. At the maximum value all tundra hills are over 240
     * meters high.
     */
    public GeneratorSettingsBuilder biomeScaleWeight(double biomeScaleWeight) {
        this.biomeScaleWeight = biomeScaleWeight;
        return this;
    }

    /**
     * Moves biome specific characteristics. With increasing value these characteristics are moved
     * further. The higher the value, the longer it takes to generate the landscape.
     */
    public GeneratorSettingsBuilder biomeScaleOffset(double biomeScaleOffset) {
        this.biomeScaleOffset = biomeScaleOffset;
        return this;
    }

    /**
     * The surface level of all oceans and rivers. If the level is set below the default, land masses
     * are bigger, and rivers may be shallow or dry. All land below level 63 is gravel. For low
     * settings, the ocean may be reduced to small lakes, may be restricted to underground caves and
     * caverns, or sometimes even doesn't exist. If the level is set above the default, low-lying
     * biomes such as swamps are fragmented or nonexistent, creating a world with smaller land masses.
     * Rivers are not "rivers" in the literal sense, but are more like subsurface valleys. The world
     * may, at high levels, consist of sparse islands separated by very deep water or may be all
     * ocean, as in the Water World preset. Setting it to a negative value causes chunks never to
     * load, always displaying "Waiting for chunk..." in the F3 menu. (Chunks are still generated, and
     * can be viewed via external programs like MCEdit)
     */
    public GeneratorSettingsBuilder seaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
        return this;
    }

    /**
     * Caves are only generated underground. If omitted, they are not generated.
     */
    public GeneratorSettingsBuilder useCaves(boolean useCaves) {
        this.useCaves = useCaves;
        return this;
    }

    /**
     * Dungeons are only generated if there is an opening next to them. If there is no opening (cave,
     * stronghold, abandoned mineshaft, ravine or underground lake) near the generated position of the
     * dungeon, the dungeon is not generated.
     */
    public GeneratorSettingsBuilder useDungeons(boolean useDungeons) {
        this.useDungeons = useDungeons;
        return this;
    }

    /**
     * There are 128 strongholds generated in eight rings around the center (0, 0) (see stronghold).
     * End portals will not be generated in the world if strongholds are not generated.
     */
    public GeneratorSettingsBuilder useStrongholds(boolean useStrongholds) {
        this.useStrongholds = useStrongholds;
        return this;
    }

    /**
     * Whether mineshafts are generated in each biome underground.
     */
    public GeneratorSettingsBuilder useMineShafts(boolean useMineShafts) {
        this.useMineShafts = useMineShafts;
        return this;
    }

    /**
     * Ravines are only generated underground. If omitted, they are not generated.
     */
    public GeneratorSettingsBuilder useRavines(boolean useRavines) {
        this.useRavines = useRavines;
        return this;
    }

    /**
     * If No, water lakes will not generate
     */
    public GeneratorSettingsBuilder useWaterLakes(boolean useWaterLakes) {
        this.useWaterLakes = useWaterLakes;
        return this;
    }

    /**
     * If No, lava lakes will not generate (Note: This does not prevent lava from filling up caves
     * under Y level 10)
     */
    public GeneratorSettingsBuilder useLavaLakes(boolean useLavaLakes) {
        this.useLavaLakes = useLavaLakes;
        return this;
    }

    /**
     * The setting determines whether all the world's oceans and rivers should be filled with lava. If
     * they are filled with lava, all combustible materials on their shores catch on fire when a
     * player is nearby.
     */
    public GeneratorSettingsBuilder useLavaOceans(boolean useLavaOceans) {
        this.useLavaOceans = useLavaOceans;
        return this;
    }

    /**
     * Villages will only generate if there are plains, savanna, taiga or desert biomes. If the world
     * is set to another biome, they are not generated. If no villages exist, there are no spawn areas
     * for villagers. However, villagers can still be obtained by curing zombie villagers.
     */
    public GeneratorSettingsBuilder useVillages(boolean useVillages) {
        this.useVillages = useVillages;
        return this;
    }

    /**
     * When this is turned on, jungle temples generate in jungle biomes, desert temples generate in
     * desert biomes, witch huts generate in swamp biomes, and igloos generate in ice plains. If the
     * world is set to another biome, they are not generated.
     */
    public GeneratorSettingsBuilder useTemples(boolean useTemples) {
        this.useTemples = useTemples;
        return this;
    }

    /**
     * When this structure is turned on, the ocean monuments are generated in the deep ocean biomes.
     * The adjusted sea level does not affect the generation of ocean monuments. If the world is set
     * to another biome, they are not generated.
     */
    public GeneratorSettingsBuilder useMonuments(boolean useMonuments) {
        this.useMonuments = useMonuments;
        return this;
    }

    /**
     * Changes how many times the game will try to generate a dungeon per chunk. This is not the
     * actual number of dungeons per chunk - as not all attempts are successful. This is because they
     * are only generated next to an opening. This means that at a setting of 100% only very few or no
     * dungeons are generated when the ground does not provide an opening for connecting to the right
     * places. For example, in a world with only ravines, only two dungeons were generated at a
     * setting of 100%.
     */
    public GeneratorSettingsBuilder dungeonChance(int dungeonChance) {
        this.dungeonChance = dungeonChance;
        return this;
    }

    /**
     * Increases/decreases the rarity of water lake generation (as a percentage); lower values mean a
     * higher number of water lakes. Water lakes can generate where there is no opening, unlike
     * dungeons. Therefore, the underground water lakes are usually discovered only by chance while
     * digging. The decrease in the frequency is not linear: the difference between 1% and 25% is
     * huge, the other differences up to 100% are minor. The lakes are - in contrast to the dungeons -
     * not always at the same position.
     */
    public GeneratorSettingsBuilder waterLakeChance(int waterLakeChance) {
        this.waterLakeChance = waterLakeChance;
        return this;
    }

    /**
     * Increases/decreases the rarity of lava lake generation (as a percentage); lower values mean a
     * higher number of lava lakes. Lava lakes are generated underground to the surface. Very few
     * lakes generate on the surface. In contrast to the water lakes, which are uniformly distributed
     * underground, the distribution of lava lakes is concentrated on the lower levels. In addition,
     * the positions in the lava lakes from setting to setting vary widely. Few lakes retain their
     * position at each setting. The decrease in the frequency is not linear: the difference between
     * 1% and 25% is huge, the other differences up to 100% are minor.
     */
    public GeneratorSettingsBuilder lavaLakeChance(int lavaLakeChance) {
        this.lavaLakeChance = lavaLakeChance;
        return this;
    }

    /**
     * Number corresponding to which biome(s) should generate in the world. The rare biomes (such as
     * desert M and sunflower plains), the Nether and the End are not on the list. However, biome
     * variants such as beaches, hills and edge biomes can be generated.
     */
    public GeneratorSettingsBuilder fixedBiome(int fixedBiome) {
        this.fixedBiome = fixedBiome;
        return this;
    }

    /**
     * Increases/decreases the size of biomes. Increasing the number by one doubles the size of
     * biomes. The generation of oceans and lakes are independent of biome size. Biomes are magnified
     * from the origin (0,0). Even in a single-biome world, this makes a difference; in a desert
     * world, higher "biome size" seems to predispose toward flatter terrain and more villages and
     * temples, while a lower setting results in steeper, more rugged terrain with fewer villages and
     * temples. See also Large Biomes.
     */
    public GeneratorSettingsBuilder biomeSize(int biomeSize) {
        this.biomeSize = biomeSize;
        return this;
    }

    /**
     * Increases/decreases the size and frequency of rivers. Decreasing the number by one doubles the
     * proximity of rivers to each other. So while you get only slightly larger rivers by changing
     * this 2 points, you also get approximately 3x as many rivers. At a setting of 1, rivers generate
     * through most of the landscape; at 5, there are large tracts of land with no water. Rivers are
     * magnified from the origin (0,0) so increasing the value by 1 will double the distance to a
     * river. If the sea level is reduced far below 63, many "rivers" will be dry; but their beds will
     * be dirt regardless of the surrounding biome.
     */
    public GeneratorSettingsBuilder riverSize(int riverSize) {
        this.riverSize = riverSize;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single dirt vein.
     */
    public GeneratorSettingsBuilder dirtSize(int dirtSize) {
        this.dirtSize = dirtSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder dirtCount(int dirtCount) {
        this.dirtCount = dirtCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder dirtMinHeight(int dirtMinHeight) {
        this.dirtMinHeight = dirtMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder dirtMaxHeight(int dirtMaxHeight) {
        this.dirtMaxHeight = dirtMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single gravel vein.
     */
    public GeneratorSettingsBuilder gravelSize(int gravelSize) {
        this.gravelSize = gravelSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder gravelCount(int gravelCount) {
        this.gravelCount = gravelCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder gravelMinHeight(int gravelMinHeight) {
        this.gravelMinHeight = gravelMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder gravelMaxHeight(int gravelMaxHeight) {
        this.gravelMaxHeight = gravelMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single granite vein.
     */
    public GeneratorSettingsBuilder graniteSize(int graniteSize) {
        this.graniteSize = graniteSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder graniteCount(int graniteCount) {
        this.graniteCount = graniteCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder graniteMinHeight(int graniteMinHeight) {
        this.graniteMinHeight = graniteMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder graniteMaxHeight(int graniteMaxHeight) {
        this.graniteMaxHeight = graniteMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single diorite vein.
     */
    public GeneratorSettingsBuilder dioriteSize(int dioriteSize) {
        this.dioriteSize = dioriteSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder dioriteCount(int dioriteCount) {
        this.dioriteCount = dioriteCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder dioriteMinHeight(int dioriteMinHeight) {
        this.dioriteMinHeight = dioriteMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder dioriteMaxHeight(int dioriteMaxHeight) {
        this.dioriteMaxHeight = dioriteMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single andesite vein.
     */
    public GeneratorSettingsBuilder andesiteSize(int andesiteSize) {
        this.andesiteSize = andesiteSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder andesiteCount(int andesiteCount) {
        this.andesiteCount = andesiteCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder andesiteMinHeight(int andesiteMinHeight) {
        this.andesiteMinHeight = andesiteMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder andesiteMaxHeight(int andesiteMaxHeight) {
        this.andesiteMaxHeight = andesiteMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single coal vein.
     */
    public GeneratorSettingsBuilder coalSize(int coalSize) {
        this.coalSize = coalSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder coalCount(int coalCount) {
        this.coalCount = coalCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder coalMinHeight(int coalMinHeight) {
        this.coalMinHeight = coalMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder coalMaxHeight(int coalMaxHeight) {
        this.coalMaxHeight = coalMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single iron vein.
     */
    public GeneratorSettingsBuilder ironSize(int ironSize) {
        this.ironSize = ironSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder ironCount(int ironCount) {
        this.ironCount = ironCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder ironMinHeight(int ironMinHeight) {
        this.ironMinHeight = ironMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder ironMaxHeight(int ironMaxHeight) {
        this.ironMaxHeight = ironMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single gold vein.
     */
    public GeneratorSettingsBuilder goldSize(int goldSize) {
        this.goldSize = goldSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder goldCount(int goldCount) {
        this.goldCount = goldCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder goldMinHeight(int goldMinHeight) {
        this.goldMinHeight = goldMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder goldMaxHeight(int goldMaxHeight) {
        this.goldMaxHeight = goldMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single redstone vein.
     */
    public GeneratorSettingsBuilder redstoneSize(int redstoneSize) {
        this.redstoneSize = redstoneSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder redstoneCount(int redstoneCount) {
        this.redstoneCount = redstoneCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder redstoneMinHeight(int redstoneMinHeight) {
        this.redstoneMinHeight = redstoneMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder redstoneMaxHeight(int redstoneMaxHeight) {
        this.redstoneMaxHeight = redstoneMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single diamond vein.
     */
    public GeneratorSettingsBuilder diamondSize(int diamondSize) {
        this.diamondSize = diamondSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder diamondCount(int diamondCount) {
        this.diamondCount = diamondCount;
        return this;
    }

    /**
     * The minimum height at which the ore generates.
     */
    public GeneratorSettingsBuilder diamondMinHeight(int diamondMinHeight) {
        this.diamondMinHeight = diamondMinHeight;
        return this;
    }

    /**
     * The maximum height at which the ore generates.
     */
    public GeneratorSettingsBuilder diamondMaxHeight(int diamondMaxHeight) {
        this.diamondMaxHeight = diamondMaxHeight;
        return this;
    }

    /**
     * The approximate maximum number of blocks in a single lapis vein.
     */
    public GeneratorSettingsBuilder lapisSize(int lapisSize) {
        this.lapisSize = lapisSize;
        return this;
    }

    /**
     * The number of times the world generator attempts to place a vein in a chunk.
     */
    public GeneratorSettingsBuilder lapisCount(int lapisCount) {
        this.lapisCount = lapisCount;
        return this;
    }

    /**
     * Center height of where veins should be spawned in the world.
     */
    public GeneratorSettingsBuilder lapisCenterHeight(int lapisCenterHeight) {
        this.lapisCenterHeight = lapisCenterHeight;
        return this;
    }

    /**
     * Distribution from the center height of where veins should spawn.
     */
    public GeneratorSettingsBuilder lapisSpread(int lapisSpread) {
        this.lapisSpread = lapisSpread;
        return this;
    }

    /**
     * @return a string which can be directly passed to {@link
     * org.bukkit.WorldCreator#generatorSettings(String)} without any needed modification using
     * data from this class.
     */
    public String build() {
        JsonObject object = new JsonObject();
        object.addProperty("coordinateScale", coordinateScale);
        object.addProperty("heightScale", heightScale);
        object.addProperty("lowerLimitScale", lowerLimitScale);
        object.addProperty("upperLimitScale", upperLimitScale);
        object.addProperty("depthNoiseScaleX", depthNoiseScaleX);
        object.addProperty("depthNoiseScaleZ", depthNoiseScaleZ);
        object.addProperty("depthNoiseScaleExponent", depthNoiseScaleExponent);
        object.addProperty("mainNoiseScaleX", mainNoiseScaleX);
        object.addProperty("mainNoiseScaleY", mainNoiseScaleY);
        object.addProperty("mainNoiseScaleZ", mainNoiseScaleZ);
        object.addProperty("baseSize", baseSize);
        object.addProperty("stretchY", stretchY);
        object.addProperty("biomeDepthWeight", biomeDepthWeight);
        object.addProperty("biomeDepthOffset", biomeDepthOffset);
        object.addProperty("biomeScaleWeight", biomeScaleWeight);
        object.addProperty("biomeScaleOffset", biomeScaleOffset);
        object.addProperty("seaLevel", seaLevel);
        object.addProperty("useCaves", useCaves);
        object.addProperty("useDungeons", useDungeons);
        object.addProperty("dungeonChance", dungeonChance);
        object.addProperty("useStrongholds", useStrongholds);
        object.addProperty("useVillages", useVillages);
        object.addProperty("useMineShafts", useMineShafts);
        object.addProperty("useTemples", useTemples);
        object.addProperty("useMonuments", useMonuments);
        object.addProperty("useRavines", useRavines);
        object.addProperty("useWaterLakes", useWaterLakes);
        object.addProperty("waterLakeChance", waterLakeChance);
        object.addProperty("useLavaLakes", useLavaLakes);
        object.addProperty("lavaLakeChance", lavaLakeChance);
        object.addProperty("useLavaOceans", useLavaOceans);
        object.addProperty("fixedBiome", fixedBiome);
        object.addProperty("biomeSize", biomeSize);
        object.addProperty("riverSize", riverSize);
        object.addProperty("dirtSize", dirtSize);
        object.addProperty("dirtCount", dirtCount);
        object.addProperty("dirtMinHeight", dirtMinHeight);
        object.addProperty("dirtMaxHeight", dirtMaxHeight);
        object.addProperty("gravelSize", gravelSize);
        object.addProperty("gravelCount", gravelCount);
        object.addProperty("gravelMinHeight", gravelMinHeight);
        object.addProperty("gravelMaxHeight", gravelMaxHeight);
        object.addProperty("graniteSize", graniteSize);
        object.addProperty("graniteCount", graniteCount);
        object.addProperty("graniteMinHeight", graniteMinHeight);
        object.addProperty("graniteMaxHeight", graniteMaxHeight);
        object.addProperty("dioriteSize", dioriteSize);
        object.addProperty("dioriteCount", dioriteCount);
        object.addProperty("dioriteMinHeight", dioriteMinHeight);
        object.addProperty("dioriteMaxHeight", dioriteMaxHeight);
        object.addProperty("andesiteSize", andesiteSize);
        object.addProperty("andesiteCount", andesiteCount);
        object.addProperty("andesiteMinHeight", andesiteMinHeight);
        object.addProperty("andesiteMaxHeight", andesiteMaxHeight);
        object.addProperty("coalSize", coalSize);
        object.addProperty("coalCount", coalCount);
        object.addProperty("coalMinHeight", coalMinHeight);
        object.addProperty("coalMaxHeight", coalMaxHeight);
        object.addProperty("ironSize", ironSize);
        object.addProperty("ironCount", ironCount);
        object.addProperty("ironMinHeight", ironMinHeight);
        object.addProperty("ironMaxHeight", ironMaxHeight);
        object.addProperty("goldSize", goldSize);
        object.addProperty("goldCount", goldCount);
        object.addProperty("goldMinHeight", goldMinHeight);
        object.addProperty("goldMaxHeight", goldMaxHeight);
        object.addProperty("redstoneSize", redstoneSize);
        object.addProperty("redstoneCount", redstoneCount);
        object.addProperty("redstoneMinHeight", redstoneMinHeight);
        object.addProperty("redstoneMaxHeight", redstoneMaxHeight);
        object.addProperty("diamondSize", diamondSize);
        object.addProperty("diamondCount", diamondCount);
        object.addProperty("diamondMinHeight", diamondMinHeight);
        object.addProperty("diamondMaxHeight", diamondMaxHeight);
        object.addProperty("lapisSize", lapisSize);
        object.addProperty("lapisCount", lapisCount);
        object.addProperty("lapisCenterHeight", lapisCenterHeight);
        object.addProperty("lapisSpread", lapisSpread);
        return object.toString();
    }
}
