package jarg.jrcm.rpc.invocation;

import jarg.jrcm.rpc.packets.AbstractRpcPacket;

import java.util.concurrent.ExecutorService;

/**
 * An {@link RpcOperationInvocator} that submits an operation task to a thread pool, instead of invoking
 * the operation directly.
 */
public abstract class AbstractThreadPoolInvocator implements RpcOperationInvocator {

    private ExecutorService workersExecutor;

    public AbstractThreadPoolInvocator(ExecutorService workersExecutor) {
        this.workersExecutor = workersExecutor;
    }

    /**
     * Invokes the operation as a task submitted to a thread pool.
     * @param packet The packet which contains information necessary to invoke
     *               the operation.
     */
    @Override
    public void invokeOperation(AbstractRpcPacket packet){
        // submit task to a worker's pool
        workersExecutor.submit(() -> invokeOperationTask(packet));
    }

    /**
     * Specifies how to invoke an operation.
     * @param packet The packet which contains information necessary to invoke
     *               the operation.
     */
    public abstract void invokeOperationTask(AbstractRpcPacket packet);

    public ExecutorService getWorkersExecutor() {
        return workersExecutor;
    }
}
