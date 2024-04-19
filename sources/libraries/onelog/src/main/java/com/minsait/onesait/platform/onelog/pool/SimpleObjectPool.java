/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.onelog.pool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SimpleObjectPool<T extends AbstractPooledObject> {

    private static final int MILLIS_PER_SECOND = 1000;

    private final BlockingQueue<T> pool = new LinkedBlockingQueue<>();
    private final Set<T> allObjects = new HashSet<>();

    private final PooledObjectFactory<T> objectFactory;
    private final int maxWaitTime;
    private final int maxLifeTime;

    public SimpleObjectPool(final PooledObjectFactory<T> objectFactory,
                            final int poolSize, final int maxWaitTime,
                            final int maxLifeTime) {

        if (poolSize < 1) {
            throw new IllegalArgumentException("poolSize must be > 0");
        }

        this.objectFactory = objectFactory;
        this.maxWaitTime = maxWaitTime;
        this.maxLifeTime = maxLifeTime < 0 ? maxLifeTime : maxLifeTime * MILLIS_PER_SECOND;

        for (int i = 0; i < poolSize; i++) {
            final T pooledObject = this.objectFactory.newInstance();
            pool.add(pooledObject);
            allObjects.add(pooledObject);
        }
    }

    @SuppressWarnings("checkstyle:illegalcatch")
    public void execute(final PooledObjectConsumer<T> consumer) throws Exception {
        T pooledObject = null;
        try {
            pooledObject = borrowObject();
            consumer.accept(pooledObject);
        } catch (final Exception e) {
            if (pooledObject != null) {
                invalidateObject(pooledObject);
                pooledObject = null;
            }

            throw e;
        } finally {
            if (pooledObject != null) {
                returnObject(pooledObject);
            }
        }
    }

    public T borrowObject() throws InterruptedException {
        final T pooledObject;
        if (maxWaitTime < 0) {
            pooledObject = pool.take();
        } else {
            pooledObject = pool.poll(maxWaitTime, TimeUnit.MILLISECONDS);

            if (pooledObject == null) {
                throw new IllegalStateException("Couldn't acquire connection from pool");
            }
        }

        return needToEvict(pooledObject) ? recycle(pooledObject) : pooledObject;
    }

    private boolean needToEvict(final T pooledObject) {
        return maxLifeTime < 0 || pooledObject.lifeTime() > maxLifeTime;
    }

    private T recycle(final T oldInstance) {
        final T newInstance = objectFactory.newInstance();
        synchronized (allObjects) {
            allObjects.remove(oldInstance);
            allObjects.add(newInstance);
        }

        oldInstance.close();
        return newInstance;
    }

    public void returnObject(final T pooledObject) {
        pool.add(pooledObject);
    }

    public void invalidateObject(final T pooledObject) {
        pool.add(recycle(pooledObject));
    }

    public void close() {
        synchronized (allObjects) {
            for (T object : allObjects) {
                object.close();
            }
        }
    }

}
