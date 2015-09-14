/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.schema.statistics;

/**
 * @author Pavol Mederly
 */
public class GenericStatisticsData {

    private int count;
    private long totalDuration;
    private Integer minDuration = null;
    private Integer maxDuration = null;

    public int getCount() {
        return count;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public int getMinDuration() {
        return minDuration != null ? minDuration : 0;
    }

    public int getMaxDuration() {
        return maxDuration != null ? maxDuration : 0;
    }

    public void recordOperation(int duration, int count) {
        this.count += count;
        totalDuration += duration;
        if (minDuration == null || minDuration > duration) {
            minDuration = duration;
        }
        if (maxDuration == null || maxDuration < duration) {
            maxDuration = duration;
        }
    }
}