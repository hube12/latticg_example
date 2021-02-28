package latticg_example.structure;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.EndBiomeSource;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.decorator.DesertWell;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.BPos;


public class ExampleStructure {
    private static final MCVersion version = MCVersion.v1_16;
    private static final PillagerOutpost OUTPOST = new PillagerOutpost(version);
    private static final Village VILLAGE_DESERT = new Village(version) {
        @Override
        public boolean isValidBiome(Biome biome) {
            return biome == Biome.DESERT;
        }
    };
    private static final Village VILLAGE = new Village(version) {
        @Override
        public boolean isValidBiome(Biome biome) {
            return biome == Biome.DESERT;
        }
    };
    private static final DesertPyramid DESERT_TEMPLE = new DesertPyramid(version);
    private static final BuriedTreasure TREASURE = new BuriedTreasure(version);
    private static final Monument OCEAN_MONUMENT = new Monument(version);
    private static final DesertWell DESERT_WELL = new DesertWell(version);
    private static final BastionRemnant BASTION_REMNANT = new BastionRemnant(version);
    private static final EndCity END_CITY = new EndCity(version);


    public static void main(String[] args) {
        BPos bPos=new BPos(0,0,0); // block position at x0 z0
        long worldSeed=1L;
        ChunkRand chunkRand=new ChunkRand(); // instance shared among everyone to speed it up, if multithreading please create one per thread
        OverworldBiomeSource overworldBiomeSource=new OverworldBiomeSource(version,worldSeed); // overworld (dimCoeff 0)
        NetherBiomeSource netherBiomeSource =new NetherBiomeSource(version,worldSeed); // nether (dimCoeff 3 or divided by 8)
        EndBiomeSource endBiomeSource =new EndBiomeSource(version,worldSeed); //end (dimCoeff 0)
        // 10 villages closest to bpos
        System.out.println("10 villages");
        StructureHelper.getClosest(VILLAGE,bPos,worldSeed,chunkRand,overworldBiomeSource,0).limit(10).forEach(System.out::println);
        // 10 bastions closest to bpos
        System.out.println("10 bastions");
        StructureHelper.getClosest(BASTION_REMNANT,bPos,worldSeed,chunkRand,netherBiomeSource,3).limit(10).forEach(System.out::println);
        // 10 endcities closest to bpos
        System.out.println("10 end cities");
        StructureHelper.getClosest(END_CITY,bPos,worldSeed,chunkRand,endBiomeSource,0).limit(10).forEach(System.out::println);
    }
}
