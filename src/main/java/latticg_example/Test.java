package latticg_example;

import com.seedfinding.latticg.RandomReverser;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) {
        RandomReverser dynamicProgram = new RandomReverser();
        // my constraints
        dynamicProgram.addNextFloatCall(0.0f,0.0001f); // pitch
        dynamicProgram.consumeNextFloatCalls(1);
        dynamicProgram.addNextFloatCall(0.999999f,1f); // width
        dynamicProgram.addNextIntCall(28,0,0);

//        pitch = (nextFloat(&fakeseed) - 0.5F) * 2.0F / 8.0F;
//        temp = nextFloat(&fakeseed);
//        width = (temp*2.0F + nextFloat(&fakeseed))*2.0F;
//        maxLength = 112L - nextInt(&fakeseed, 28);
        long start = System.nanoTime();
        ArrayList<Long> seeds=new ArrayList<>();
        AtomicInteger count = new AtomicInteger(0);
        dynamicProgram.setVerbose(true);
        dynamicProgram.findAllValidSeeds().forEach(e -> {
//            System.out.println(e+"L,");
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
}
