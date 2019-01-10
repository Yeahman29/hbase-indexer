/*
 * Copyright 2012 NGDATA nv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ngdata.sep.impl;



/**
 * Metrics for the Side-Effect Processor (SEP) system.
 */
public class SepMetrics  {

    
    public SepMetrics(String recordName) {
        
    }

    public void shutdown() {
       
    }

    /**
     * Report that a filtered SEP operation has been processed. This method should only be called to
     * report SEP operations that have been processed after making it through the filtering process.
     * 
     * @param duration The number of millisecods spent handling the SEP operation
     */
    public void reportFilteredSepOperation(long duration) {
        
    }

    /**
     * Report the original write timestamp of a SEP operation that was received. Assuming that SEP
     * operations are delivered in the same order as they are originally written in HBase (which
     * will always be the case except for when a region split or move takes place), this metric will always
     * hold the write timestamp of the most recent operation in HBase that has been handled by the SEP system.
     * 
     * @param timestamp The write timestamp of the last SEP operation
     */
    public void reportSepTimestamp(long writeTimestamp) {
        
    }

   
   
    public class SepMetricsMXBean  {
        
    }

}
