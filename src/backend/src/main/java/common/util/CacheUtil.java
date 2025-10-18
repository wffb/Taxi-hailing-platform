package common.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.cache.GuavaCacheManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CacheUtil {

    public static final GuavaCacheManager<String, Object> LoginUserCache = new GuavaCacheManager(
            1000, // maxSize
            60 // expireAFTERAccessMinutes
    );

    public static final Cache<String, String> GeneralCache =
            CacheBuilder. newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();



    public static String tryLock(String key) {
        String lockId = UUID.randomUUID().toString();

        // atomic operation: only set value if it doesn't exist
            //get concurrent map
        String existing = GeneralCache.asMap().putIfAbsent(key, lockId);

        return (existing == null) ? lockId : null;
    }


    //reuse and refresh the lock
    public static String truLock(String key, String lockId) {

        if(lockId == null)
            return null;

        // atomic operation: only set value if it matches the lockId
        if(GeneralCache.asMap().get(key).equals(lockId)){
            String newLockId = UUID.randomUUID().toString();
            boolean res =  GeneralCache.asMap().replace(key, lockId, newLockId);

            if(res)
                return newLockId;
        }

        return null;
    }


    public static boolean releaseLock(String key, String lockId) {
        if (lockId == null) {
            return false;
        }

        // atomic operation: only remove value if it matches the lockId
        //get concurrent map
        return GeneralCache.asMap().remove(key, lockId);
    }


}
