package killim;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import kilim.Pausable;

import java.util.concurrent.TimeUnit;

public class GoroutineCacheDemo {

    public static void main(String[] args) throws Exception {


        Cache<String, Integer> cache = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.SECONDS)
                .maximumSize(2000).build();

        CoroutineCache<String, Integer> cacheHelper = new CoroutineCache<String, Integer>(cache);

        cacheHelper.getCache().put("any_key", 1);
        int value = cacheHelper.get("any_key", new CoroutineCache.XCallable<Integer>() {
            @Override
            protected Integer loadFromRemote() throws Pausable, Exception {
                return anyPausableMethod();
            }
        });
        System.out.println("value: " + value);

    }

    private static Integer anyPausableMethod() throws Pausable {
        return 2;
    }
}
