/*
 * This file is part of DietrichEvents - https://github.com/FlorianMichael/DietrichEvents
 * Copyright (C) 2023-2024 FlorianMichael/EnZaXD <florian.michael07@gmail.com> and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.florianmichael.dietrichevents.enums;

/**
 * This class is optional and does not have to be used
 */
public class Priorities {
    public final static int LOWEST = -2;
    public final static int LOW = -1;
    public final static int NONE = 0;
    public final static int HIGH = 1;
    public final static int HIGHEST = 2;

    public final static int MONITOR = Integer.MAX_VALUE;
    public final static int FIRST = Integer.MIN_VALUE;
}
