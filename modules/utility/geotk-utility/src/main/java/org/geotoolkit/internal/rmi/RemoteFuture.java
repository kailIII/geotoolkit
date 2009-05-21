/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.internal.rmi;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/**
 * The pending result of a task submited to a {@link RemoteExecutor}.
 *
 * @param <Output> The return value of the task.
 *
 * @author Martin Desruisseaux (Geomatys)
 * @version 3.0
 *
 * @since 3.0
 * @module
 */
final class RemoteFuture<Output> extends UnicastRemoteObject implements TaskFuture<Output> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 7868765562360450526L;

    /**
     * The task for which results are being computed.
     */
    private final ShareableTask<?,Output> task;

    /**
     * The futures to be aggregated and returned by this wrapper.
     */
    private final List<TaskFuture<Output>> futures;

    /**
     * The result, computed when first required.
     */
    private transient Output output;

    /**
     * Creates a new remote future.
     *
     * @param  task The task for which results are being computed.
     * @param  future The future to be returned by this wrapper.
     * @throws RemoteException If an error occured while exporting this future.
     */
    RemoteFuture(final ShareableTask<?,Output> task, final List<TaskFuture<Output>> futures) throws RemoteException {
        this.task = task;
        this.futures = futures;
    }

    /**
     * Returns the task result.
     *
     * @throws RemoteException If a RMI error occured.
     * @throws ExecutionException If an error occured during the execution of the task.
     * @throws InterruptedException If the worker thread was interrupted while waiting.
     */
    @Override
    public synchronized Output get() throws RemoteException, ExecutionException, InterruptedException {
        if (output == null) {
            final List<Output> results = new ArrayList<Output>(futures.size());
            for (final TaskFuture<Output> future : futures) {
                results.add(future.get());
            }
            output = task.aggregate(results);
        }
        return output;
    }
}
