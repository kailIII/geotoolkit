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
import java.rmi.RemoteException;


/**
 * Executes {@linkplain ShareableTask shareable tasks} on one or many remote machines.
 * An instance of {@code RemoteExecutor} can have at most one master, but may have an
 * arbitrary amount of slaves (including zero, in which case the tasks are executed
 * locally).
 *
 * @author Martin Desruisseaux (Geomatys)
 * @version 3.0
 *
 * @since 3.0
 * @module
 */
class RemoteExecutor extends RemoteService implements TaskExecutor {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8679778209372812019L;

    /**
     * The name of the object to lookup from the RMI registry.
     */
    static final String NAME = "org/geotoolkit/RemoteExecutor";

    /**
     * The master, which is typically an other {@link RemoteExecutor} instance.
     */
    private final TaskExecutor master;

    /**
     * A list of executors where to delegate the work.
     */
    private final List<TaskExecutor> slaves;

    /**
     * Creates a new remote executor with the given master, which may be {@code null).
     *
     * @param  master The master if this executor is a slave, or {@code null} if this
     *         executor is itself a master.
     * @throws RemoteException If an error occured while exporting this executor.
     */
    public RemoteExecutor(final TaskExecutor master) throws RemoteException {
        super(NAME);
        slaves = new ArrayList<TaskExecutor>(1);
        this.master = master;
        if (master != null) {
            slaves.add(new LocalExecutor());
            master.slave(this, true);
        }
    }

    /**
     * Returns the name of the machine hosting this executor.
     */
    @Override
    public String name() {
        return LocalExecutor.hostname();
    }

    /**
     * Executes the given task and wait for completion before to return the value.
     *
     * @throws RemoteException If a RMI error occured.
     */
    @Override
    public synchronized <Input,Output> TaskFuture<Output> submit(final ShareableTask<Input,Output> task)
            throws RemoteException
    {
        /*
         * If this executor has no slave, add an executor which will run the tasks locally.
         * It happen only if a master got no slave, which should be uncommon.
         */
        if (slaves.isEmpty()) {
            slaves.add(new LocalExecutor());
        }
        /*
         * Now gives the tasks to every slaves.
         */
        final List<TaskFuture<Output>> futures = new ArrayList<TaskFuture<Output>>(slaves.size());
        for (final TaskExecutor slave : slaves) {
            futures.add(slave.submit(task));
        }
        return new RemoteFuture<Output>(task, futures);
    }

    /**
     * {@inheritDoc}
     *
     * @throws RemoteException If a RMI error occured.
     */
    @Override
    public synchronized void slave(final TaskExecutor slave, final boolean available)
            throws RemoteException
    {
        /*
         * A side effect of logging the message first is that if a RMI error occured,
         * the slave will not be registered. So this is a way to test the connection
         * before doing the actual registration.
         */
        logger().info((available ? "Register" : "Shutdown") + " \"" + slave.name() + "\" node.");
        if (available) {
            slaves.add(slave);
        } else {
            slaves.remove(slave);
        }
    }

    /**
     * Shutdown the executor and all its slaves.
     *
     * @throws RemoteException If a RMI error occured.
     */
    @Override
    public void shutdown() throws RemoteException {
        final TaskExecutor[] slaves;
        synchronized (this) {
            super.shutdown(); // Unbind this object before to shutdown it.
            if (master != null) {
                master.slave(this, false);
            }
            slaves = this.slaves.toArray(new TaskExecutor[this.slaves.size()]);
            this.slaves.clear();
        }
        // MUST be executed outside the synchronized block, otherwise we have deadlock.
        for (final TaskExecutor slave : slaves) {
            slave.shutdown();
        }
    }
}
