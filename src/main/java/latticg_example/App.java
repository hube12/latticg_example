package latticg_example;

import com.seedfinding.latticg.reversal.DynamicProgram;
import com.seedfinding.latticg.reversal.calltype.java.JavaCalls;
import com.seedfinding.latticg.util.LCG;
import kaptainwutax.biomeutils.device.Main;
import kaptainwutax.seedutils.lcg.rand.Rand;
import kaptainwutax.seedutils.mc.MCVersion;
import mjtb49.hashreversals.ChunkRandomReverser;

import java.util.concurrent.atomic.AtomicInteger;

public class App {
    public void reverse12NextDoubleCalls(){
        DynamicProgram dynamicProgram=DynamicProgram.create(LCG.JAVA);
        // my constraints
        for (int i = 0; i < 12; i++) {
            dynamicProgram.add(JavaCalls.nextDouble().betweenII(0.0D,0.1D));
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

    public void useChunkRandomReversal(){
        long decoratorSeed=1234L;
        int posX=100;
        int posZ=100;
        MCVersion version= MCVersion.v1_16;
        ChunkRandomReverser.reversePopulationSeed(decoratorSeed ^ LCG.JAVA.multiplier, posX >> 4, posZ >> 4,version).forEach(s -> {
            System.out.println("Found world seed: "+s);
        });
    }
    public static void main(String[] args) {
        App app=new App();
        app.useChunkRandomReversal();
        app.reverse12NextDoubleCalls();

    }
}
