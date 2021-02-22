package latticg_example;

import com.seedfinding.latticg.reversal.DynamicProgram;
import com.seedfinding.latticg.reversal.calltype.java.JavaCalls;
import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.mc.MCVersion;
import mjtb49.hashreversals.ChunkRandomReverser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
//        pitch = (nextFloat(&fakeseed) - 0.5F) * 2.0F / 8.0F;
//        temp = nextFloat(&fakeseed);
//        width = (temp*2.0F + nextFloat(&fakeseed))*2.0F;
//        maxLength = 112L - nextInt(&fakeseed, 28);
public class MagmaSeed {
    public static  LCG previous1=LCG.JAVA.combine(-1);
    public static  LCG previous5=LCG.JAVA.combine(-5);
    public static void main(String[] args) {
        DynamicProgram dynamicProgram = DynamicProgram.create(com.seedfinding.latticg.util.LCG.JAVA);
        // my constraints
        dynamicProgram.add(JavaCalls.nextFloat().betweenII(.0f,0.0001f)); // pitch
        dynamicProgram.add(JavaCalls.nextFloat().betweenII(0.999f,1f)); //yaw ignored
        dynamicProgram.add(JavaCalls.nextFloat().betweenII(0.999f,1f)); //
        dynamicProgram.add(JavaCalls.nextInt(28).betweenII(0,0)); //width

        long start = System.nanoTime();
        ArrayList<Long> seeds=new ArrayList<>();
        AtomicInteger count = new AtomicInteger(0);
        dynamicProgram.reverse().forEach(e -> {
            seeds.add(e);
            count.incrementAndGet();
        });
        if (count.get() == 1)
            System.out.println("Found " + count + " seed.");
        else System.out.println("Found " + count + " seeds.");
        long end = System.nanoTime();
        System.out.printf("elapsed: %.2fs%n", (end - start) * 1e-9);
        MagmaSeed.backwards(seeds);
    }

    public static void backwards(ArrayList<Long> seeds){
        Randy randy=new Randy();
        ArrayList<Long> godSeeds=new ArrayList<>();
        for (long seed:seeds){
            randy.setSeed(seed^ LCG.JAVA.multiplier);
            long previousSeed=previous5.nextSeed(seed);
            godSeeds.add(previousSeed);
        }
        for (long seed:godSeeds){
            if (checkCondition(seed)) {
                forward(seed);
                System.out.println("Found ^: " + previous1.nextSeed(seed));
                reverseCarverSeed(previous1.nextSeed(seed));
            }
        }
    }


    public static  boolean checkCondition(long seed){
        LCG previous=LCG.JAVA.combine(-1);
        Randy randy=new Randy(previous.nextSeed(seed)^LCG.JAVA.multiplier);
        return randy.nextFloat()<0.02f;
    }



    public static void forward(long seed){
        // raw seedd
        Randy randy=new Randy(seed^ LCG.JAVA.multiplier);
        int magmax= randy.nextInt(16);
        int partially=randy.nextInt(40)+8;
        int magmay=20+ randy.nextInt(partially);
        int magmaz= randy.nextInt(16);
        randy.nextFloat();
        float pitch = (randy.nextFloat() - 0.5F) * 2.0F / 8.0F;
        float temp = randy.nextFloat();
        float width = (temp*2.0F + randy.nextFloat())*2.0F;
        int maxLength = 112 - randy.nextInt(28);
        System.out.printf("Y %d Pitch %f Width %f maxLength %d%n",magmay,pitch,width,maxLength);

    }

    public static void reverseCarverSeed(long seed) {
        int bound=1;
        for (int x = -bound; x < bound; x++) {
            for (int z = -bound; z < bound; z++) {
                List<Long> longList= ChunkRandomReverser.reverseCarverSeed(seed,x,z, MCVersion.v1_16);
                if (!longList.isEmpty()) {
                    int finalX = x;
                    int finalZ = z;
                    longList.forEach(e-> System.out.println(((e-1L)^0x5deece66dL)+" "+ finalX +" "+ finalZ));
                }
            }
        }
    }
}
