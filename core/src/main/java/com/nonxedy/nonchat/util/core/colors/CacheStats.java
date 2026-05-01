package com.nonxedy.nonchat.util.core.colors;

public record CacheStats( 
        Entry normalizeCache,
        Entry componentCache,
        Entry colorCache
) {
    public record Entry(long hits, long misses, int size) {
        public double hitRate() {
            long total = hits + misses;
            return total == 0 ? 0.0 : (double) hits / total;
        }

        @Override
        public String toString() {
            return String.format("size=%d, hits=%d, misses=%d, hitRate=%.1f%%",
                    size, hits, misses, hitRate() * 100);
        }
    }
}
