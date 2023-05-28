package de.florianmichael.dietrichevents;

import de.florianmichael.dietrichevents.handle.EventExecutor;
import de.florianmichael.dietrichevents.handle.Listener;
import org.openjdk.jmh.infra.Blackhole;

public interface BenchmarkListener extends Listener {

    void onBenchmark(final Blackhole blackhole);

    class BenchmarkEvent extends AbstractEvent<BenchmarkListener> {
        private final EventExecutor<BenchmarkListener> eventExecutor;

        public BenchmarkEvent(final Blackhole blackhole) {
            this.eventExecutor = listener -> listener.onBenchmark(blackhole);
        }

        @Override
        public EventExecutor<BenchmarkListener> getEventExecutor() {
            return this.eventExecutor;
        }

        @Override
        public Class<BenchmarkListener> getListenerType() {
            return BenchmarkListener.class;
        }
    }
}
