/*
 * This file is part of DietrichEvents - https://github.com/FlorianMichael/DietrichEvents
 * Copyright (C) 2023 FlorianMichael/EnZaXD and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.florianmichael.dietrichevents;

import de.florianmichael.dietrichevents.handle.Listener;
import org.openjdk.jmh.infra.Blackhole;

public interface BenchmarkListener extends Listener {

    void onBenchmark(final Blackhole blackhole);

    class BenchmarkEvent extends AbstractEvent<BenchmarkListener> {
        private final Blackhole blackhole;

        public BenchmarkEvent(final Blackhole blackhole) {
            this.blackhole = blackhole;
        }

        @Override
        public void call(BenchmarkListener listener) {
            listener.onBenchmark(blackhole);
        }

        @Override
        public Class<BenchmarkListener> getListenerType() {
            return BenchmarkListener.class;
        }
    }
}
