package io.codeleaf.service.engines.posix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public final class PosixServiceIOHandler {

    public interface Input {

        void doInput(ByteBuffer buffer) throws IOException;

        void doSignal(int signal) throws IOException;

    }

    public interface Output {

        void doOutput(ByteBuffer buffer) throws IOException;

        void doError(ByteBuffer buffer) throws IOException;

    }

    private final class OutputReader implements Runnable {

        private volatile boolean running = true;
        private final ByteBuffer buffer;

        public OutputReader(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
            while (running) {
                for (Map.Entry<Long, InputOutput> entry : inputOutputs.entrySet()) {
                    buffer.clear();
                    try {
                        Future<Integer> readStdout = entry.getValue().getStdoutChannel().read(buffer, 0);
                        readStdout.get(1, TimeUnit.MILLISECONDS);
                        if (buffer.hasRemaining()) {
                            entry.getValue().doOutput(buffer);
                        }
                        Future<Integer> readStderr = entry.getValue().getStderrChannel().read(buffer, 0);
                        readStderr.get(1, TimeUnit.MILLISECONDS);
                        if (buffer.hasRemaining()) {
                            entry.getValue().doError(buffer);
                        }
                    } catch (InterruptedException | ExecutionException | TimeoutException | IOException cause) {
                        cause.printStackTrace();
                        if (!running) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private static final class InputOutput implements Input, Output {

        private final long pid;
        private final AsynchronousFileChannel stdinChannel;
        private final AsynchronousFileChannel stdoutChannel;
        private final AsynchronousFileChannel stderrChannel;
        private final Set<PosixPipedServiceConnection> connections = new CopyOnWriteArraySet<>();

        public InputOutput(long pid, AsynchronousFileChannel stdinChannel, AsynchronousFileChannel stdoutChannel, AsynchronousFileChannel stderrChannel) {
            this.pid = pid;
            this.stdinChannel = stdinChannel;
            this.stdoutChannel = stdoutChannel;
            this.stderrChannel = stderrChannel;
        }

        public AsynchronousFileChannel getStdinChannel() {
            return stdinChannel;
        }

        public AsynchronousFileChannel getStdoutChannel() {
            return stdoutChannel;
        }

        public AsynchronousFileChannel getStderrChannel() {
            return stderrChannel;
        }

        public void addConnection(PosixPipedServiceConnection connection) {
            connections.add(connection);
        }

        public void removeConnection(PosixPipedServiceConnection connection) {
            connections.remove(connection);
        }

        @Override
        public void doInput(ByteBuffer buffer) throws IOException {
            stdinChannel.write(buffer, stdinChannel.size());
        }

        @Override
        public void doSignal(int signal) throws IOException {
            PosixSignals.signal(signal, pid);
        }

        @Override
        public void doOutput(ByteBuffer buffer) throws IOException {
            for (Output output : connections) {
                output.doOutput(buffer);
            }
        }

        @Override
        public void doError(ByteBuffer buffer) throws IOException {
            for (Output output : connections) {
                output.doError(buffer);
            }
        }
    }

    private final Map<Long, InputOutput> inputOutputs = new ConcurrentHashMap<>();
    private OutputReader outputReader;
    private Thread workerThread;

    public static PosixServiceIOHandler create() {
        return new PosixServiceIOHandler();
    }

    public void init() {
        outputReader = new OutputReader(ByteBuffer.allocate(64 * 1024));
        workerThread = new Thread(outputReader);
        workerThread.setName("OutputReader");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void shutdown() {
        outputReader.running = false;
        workerThread.interrupt();
    }

    public void registerService(long pid, AsynchronousFileChannel stdinChannel, AsynchronousFileChannel stdoutChannel, AsynchronousFileChannel stderrChannel) throws IOException {
        InputOutput inputOutput = new InputOutput(pid, stdinChannel, stdoutChannel, stderrChannel);
        inputOutputs.put(pid, inputOutput);
    }

    public void connect(PosixPipedServiceConnection connection) {
        InputOutput inputOutput = inputOutputs.get(connection.getPid());
        connection.setInput(inputOutput);
        inputOutput.connections.add(connection);
    }

    public void disconnect(PosixPipedServiceConnection connection) {
        inputOutputs.get(connection.getPid()).connections.remove(connection);
    }

    public void unregisterService(long pid) throws IOException {
        InputOutput inputOutput = inputOutputs.remove(pid);
        if (inputOutput == null) {
            return;
        }
        inputOutput.stdinChannel.close();
        inputOutput.stdoutChannel.close();
        inputOutput.stderrChannel.close();
        for (PosixPipedServiceConnection connection : inputOutput.connections) {
            connection.close();
        }
    }
}
