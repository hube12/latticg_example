package latticg_example;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.seedutils.mc.MCVersion;

import java.util.ArrayList;

public class BiomeCrack {

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
            if (biome== Biome.WARM_OCEAN || biome==Biome.DEEP_WARM_OCEAN || biome==Biome.DEEP_LUKEWARM_OCEAN) return 0;
            if (biome.getCategory() == this.biome.getCategory()) {
                return biome == this.biome ? 1 : 0.5;
            }
            return 0;

        }
    }

    public static void crackBiomes() {
        long seed = 74912709064725L;
        ArrayList<BiomeData> biomeDatas = new ArrayList<>();
        biomeDatas.add(new BiomeData(1*16, 4*16, Biome.OCEAN));
        for (long i = 0; i < (1L << 16); i++) {
            long cur = (i << 48) | seed;
            boolean good = true;
            double score = 0.0;
            OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, cur);
            for (BiomeData data : biomeDatas) {
                double s = data.check(source);
                if (s == 0) {
                    good = false;
                    break;
                }
                score += s;
            }
            if (good && score > biomeDatas.size() - 2) System.out.println("Good seed: " + cur + " with a score of " + score + " out of " + biomeDatas.size());
        }
    }

    public static void main(String[] args) {
        crackBiomes();
    }
}
