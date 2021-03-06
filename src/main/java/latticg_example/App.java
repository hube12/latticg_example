package latticg_example;

import com.seedfinding.latticg.reversal.DynamicProgram;
import com.seedfinding.latticg.reversal.calltype.java.JavaCalls;
import com.seedfinding.latticg.util.LCG;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.Feature;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.featureutils.structure.RuinedPortal;
import kaptainwutax.featureutils.structure.Village;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import mjtb49.hashreversals.ChunkRandomReverser;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

public class App {
    public void reverse12NextDoubleCalls() {
        DynamicProgram dynamicProgram = DynamicProgram.create(LCG.JAVA);
        // my constraints
        for (int i = 0; i < 12; i++) {
            dynamicProgram.add(JavaCalls.nextDouble().betweenII(0.0D, 0.1D));
        }
        long start = System.nanoTime();

        AtomicInteger count = new AtomicInteger(0);
        dynamicProgram.reverse().forEach(s -> {
            count.incrementAndGet();
            System.out.println(s);
        });
        if (count.get() == 1)
            System.out.println("Found " + count + " seed.");
        else System.out.println("Found " + count + " seeds.");

        long end = System.nanoTime();

        System.out.printf("elapsed: %.2fs%n", (end - start) * 1e-9);

    }

    public void test() {
        DynamicProgram dynamicProgram = DynamicProgram.create(LCG.JAVA);
        for (int i = 0; i < 15; i++) dynamicProgram.add(JavaCalls.nextFloat().lessThan(0.1f));
        dynamicProgram.reverse().forEach(System.out::println);
    }

    public void useChunkRandomReversal() {
        long decoratorSeed = 1234L;
        int posX = 100;
        int posZ = 100;
        MCVersion version = MCVersion.v1_16;
        ChunkRandomReverser.reversePopulationSeed(decoratorSeed ^ LCG.JAVA.multiplier, posX >> 4, posZ >> 4, version).forEach(s -> {
            System.out.println("Found world seed: " + s);
        });
    }

    public void treeForward12(long startseed, int x, int z, boolean small, int trunk, int[] leaves) {
        assert leaves.length == 12;
        this._treeForward12(startseed, x, z, small, trunk, leaves, 4);
    }

    public void _treeForward12(long startseed, int x, int z, boolean small, int trunk, int[] leaves, int def_height) {
        boolean extremeHills = true;

        Randy random = new Randy(startseed ^ LCG.JAVA.multiplier);
        System.out.println(random.getSeed());
        int offX = random.nextInt(16);
        System.out.println("FOUND OFFX " + offX + " FOR " + ((x - 8) & 0xF) + " " + (((x - 8) & 0xF) == offX));
        int offZ = random.nextInt(16);
        System.out.println("FOUND OFFZ " + offZ + " FOR " + ((z - 8) & 0xF) + " " + (((z - 8) & 0xF) == offZ));
        if (extremeHills) {
            boolean hills = random.nextInt(3) == 0;
            System.out.println("FOUND " + (hills ? "OAK" : "SPRUCE") + " AND " + (hills == extremeHills));
        }
        boolean smallTree = random.nextInt(10) != 0;
        System.out.println("FOUND " + (smallTree ? "SMALL" : "BIG") + " AND " + (smallTree == small));
        System.out.println(random.getSeed());
        int trunkHeight = random.nextInt(3) + def_height;
        System.out.println("FOUND TRUNK " + trunkHeight + " FOR " + trunk + " " + (trunkHeight == trunk));
        System.out.println(random.getSeed());
        // nextInt(2)==1 means placed so in leaves array
        for (int leaf : leaves) {
            int placed = random.nextInt(2);
            System.out.println("FOUND " + (placed == 1 ? "LEAVES" : "AIR") + " SO " + (placed == leaf));
        }
        System.out.println(random.getSeed());
        for (int i = 0; i < 4; i++) {
            random.nextInt(2);
        }
        System.out.println(random.getSeed());
    }

