package latticg_example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;


public
class Randy implements java.io.Serializable {

    static final long serialVersionUID = 3905348978240129619L;

    public void printMessage(String message) {
        System.out.println(message + " " +  this.getCounter() + " " + this.getSeed());
    }

    private final AtomicLong seed;

    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;

    private static final double DOUBLE_UNIT = 0x1.0p-53;

    private long counter = 0;
    static final String BadBound = "bound must be positive";
    static final String BadRange = "bound must be greater than origin";
    static final String BadSize = "size must be non-negative";


    public Randy() {
        this(seedUniquifier() ^ System.nanoTime());
    }

    private static long seedUniquifier() {


        for (; ; ) {
            long current = seedUniquifier.get();
            long next = current * 1181783497276652981L;
            if (seedUniquifier.compareAndSet(current, next))
                return next;
        }
    }

    public long getSeed() {
        return seed.get();

    }

    private static final AtomicLong seedUniquifier
            = new AtomicLong(8682522807148012L);


    public Randy(long seed) {
        if (getClass() == Randy.class)
            this.seed = new AtomicLong(initialScramble(seed));
        else {

            this.seed = new AtomicLong();
            setSeed(seed);
        }
    }

    public static void swap(List<?> list, int i, int j) {
        // instead of using a raw type here, it's possible to capture
        // the wildcard but it will require a call to a supplementary
        // private method
        final List l = list;
        l.set(i, l.set(j, l.get(i)));
    }

    private static void swap(Object[] arr, int i, int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    private static final int SHUFFLE_THRESHOLD = 5;

    public static void shuffle(List<?> list, Randy rnd) {
        int size = list.size();
        if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
            for (int i = size; i > 1; i--)
                swap(list, i - 1, rnd.nextInt(i));
        } else {
            Object[] arr = list.toArray();

            // Shuffle array
            for (int i = size; i > 1; i--)
                swap(arr, i - 1, rnd.nextInt(i));

            // Dump array back into list
            // instead of using a raw type here, it's possible to capture
            // the wildcard but it will require a call to a supplementary
            // private method
            ListIterator it = list.listIterator();
            for (Object e : arr) {
                it.next();
                it.set(e);
            }
        }
    }

