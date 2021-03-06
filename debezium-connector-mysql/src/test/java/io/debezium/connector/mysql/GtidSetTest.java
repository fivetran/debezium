/*
 * Copyright Debezium Authors.
 * 
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.mysql;

import java.util.LinkedList;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

import io.debezium.connector.mysql.GtidSet.Interval;
import io.debezium.connector.mysql.GtidSet.UUIDSet;

/**
 * @author Randall Hauch
 *
 */
public class GtidSetTest {
    
    private static final String UUID1 = "24bc7850-2c16-11e6-a073-0242ac110002";

    private GtidSet gtids;
    
    @Test
    public void shouldCreateSetWithSingleInterval() {
        gtids = new GtidSet(UUID1 + ":1-191");
        asertIntervalCount(UUID1,1);
        asertIntervalExists(UUID1,1,191);
        asertFirstInterval(UUID1,1,191);
        asertLastInterval(UUID1,1,191);
        assertThat(gtids.toString()).isEqualTo(UUID1 + ":1-191");
    }
    
    @Test
    public void shouldCollapseAdjacentIntervals() {
        gtids = new GtidSet(UUID1 + ":1-191:192-199");
        asertIntervalCount(UUID1,1);
        asertIntervalExists(UUID1,1,199);
        asertFirstInterval(UUID1,1,199);
        asertLastInterval(UUID1,1,199);
        assertThat(gtids.toString()).isEqualTo(UUID1 + ":1-199");
    }

    
    @Test
    public void shouldNotCollapseNonAdjacentIntervals() {
        gtids = new GtidSet(UUID1 + ":1-191:193-199");
        asertIntervalCount(UUID1,2);
        asertFirstInterval(UUID1,1,191);
        asertLastInterval(UUID1,193,199);
        assertThat(gtids.toString()).isEqualTo(UUID1 + ":1-191:193-199");
    }
    
    @Test
    public void shouldCreateWithMultipleIntervals() {
        gtids = new GtidSet(UUID1 + ":1-191:193-199:1000-1033");
        asertIntervalCount(UUID1,3);
        asertFirstInterval(UUID1,1,191);
        asertIntervalExists(UUID1,193,199);
        asertLastInterval(UUID1,1000,1033);
        assertThat(gtids.toString()).isEqualTo(UUID1 + ":1-191:193-199:1000-1033");
    }
    
    @Test
    public void shouldCreateWithMultipleIntervalsThatMayBeAdjacent() {
        gtids = new GtidSet(UUID1 + ":1-191:192-199:1000-1033:1035-1036:1038-1039");
        asertIntervalCount(UUID1, 4);
        asertFirstInterval(UUID1, 1, 199);
        asertIntervalExists(UUID1, 1000, 1033);
        asertIntervalExists(UUID1, 1035, 1036);
        asertLastInterval(UUID1, 1038, 1039);
        assertThat(gtids.toString()).isEqualTo(UUID1 + ":1-199:1000-1033:1035-1036:1038-1039"); // ??
    }
    
    protected void asertIntervalCount( String uuid, int count) {
        UUIDSet set = gtids.forServerWithId(uuid);
        assertThat(set.getIntervals().size()).isEqualTo(count);
    }
    
    protected void asertIntervalExists( String uuid, int start, int end) {
        assertThat(hasInterval(uuid,start,end)).isTrue();
    }
    
    protected void asertFirstInterval( String uuid, int start, int end) {
        UUIDSet set = gtids.forServerWithId(uuid);
        Interval interval = set.getIntervals().iterator().next();
        assertThat(interval.getStart()).isEqualTo(start);
        assertThat(interval.getEnd()).isEqualTo(end);
    }
    
    protected void asertLastInterval( String uuid, int start, int end) {
        UUIDSet set = gtids.forServerWithId(uuid);
        Interval interval = new LinkedList<>(set.getIntervals()).getLast();
        assertThat(interval.getStart()).isEqualTo(start);
        assertThat(interval.getEnd()).isEqualTo(end);
    }
    
    protected boolean hasInterval( String uuid, int start, int end) {
        UUIDSet set = gtids.forServerWithId(uuid);
        for ( Interval interval : set.getIntervals() ) {
            if ( interval.getStart() == start && interval.getEnd() == end ) return true;
        }
        return false;
    }

}
