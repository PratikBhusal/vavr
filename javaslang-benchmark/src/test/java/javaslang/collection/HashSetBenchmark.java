package javaslang.collection;

import javaslang.JmhRunner;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;

import static javaslang.JmhRunner.getRandomValues;

public class HashSetBenchmark {
    static final Array<Class<?>> CLASSES = Array.of(
            Add.class,
            Iterate.class
    );

    @Test
    public void testAsserts() {
        JmhRunner.runDebugWithAsserts(CLASSES);
    }

    public static void main(String... args) {
        JmhRunner.runNormalNoAsserts(CLASSES);
    }

    @State(Scope.Benchmark)
    public static class Base {
        @Param({ "10", "100", "1000" })
        public int CONTAINER_SIZE;

        int EXPECTED_AGGREGATE;
        Integer[] ELEMENTS;
        Set<Integer> SET;

        @SuppressWarnings("unchecked")
        scala.collection.immutable.HashSet<Integer> scalaPersistent = (scala.collection.immutable.HashSet<Integer>) scala.collection.immutable.HashSet$.MODULE$.empty();
        org.pcollections.PSet<Integer> pcollectionsPersistent = org.pcollections.HashTreePSet.empty();
        javaslang.collection.Set<Integer> slangPersistent = javaslang.collection.HashSet.empty();

        @Setup
        public void setup() {
            ELEMENTS = getRandomValues(CONTAINER_SIZE, 0);

            SET = TreeSet.of(ELEMENTS);
            EXPECTED_AGGREGATE = SET.reduce(JmhRunner::aggregate);

            for (int value : SET) {
                pcollectionsPersistent = pcollectionsPersistent.plus(value);
                scalaPersistent = scalaPersistent.$plus(value);
            }
            slangPersistent = javaslang.collection.HashSet.ofAll(SET);

            assert SET.forAll(v -> pcollectionsPersistent.contains(v))
                   && SET.forAll(v -> scalaPersistent.contains(v))
                   && SET.forAll(v -> slangPersistent.contains(v));
        }
    }

    public static class Add extends Base {
        @Benchmark
        public Object pcollections_persistent() {
            org.pcollections.PSet<Integer> values = org.pcollections.HashTreePSet.empty();
            for (Integer element : ELEMENTS) {
                values = values.plus(element);
            }
            assert SET.forAll(values::contains);
            return values;
        }

        @Benchmark
        public Object scala_persistent() {
            scala.collection.immutable.HashSet<Integer> values = new scala.collection.immutable.HashSet<>();
            for (Integer element : ELEMENTS) {
                values = values.$plus(element);
            }
            assert SET.forAll(values::contains);
            return values;
        }

        @Benchmark
        public Object slang_persistent() {
            javaslang.collection.Set<Integer> values = javaslang.collection.HashSet.empty();
            for (Integer element : ELEMENTS) {
                values = values.add(element);
            }
            assert SET.forAll(values::contains);
            return values;
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static class Iterate extends Base {
        @Benchmark
        public int scala_persistent() {
            int aggregate = 0;
            for (final scala.collection.Iterator<Integer> iterator = scalaPersistent.iterator(); iterator.hasNext(); ) {
                aggregate ^= iterator.next();
            }
            assert aggregate == EXPECTED_AGGREGATE;
            return aggregate;
        }

        @Benchmark
        public int pcollections_persistent() {
            int aggregate = 0;
            for (final java.util.Iterator<Integer> iterator = pcollectionsPersistent.iterator(); iterator.hasNext(); ) {
                aggregate ^= iterator.next();
            }
            assert aggregate == EXPECTED_AGGREGATE;
            return aggregate;
        }

        @Benchmark
        public int slang_persistent() {
            int aggregate = 0;
            for (final javaslang.collection.Iterator<Integer> iterator = slangPersistent.iterator(); iterator.hasNext(); ) {
                aggregate ^= iterator.next();
            }
            assert aggregate == EXPECTED_AGGREGATE;
            return aggregate;
        }
    }
}