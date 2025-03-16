package com.vtp.vipo.seller.common.dao.entity.base;

import lombok.Getter;
import lombok.Setter;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

/**
 * Distributed Sequence Generator. Inspired by Twitter snowflake: <a
 * href="https://github.com/twitter/snowflake/tree/snowflake-2010">...</a>
 *
 * <p>This class should be used as a Singleton. Make sure that you create and reuse a Single
 * instance of Snowflake per node in your distributed system cluster.
 *
 * @author haidv
 * @version 1.0
 */
@Setter
@Getter
public class Snowflake {
    private static final int UNUSED_BITS = 1; // Sign bit, Unused (always set to 0)
    private static final int EPOCH_BITS = 41;
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_NODE_ID = (1L << NODE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    // Custom Epoch (January 1, 2015 Midnight UTC = 2015-01-01T00:00:00Z)
    private static final long DEFAULT_CUSTOM_EPOCH = 1420070400000L;
    private static Snowflake instance;
    private long nodeId;
    private long customEpoch;
    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    /**
     * Private constructor to prevent instantiation.
     */
    private Snowflake() {
    }

    /**
     * Get the Snowflake instance, creating a new one if necessary. The node ID is generated
     * automatically.
     *
     * @return the Snowflake instance
     */
    public static Snowflake getInstance() {
        return getInstance(createNodeId());
    }

    /**
     * Get the Snowflake instance, creating a new one if necessary. The node ID is provided by the
     * caller.
     *
     * @param nodeId the node ID
     * @return the Snowflake instance
     */
    public static Snowflake getInstance(long nodeId) {
        return getInstance(nodeId, DEFAULT_CUSTOM_EPOCH);
    }

    /**
     * Get the Snowflake instance, creating a new one if necessary. The node ID and custom epoch are
     * provided by the caller.
     *
     * @param nodeId      the node ID
     * @param customEpoch the custom epoch
     * @return the Snowflake instance
     */
    public static Snowflake getInstance(long nodeId, long customEpoch) {
        if (instance == null) {
            Snowflake snowflake = new Snowflake();
            snowflake.setCustomEpoch(customEpoch);
            snowflake.setNodeId(nodeId);
            instance = snowflake;
        }
        return instance;
    }

    /**
     * Create a node ID based on the MAC address of the network interfaces. If the MAC address cannot
     * be obtained, a random number is used.
     *
     * @return the node ID
     */
    private static long createNodeId() {
        long nodeId;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (byte macPort : mac) {
                        sb.append(String.format("%02X", macPort));
                    }
                }
            }
            nodeId = sb.toString().hashCode();
        } catch (Exception ex) {
            nodeId = (new SecureRandom().nextInt());
        }
        nodeId = nodeId & MAX_NODE_ID;
        return nodeId;
    }

    /**
     * Generate the next ID in the sequence.
     *
     * @return the next ID
     */
    public synchronized long nextId() {
        long currentTimestamp = timestamp();

        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // Sequence Exhausted, wait till next millisecond.
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            // reset sequence to start with zero for the next millisecond
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        return currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS)
                | (nodeId << SEQUENCE_BITS)
                | sequence;
    }

    /**
     * Get the current timestamp in milliseconds, adjusted for the custom epoch.
     *
     * @return the timestamp
     */
    private long timestamp() {
        return Instant.now().toEpochMilli() - customEpoch;
    }

    /**
     * Block and wait until the next millisecond.
     *
     * @param currentTimestamp the current timestamp
     * @return the next timestamp
     */
    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }

    /**
     * Parse an ID into its components: timestamp, node ID, and sequence.
     *
     * @param id the ID to parse
     * @return an array containing the timestamp, node ID, and sequence
     */
    public long[] parse(long id) {
        long maskNodeId = ((1L << NODE_ID_BITS) - 1) << SEQUENCE_BITS;
        long maskSequence = (1L << SEQUENCE_BITS) - 1;

        long timestamp = (id >> (NODE_ID_BITS + SEQUENCE_BITS)) + customEpoch;
        long extractNodeId = (id & maskNodeId) >> SEQUENCE_BITS;
        long extractSequence = id & maskSequence;

        return new long[]{timestamp, extractNodeId, extractSequence};
    }

    @Override
    public String toString() {
        return "Snowflake Settings [EPOCH_BITS="
                + EPOCH_BITS
                + ", NODE_ID_BITS="
                + NODE_ID_BITS
                + ", SEQUENCE_BITS="
                + SEQUENCE_BITS
                + ", CUSTOM_EPOCH="
                + customEpoch
                + ", NodeId="
                + nodeId
                + "]";
    }
}