    public int[] makeLeafPattern(int[] bottom4, int[] middle4, int[] top4) {
        int[] arr = new int[12];

        int i = 0;
        for (int b : bottom4) {
            arr[i++] = b;
        }
        for (int b : middle4) {
            arr[i++] = b;
        }
        for (int b : top4) {
            arr[i++] = b;
        }
        return arr;
    }

    public void testTree() {
        // SEED: -3295837504754545043
        // /tp @p 72 85 521
        // OAK Tree
        // 5625546575749 at 5077 Before for -3295837504754545043
        // x=72, y=78, z=521
        // nextInt(16)+8+X=72 so 72&15=0=nextInt(16)
        // nextInt(16)+8+Z=521 so 513&15=1=nextInt(16)
        // Extreme hills
        // nextInt(3)==0 (see biomes dependency)
        // small tree nextInt(10)!=0 (general case)
        // 134986574823093 at 5081
        // height=5
        // nextInt(3)==1 because trunk=4
        // 261538975044976 at 5082
        // LEAVES PLACING
        // 121517295381052 at 5094
        // 4 BURN OUT CALLS
        // 126916939308800 at 5098
        // NO VINES PLACED
        // NO VINES UPDATE
        // NW SW NE SE
        int[] bottom4 = {0, 1, 1, 1};
        // NW SW NE SE
        int[] middle4 = {1, 1, 1, 1};
        // NW SW NE SE
        int[] top4 = {0, 1, 0, 0};
        this.treeForward12(5625546575749L, 72, 521, true, 5, makeLeafPattern(bottom4, middle4, top4));
        this.treeReversal12(5625546575749L, 72, 521, true, 5, makeLeafPattern(bottom4, middle4, top4));
    }

    public void _treeReversal12(long target, int x, int z, boolean small, int trunk, int[] leaves, int def_height) {


        DynamicProgram dynamicProgram = DynamicProgram.create(LCG.JAVA);
        // my constraints
        int offX = (x - 8) & 0xf;
        dynamicProgram.add(JavaCalls.nextInt(16).equalTo(offX));
        int offZ = (z - 8) & 0xf;
        dynamicProgram.add(JavaCalls.nextInt(16).equalTo(offZ));
        // specific extreme hills
        dynamicProgram.add(JavaCalls.nextInt(3).equalTo(0));
        // tree type
        if (small) {
            dynamicProgram.add(JavaCalls.nextInt(10).notEqualTo(0));
        } else {
            dynamicProgram.add(JavaCalls.nextInt(10).equalTo(0));
        }
        // trunk height
        dynamicProgram.add(JavaCalls.nextInt(3).equalTo(trunk - def_height));
        for (int leaf : leaves) {
            dynamicProgram.add(JavaCalls.nextInt(2).equalTo(leaf));
        }

        long start = System.nanoTime();
        AtomicInteger count = new AtomicInteger(0);
        dynamicProgram.reverse().forEach(s -> {
            count.incrementAndGet();
            System.out.println(s);
            if (s == target) {
                System.out.println("FOUND in t = " + (int) (((System.nanoTime() - start) * 1e-9)) + " s");
            }
        });
        if (count.get() == 1)
            System.out.println("Found " + count + " seed.");
        else System.out.println("Found " + count + " seeds.");

        long end = System.nanoTime();

        System.out.printf("elapsed: %.2fs%n", (end - start) * 1e-9);

    }

    public void treeReversal12(long target, int x, int z, boolean small, int trunk, int[] leaves) {
        this._treeReversal12(target, x, z, small, trunk, leaves, 4);
    }


