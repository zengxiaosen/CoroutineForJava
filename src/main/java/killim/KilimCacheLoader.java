package killim;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import kilim.Pausable;
import kilim.Task;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * guava provides a CacheLoader, which is used to fill a LoadingCache. the API supports asynchronous reloading of values, but the initial loading is synchronous. as a result, non-blocking integration with kilim isn't trivial. however, by always loading an initial fake value, the synchronous portion can be bypassed
 * KilimCacheLoader extends CacheLoader and provides a static getter. this getter must be used to access the cache. otherwise, the initial asynchronous load will not be able to complete
 *
 * @param <KK>
 * @param <VV>
 */
public class KilimCacheLoader<KK, VV> extends CacheLoader<KK, VV> {

    public interface PausableFuture<KK> {
        void calcFutureValue(KK key, SettableFuture future) throws Pausable;
    }

    @Override
    public VV load(KK key) {
        return (VV) dummyValue;
    }

    private final VV dummyValue;
    private final PausableFuture pausableFuture;

    public KilimCacheLoader(VV dummyValue, PausableFuture pausableFuture) {
        this.dummyValue = dummyValue;
        this.pausableFuture = pausableFuture;

    }


    //CacheLoader.reload(K, V) 生成新的value过程中允许使用旧的value
    @Override
    public ListenableFuture reload(final KK key, VV oldValue) {
        final SettableFuture future = SettableFuture.create();
        Task.fork(() -> {
            if (pausableFuture == null) {
                calcFutureValue(key, future);
            } else {
                pausableFuture.calcFutureValue(key, future);
            }
        });
        return future;
    }

    public void calcFutureValue(KK key, SettableFuture future) throws Pausable {
        future.set(dummyValue);
    }

    public static class Getter<KK, VV> {
        LoadingCache<KK, VV> cache;
        int delay;
        VV dummyValue;

        public Getter(LoadingCache<KK, VV> cache, int delay, VV dummyValue) {
            this.cache = cache;
            this.delay = delay;
            this.dummyValue = dummyValue;
        }

        public VV get(KK key) throws Pausable {
            return getCache(cache, key, delay, dummyValue);
        }


    }

    /**
     * get a value from the cache asynchronously
     *
     * @param cache
     * @param key
     * @param delay
     * @param dummyValue
     * @return
     */
    public static <KK, VV> VV getCache(LoadingCache<KK, VV> cache, KK key, int delay, VV dummyValue) throws Pausable {
        VV result = null;
        while (true) {
            try {
                //firstly, get dummyValue, use load function( we define it to get dummyValue)
                //direct get dummyValue
                result = cache.get(key);

                if (result == dummyValue) {
                    cache.refresh(key);
                    //refresh means using reload function, here we set dummyValue
                    //so in this way bypass the synchronous initial loading
                    //refresh(user defined) -> future.set(dummyValue) -> asynchronous
                } else {
                    //if use put function in buz , will in come here, and then return
                    return result;
                }
            } catch (ExecutionException ex) {

            }
            Task.sleep(delay);
        }
    }

    public static <KK, VV> VV getCacheOneShot(LoadingCache<KK, VV> cache, KK key, int delay, VV dummyValue) throws Pausable {
        VV result = null;
        try {
            result = cache.get(key);
            if (result == dummyValue) {
                cache.refresh(key);
                Task.sleep(delay);
                return cache.get(key);
            } else {
                return result;
            }
        } catch (ExecutionException e) {

        }

        return dummyValue;
    }

    public static void main(String[] args) throws Exception {
        if (kilim.tools.Kilim.trampoline(false, args))
            return;
        Random random = new Random();

        LoadingCache<String, Integer> cache = CacheBuilder.newBuilder()
                .refreshAfterWrite(1, TimeUnit.SECONDS)
                .build(
                        new KilimCacheLoader(
                                random.nextInt(1000), null
                        ));

        Task.fork(() -> {
            while (true) {
                Getter getter = new Getter(cache, 50, 1);
                int val = (Integer) getter.get("any_key");
                //int val = KilimCacheLoader.getCache(cache,"any_key",50);
                System.out.println("value: " + val);
                Task.sleep(100);
            }

        });
    }
}
