package common.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class GuavaCacheManager<K, V> {

    private final Cache<K, V> cache;

    /**
     * 构造函数 - 使用默认配置
     */
    public GuavaCacheManager() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(1000)                    // 最大缓存条目数
                .expireAfterWrite(30, TimeUnit.MINUTES) // 写入后30分钟过期
                .expireAfterAccess(10, TimeUnit.MINUTES) // 访问后10分钟过期
                .recordStats()                        // 启用统计
                .build();
    }

    /**
     * 构造函数 - 自定义配置
     */
    public GuavaCacheManager(long maxSize, long expireAfterWriteMinutes, long expireAfterAccessMinutes) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
                .expireAfterAccess(expireAfterAccessMinutes, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
    public GuavaCacheManager(long maxSize,  long expireAfterAccessMinutes) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(expireAfterAccessMinutes, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 构造函数 - 完全自定义配置
     */
    public GuavaCacheManager(CacheBuilder<Object, Object> builder) {
        this.cache = builder.build();
    }

    /**
     * 放入缓存
     */
    public void put(K key, V value) {
        cache.put(key, value);
    }

    /**
     * 获取缓存值
     */
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    /**
     * 获取缓存值，如果不存在则通过 Callable 获取并放入缓存
     */
    public V get(K key, Callable<V> valueLoader) throws Exception {
        return cache.get(key, valueLoader);
    }

    /**
     * 查询键是否存在
     */
    public boolean containsKey(K key) {
        return cache.getIfPresent(key) != null;
    }

    /**
     * 移除指定键
     */
    public void remove(K key) {
        cache.invalidate(key);
    }

    /**
     * 移除多个键
     */
    public void removeAll(Iterable<K> keys) {
        cache.invalidateAll(keys);
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        cache.invalidateAll();
    }

    /**
     * 获取缓存大小
     */
    public long size() {
        return cache.size();
    }

    /**
     * 获取所有键
     */
    public Set<K> getAllKeys() {
        return cache.asMap().keySet();
    }

    /**
     * 获取所有缓存项
     */
    public Map<K, V> getAllEntries() {
        return cache.asMap();
    }

    /**
     * 手动触发缓存清理（清理过期项）
     */
    public void cleanUp() {
        cache.cleanUp();
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        return cache.stats();
    }


}
