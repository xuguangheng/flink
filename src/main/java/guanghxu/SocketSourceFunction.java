package guanghxu;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.table.data.RowData;
import org.apache.flink.types.Row;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SocketSourceFunction extends RichSourceFunction<RowData> implements ResultTypeQueryable<RowData>, CheckpointedFunction {

    private final String hostname;
    private final int port;
    private final byte byteDelimiter;
    private final DeserializationSchema<RowData> deserializer;

    private volatile boolean isRunning = true;
    private Socket currentSocket;
    private transient ListState<Map<String, Row>> states;

    private Map<String, Row> rowMap = new HashMap<>();

    public SocketSourceFunction(String hostname, int port, byte byteDelimiter, DeserializationSchema<RowData> deserializer) {
        this.hostname = hostname;
        this.port = port;
        this.byteDelimiter = byteDelimiter;
        this.deserializer = deserializer;
    }

    @Override
    public TypeInformation<RowData> getProducedType() {
        return deserializer.getProducedType();
    }

    @Override
    public void open(Configuration parameters) throws Exception {

    }

    @Override
    public void run(SourceContext<RowData> ctx) throws Exception {
        while (isRunning) {
            // open and consume from socket
            try (final Socket socket = new Socket()) {
                currentSocket = socket;
                socket.connect(new InetSocketAddress(hostname, port), 0);
                try (InputStream stream = socket.getInputStream()) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int b;
                    while ((b = stream.read()) >= 0) {
                        // buffer until delimiter
                        if (b != byteDelimiter) {
                            buffer.write(b);
                        }
                        // decode and emit record
                        else {
                            byte[] raw = buffer.toByteArray();
//                            List<RowData> rows = ((ChangelogCsvDeserializer)deserializer).deserializeCustomer(raw, rowMap);
//                            rows.forEach(ctx::collect);
                            ctx.collect(deserializer.deserialize(raw));
                            buffer.reset();
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace(); // print and continue
            }
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
        try {
            currentSocket.close();
        } catch (Throwable t) {
            // ignore
        }
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        states.clear();
        states.add(rowMap);
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        ListStateDescriptor<Map<String, Row>> descriptor =
                new ListStateDescriptor<>(
                        "key-map",
                        TypeInformation.of(new TypeHint<Map<String, Row>>() {}));

        states = context.getOperatorStateStore().getListState(descriptor);

        if (context.isRestored()) {
            states.get().forEach(rowMap::putAll);
        }
    }
}