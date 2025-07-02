package com.pickyboy.blingBackend.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor // 使用Lombok自动注入final字段
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // ============================ String类型操作 ============================

    /**
     * 写入缓存
     * @param key   键
     * @param value 值
     */
    public void set(final String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入缓存并设置过期时间
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(final String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 读取缓存
     * @param key 键
     * @return 值
     */
    public Object get(final String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     * @param key 键
     * @return 是否成功
     */
    public boolean delete(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 判断缓存中是否有对应的key
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(final String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // ============================ ZSet (有序集合) 操作 ============================

    /**
     * 向有序集合添加一个成员，或者更新已存在成员的分数
     * @param key    ZSet的键
     * @param member 成员
     * @param score  分数
     * @return 是否成功添加
     */
    public Boolean zAdd(String key, Object member, double score) {
        return redisTemplate.opsForZSet().add(key, member, score);
    }

    /**
     * 【核心】原子性地增加成员的分数（如果成员不存在，则添加）
     * 这是实现热点值累加的关键方法。
     * @param key    ZSet的键
     * @param member 成员
     * @param delta  要增加的分数（可以为负数）
     * @return 成员的新分数
     */
    public Double zIncrementScore(String key, Object member, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, member, delta);
    }

    /**
     * 【核心】按分数从高到低(ZREVRANGE)，获取排行榜中的成员
     * @param key   ZSet的键
     * @param start 开始位置 (0代表第一个)
     * @param end   结束位置 (-1代表最后一个)
     * @return 成员的Set集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 【核心】按分数从高到低(ZREVRANGE)，获取排行榜中的成员及其分数
     * @param key   ZSet的键
     * @param start 开始位置
     * @param end   结束位置
     * @return 包含成员和分数的TypedTuple集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    /**
     * 【新增】按分数从低到高(ZRANGE)，获取排行榜中的成员
     * @param key   ZSet的键
     * @param start 开始位置 (0代表第一个)
     * @param end   结束位置 (-1代表最后一个)
     * @return 成员的Set集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 【新增】按分数从低到高(ZRANGE)，获取排行榜中的成员及其分数
     * @param key   ZSet的键
     * @param start 开始位置
     * @param end   结束位置
     * @return 包含成员和分数的TypedTuple集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 移除有序集合中的一个或多个成员
     * @param key     ZSet的键
     * @param members 要移除的成员
     * @return 成功移除的成员数量
     */
    public Long zRemove(String key, Object... members) {
        return redisTemplate.opsForZSet().remove(key, members);
    }

    /**
     * 获取有序集合的大小
     * @param key ZSet的键
     * @return 集合大小
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }
}