    public void tree_layout() {
        for (int rangeY = -3; rangeY <= 0; ++rangeY) {
            int sizeXZ = 1 - rangeY / 2;
            for (int rangeX = -sizeXZ; rangeX <= +sizeXZ; ++rangeX) {
                for (int rangeZ = -sizeXZ; rangeZ <= +sizeXZ; ++rangeZ) {
                    if (sizeXZ != Math.abs(rangeX)) {
                        System.out.println("LEAVE PLACED " + rangeX + " " + rangeY + " " + rangeZ);
                    } else if (Math.abs(rangeZ) != sizeXZ) {
                        System.out.println("LEAVE PLACED " + rangeX + " " + rangeY + " " + rangeZ);
                    } else if (/*rand.nextInt(2) != 0 &&*/ rangeY != 0) {
                        System.out.println("LEAVE PLACED RAND " + rangeX + " " + rangeY + " " + rangeZ);
                    } else {
                        System.out.println("LEAVE NOT PLACED " + rangeX + " " + rangeY + " " + rangeZ);
                    }

                }
            }
        }
    }

    public static class BiomeData {
        public Biome biome;
        public int x;
        public int z;

        BiomeData(int x, int z, Biome biome) {
            this.x = x;
            this.z = z;
            this.biome = biome;
        }

        public double check(OverworldBiomeSource source) {
            Biome biome = source.getBiome(x, 0, z);

            if (biome.getCategory() == this.biome.getCategory()) {
                return biome == this.biome ? 1 : 0.5;
            }
            return 0;

        }
    }

    public void crackBiomes() {
        long seed = 27594263L;
        ArrayList<BiomeData> biomeDatas = new ArrayList<>();
        biomeDatas.add(new BiomeData(-9070, -4555, Biome.FLOWER_FOREST));
        biomeDatas.add(new BiomeData(1909, -1279, Biome.SAVANNA));
        biomeDatas.add(new BiomeData(1922, -1223, Biome.JUNGLE_EDGE));
        for (long i = 0; i < (1L << 16); i++) {
            long cur = (i << 48) | seed;
            boolean good = true;
            double score = 0.0;
            OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_12, cur);
            for (BiomeData data : biomeDatas) {
                double s = data.check(source);
                if (s == 0) {
                    good = false;
                    break;
                }
                score += s;
            }
            if (good && score > 2) System.out.println(cur);
        }


    }

    public void runParallel() {

        Village village = new Village(MCVersion.v1_16);
        ChunkRand rand = new ChunkRand();

        ArrayList<RegionStructure.Data<?>> list = new ArrayList<>();
        list.add(village.at(0, 0));
        LongStream stream = LongStream.range(0, 1L << 48).map(structureSeed -> testStructures(list, structureSeed, rand)).parallel();
        stream.filter(l -> l != 0L).forEach(System.out::println);
    }

    public Long testStructures(ArrayList<RegionStructure.Data<?>> list, long structureSeed, ChunkRand rand) {
        boolean isOk = true;
        for (Feature.Data<?> data : list) {
            if (!data.feature.canStart(data, structureSeed, rand)) {
                isOk = false;
            }
        }
        if (isOk) {
            for (long upperBits = 0; upperBits < (1L << 16); upperBits++) {
                long worldSeed = upperBits << 48 | structureSeed;
                OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, worldSeed);
                isOk = true;
                for (Feature.Data<?> data : list) {
                    if (!data.feature.canSpawn(data, source)) {
                        isOk = false;
                    }
                }
                if (isOk) {
                    return worldSeed;
                }
            }
        }
        return 0L;
    }

    public static void main(String[] args) {
        RuinedPortal.CONFIGS.add(MCVersion.v1_16, new RegionStructure.Config(25, 10, 34222645));
        RuinedPortal netherRuinedPortal=new RuinedPortal(MCVersion.v1_16);
        System.out.println(netherRuinedPortal.getConfig().separation);
        App app = new App();
        //app.useChunkRandomReversal();
        //app.reverse12NextDoubleCalls();
        //app.treeReversal12();
        //app.testTree();
        //app.crackBiomes();
        //app.crackBiomes();
    }
}