    public static void shuffle(List<?> list) {
        Randy rnd = new Randy();
        int size = list.size();
        if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
            for (int i = size; i > 1; i--)
                swap(list, i - 1, rnd.nextInt(i));
        } else {
            Object[] arr = list.toArray();

            // Shuffle array
            for (int i = size; i > 1; i--)
                swap(arr, i - 1, rnd.nextInt(i));

            // Dump array back into list
            // instead of using a raw type here, it's possible to capture
            // the wildcard but it will require a call to a supplementary
            // private method
            ListIterator it = list.listIterator();
            for (Object e : arr) {
                it.next();
                it.set(e);
            }
        }
    }

    private static long initialScramble(long seed) {
        return (seed ^ multiplier) & mask;
    }


    public synchronized void setSeed(long seed) {
        counter = 0;
        this.seed.set(initialScramble(seed));
        haveNextNextGaussian = false;
    }


    protected int next(int bits) {

        long oldseed, nextseed;
        AtomicLong seed = this.seed;
        do {
            oldseed = seed.get();
            nextseed = (oldseed * multiplier + addend) & mask;
        } while (!seed.compareAndSet(oldseed, nextseed));
        counter++;
        return (int) (nextseed >>> (48 - bits));
    }

    public long getCounter() {
        return counter;
    }

    public void nextBytes(byte[] bytes) {
        for (int i = 0, len = bytes.length; i < len; )
            for (int rnd = nextInt(),
                 n = Math.min(len - i, Integer.SIZE / Byte.SIZE);
                 n-- > 0; rnd >>= Byte.SIZE)
                bytes[i++] = (byte) rnd;
    }


    final long internalNextLong(long origin, long bound) {
        long r = nextLong();
        if (origin < bound) {
            long n = bound - origin, m = n - 1;
            if ((n & m) == 0L)
                r = (r & m) + origin;
            else if (n > 0L) {
                for (long u = r >>> 1;
                     u + m - (r = u % n) < 0L;
                     u = nextLong() >>> 1)
                    ;
                r += origin;
            } else {
                while (r < origin || r >= bound)
                    r = nextLong();
            }
        }
        return r;
    }


    final int internalNextInt(int origin, int bound) {
        if (origin < bound) {
            int n = bound - origin;
            if (n > 0) {
                return nextInt(n) + origin;
            } else {
                int r;
                do {
                    r = nextInt();
                } while (r < origin || r >= bound);
                return r;
            }
        } else {
            return nextInt();
        }
    }


    final double internalNextDouble(double origin, double bound) {
        double r = nextDouble();
        if (origin < bound) {
            r = r * (bound - origin) + origin;
            if (r >= bound)
                r = Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
        }
        return r;
    }


    public int nextInt() {
        return next(32);
    }


    public int nextInt(int bound) {
        if (bound <= 0)
            throw new IllegalArgumentException(BadBound);

        int r = next(31);
        int m = bound - 1;
        if ((bound & m) == 0)
            r = (int) ((bound * (long) r) >> 31);
        else {
            for (int u = r;
                 u - (r = u % bound) + m < 0;
                 u = next(31))
                ;
        }
        return r;
    }


    public long nextLong() {

        return ((long) (next(32)) << 32) + next(32);
    }


    public boolean nextBoolean() {
        return next(1) != 0;
    }


    public float nextFloat() {
        return next(24) / ((float) (1 << 24));
    }


    public double nextDouble() {
        return (((long) (next(26)) << 27) + next(27)) * DOUBLE_UNIT;
    }

    private double nextNextGaussian;
    private boolean haveNextNextGaussian = false;


    public synchronized double nextGaussian() {

        if (haveNextNextGaussian) {
            haveNextNextGaussian = false;
            return nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                v1 = 2 * nextDouble() - 1;
                v2 = 2 * nextDouble() - 1;
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
            nextNextGaussian = v2 * multiplier;
            haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }


    public IntStream ints(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.intStream
                (new RandyIntsSpliterator
                                (this, 0L, streamSize, Integer.MAX_VALUE, 0),
                        false);
    }


    public IntStream ints() {
        return StreamSupport.intStream
                (new RandyIntsSpliterator
                                (this, 0L, Long.MAX_VALUE, Integer.MAX_VALUE, 0),
                        false);
    }


    public IntStream ints(long streamSize, int RandyNumberOrigin,
                          int RandyNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        if (RandyNumberOrigin >= RandyNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.intStream
                (new RandyIntsSpliterator
                                (this, 0L, streamSize, RandyNumberOrigin, RandyNumberBound),
                        false);
    }


    public IntStream ints(int RandyNumberOrigin, int RandyNumberBound) {
        if (RandyNumberOrigin >= RandyNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.intStream
                (new RandyIntsSpliterator
                                (this, 0L, Long.MAX_VALUE, RandyNumberOrigin, RandyNumberBound),
                        false);
    }


    public LongStream longs(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.longStream
                (new RandyLongsSpliterator
                                (this, 0L, streamSize, Long.MAX_VALUE, 0L),
                        false);
    }


    public LongStream longs() {
        return StreamSupport.longStream
                (new RandyLongsSpliterator
                                (this, 0L, Long.MAX_VALUE, Long.MAX_VALUE, 0L),
                        false);
    }


    public LongStream longs(long streamSize, long RandyNumberOrigin,
                            long RandyNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        if (RandyNumberOrigin >= RandyNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.longStream
                (new RandyLongsSpliterator
                                (this, 0L, streamSize, RandyNumberOrigin, RandyNumberBound),
                        false);
    }


    public LongStream longs(long RandyNumberOrigin, long RandyNumberBound) {
        if (RandyNumberOrigin >= RandyNumberBound)
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.longStream
                (new RandyLongsSpliterator
                                (this, 0L, Long.MAX_VALUE, RandyNumberOrigin, RandyNumberBound),
                        false);
    }


    public DoubleStream doubles(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        return StreamSupport.doubleStream
                (new RandyDoublesSpliterator
                                (this, 0L, streamSize, Double.MAX_VALUE, 0.0),
                        false);
    }


    public DoubleStream doubles() {
        return StreamSupport.doubleStream
                (new RandyDoublesSpliterator
                                (this, 0L, Long.MAX_VALUE, Double.MAX_VALUE, 0.0),
                        false);
    }


    public DoubleStream doubles(long streamSize, double RandyNumberOrigin,
                                double RandyNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BadSize);
        if (!(RandyNumberOrigin < RandyNumberBound))
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.doubleStream
                (new RandyDoublesSpliterator
                                (this, 0L, streamSize, RandyNumberOrigin, RandyNumberBound),
                        false);
    }


    public DoubleStream doubles(double RandyNumberOrigin, double RandyNumberBound) {
        if (!(RandyNumberOrigin < RandyNumberBound))
            throw new IllegalArgumentException(BadRange);
        return StreamSupport.doubleStream
                (new RandyDoublesSpliterator
                                (this, 0L, Long.MAX_VALUE, RandyNumberOrigin, RandyNumberBound),
                        false);
    }


    static final class RandyIntsSpliterator implements Spliterator.OfInt {
        final Randy rng;
        long index;
        final long fence;
        final int origin;
        final int bound;

        RandyIntsSpliterator(Randy rng, long index, long fence,
                             int origin, int bound) {
            this.rng = rng;
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandyIntsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                    new RandyIntsSpliterator(rng, i, index = m, origin, bound);
        }

        public long estimateSize() {
            return fence - index;
        }

        public int characteristics() {
            return (Spliterator.SIZED | Spliterator.SUBSIZED |
                    Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public boolean tryAdvance(IntConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                consumer.accept(rng.internalNextInt(origin, bound));
                index = i + 1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(IntConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                index = f;
                Randy r = rng;
                int o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextInt(o, b));
                } while (++i < f);
            }
        }
    }


    static final class RandyLongsSpliterator implements Spliterator.OfLong {
        final Randy rng;
        long index;
        final long fence;
        final long origin;
        final long bound;

        RandyLongsSpliterator(Randy rng, long index, long fence,
                              long origin, long bound) {
            this.rng = rng;
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandyLongsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                    new RandyLongsSpliterator(rng, i, index = m, origin, bound);
        }

        public long estimateSize() {
            return fence - index;
        }

        public int characteristics() {
            return (Spliterator.SIZED | Spliterator.SUBSIZED |
                    Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public boolean tryAdvance(LongConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                consumer.accept(rng.internalNextLong(origin, bound));
                index = i + 1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(LongConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                index = f;
                Randy r = rng;
                long o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextLong(o, b));
                } while (++i < f);
            }
        }

    }


    static final class RandyDoublesSpliterator implements Spliterator.OfDouble {
        final Randy rng;
        long index;
        final long fence;
        final double origin;
        final double bound;

        RandyDoublesSpliterator(Randy rng, long index, long fence,
                                double origin, double bound) {
            this.rng = rng;
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandyDoublesSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null :
                    new RandyDoublesSpliterator(rng, i, index = m, origin, bound);
        }

        public long estimateSize() {
            return fence - index;
        }

        public int characteristics() {
            return (Spliterator.SIZED | Spliterator.SUBSIZED |
                    Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public boolean tryAdvance(DoubleConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                consumer.accept(rng.internalNextDouble(origin, bound));
                index = i + 1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(DoubleConsumer consumer) {
            if (consumer == null) throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                index = f;
                Randy r = rng;
                double o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextDouble(o, b));
                } while (++i < f);
            }
        }
    }


    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("seed", Long.TYPE),
            new ObjectStreamField("nextNextGaussian", Double.TYPE),
            new ObjectStreamField("haveNextNextGaussian", Boolean.TYPE)
    };


    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {

        ObjectInputStream.GetField fields = s.readFields();


        long seedVal = fields.get("seed", -1L);
        if (seedVal < 0)
            throw new java.io.StreamCorruptedException(
                    "Randy: invalid seed");
        resetSeed(seedVal);
        nextNextGaussian = fields.get("nextNextGaussian", 0.0);
        haveNextNextGaussian = fields.get("haveNextNextGaussian", false);
    }


    private synchronized void writeObject(ObjectOutputStream s)
            throws IOException {


        ObjectOutputStream.PutField fields = s.putFields();


        fields.put("seed", seed.get());
        fields.put("nextNextGaussian", nextNextGaussian);
        fields.put("haveNextNextGaussian", haveNextNextGaussian);


        s.writeFields();
    }


    private void resetSeed(long seedVal) {

    }
}
