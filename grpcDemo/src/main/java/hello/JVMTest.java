package hello;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.skywalking.apm.network.common.CPU;
import org.apache.skywalking.apm.network.language.agent.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class JVMTest {
    public static void main(String[] args) throws IOException {
        tel();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("172.21.45.117", 12800).usePlaintext(true).build();

        JVMMetricsServiceGrpc.JVMMetricsServiceBlockingStub stub = JVMMetricsServiceGrpc.newBlockingStub(channel);

        send(stub);
    }

    private static void tel() throws IOException {
        TelnetClient telnetClient = new TelnetClient("vt200");  //指明Telnet终端类型，否则会返回来的数据中文会乱码
        telnetClient.setDefaultTimeout(5000); //socket延迟时间：5000ms
        telnetClient.connect("127.0.0.1",12800);  //建立一个连接,默认端口是23
        InputStream inputStream = telnetClient.getInputStream(); //读取命令的流
        PrintStream pStream = new PrintStream(telnetClient.getOutputStream());  //写命令的流
        byte[] b = new byte[1024];
        int size;
        StringBuffer sBuffer = new StringBuffer(300);
        while(true) {     //读取Server返回来的数据，直到读到登陆标识，这个时候认为可以输入用户名
            size = inputStream.read(b);
            if(-1 != size) {
                sBuffer.append(new String(b,0,size));
                if(sBuffer.toString().trim().endsWith("login:")) {
                    break;
                }
            }
        }
        System.out.println(sBuffer.toString());
        pStream.println("exit"); //写命令
        pStream.flush(); //将命令发送到telnet Server
        if(null != pStream) {
            pStream.close();
        }
        telnetClient.disconnect();
    }


    private static void send(JVMMetricsServiceGrpc.JVMMetricsServiceBlockingStub stub) {
        JVMMetrics.Builder jvmMetrics = JVMMetrics.newBuilder();
        jvmMetrics.setApplicationInstanceId(12);

        JVMMetric.Builder jvmMetricBuilder = JVMMetric.newBuilder();
        jvmMetricBuilder.setTime(System.currentTimeMillis());

        buildCPUMetric(jvmMetricBuilder);
        buildGCMetric(jvmMetricBuilder);
        buildMemoryMetric(jvmMetricBuilder);
        buildMemoryPoolMetric(jvmMetricBuilder);

        jvmMetrics.addMetrics(jvmMetricBuilder);

        stub.collect(jvmMetrics.build());
    }

    private static void buildMemoryPoolMetric(JVMMetric.Builder metricBuilder) {
        MemoryPool.Builder codeCache = MemoryPool.newBuilder();
        codeCache.setInit(10);
        codeCache.setMax(100);
        codeCache.setCommited(10);
        codeCache.setUsed(50);
        codeCache.setType(PoolType.CODE_CACHE_USAGE);
        metricBuilder.addMemoryPool(codeCache);

        MemoryPool.Builder newGen = MemoryPool.newBuilder();
        newGen.setInit(10);
        newGen.setMax(100);
        newGen.setCommited(10);
        newGen.setUsed(50);
        newGen.setType(PoolType.NEWGEN_USAGE);
        metricBuilder.addMemoryPool(newGen);

        MemoryPool.Builder oldGen = MemoryPool.newBuilder();
        oldGen.setInit(10);
        oldGen.setMax(100);
        oldGen.setCommited(10);
        oldGen.setUsed(50);
        oldGen.setType(PoolType.OLDGEN_USAGE);
        metricBuilder.addMemoryPool(oldGen);

        MemoryPool.Builder survivor = MemoryPool.newBuilder();
        survivor.setInit(10);
        survivor.setMax(100);
        survivor.setCommited(10);
        survivor.setUsed(50);
        survivor.setType(PoolType.SURVIVOR_USAGE);
        metricBuilder.addMemoryPool(survivor);

        MemoryPool.Builder permGen = MemoryPool.newBuilder();
        permGen.setInit(10);
        permGen.setMax(100);
        permGen.setCommited(10);
        permGen.setUsed(50);
        permGen.setType(PoolType.PERMGEN_USAGE);
        metricBuilder.addMemoryPool(permGen);

        MemoryPool.Builder metaSpace = MemoryPool.newBuilder();
        metaSpace.setInit(10);
        metaSpace.setMax(100);
        metaSpace.setCommited(10);
        metaSpace.setUsed(50);
        metaSpace.setType(PoolType.METASPACE_USAGE);
        metricBuilder.addMemoryPool(metaSpace);
    }

    private static void buildMemoryMetric(JVMMetric.Builder metricBuilder) {
        Memory.Builder isHeap = Memory.newBuilder();
        isHeap.setInit(20);
        isHeap.setMax(100);
        isHeap.setCommitted(20);
        isHeap.setUsed(60);
        isHeap.setIsHeap(true);
        metricBuilder.addMemory(isHeap);

        Memory.Builder nonHeap = Memory.newBuilder();
        nonHeap.setInit(20);
        nonHeap.setMax(100);
        nonHeap.setCommitted(20);
        nonHeap.setUsed(60);
        nonHeap.setIsHeap(false);
        metricBuilder.addMemory(nonHeap);
    }

    private static void buildGCMetric(JVMMetric.Builder metricBuilder) {
        GC.Builder newGC = GC.newBuilder();
        newGC.setPhrase(GCPhrase.NEW);
        newGC.setCount(2);
        newGC.setTime(1000);
        metricBuilder.addGc(newGC);

        GC.Builder oldGC = GC.newBuilder();
        oldGC.setPhrase(GCPhrase.OLD);
        oldGC.setCount(4);
        oldGC.setTime(49);
        metricBuilder.addGc(oldGC);
    }

    private static void buildCPUMetric(JVMMetric.Builder metricBuilder) {
        CPU.Builder cpu = CPU.newBuilder();
        cpu.setUsagePercent(20);
        metricBuilder.setCpu(cpu.build());
    }
}
