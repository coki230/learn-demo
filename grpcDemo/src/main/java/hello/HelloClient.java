package hello;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.example.helloworld.HelloReply;

import java.util.concurrent.TimeUnit;

public class HelloClient {
    private ManagedChannel channel;
    private io.grpc.example.helloworld.GreeterGrpc.GreeterBlockingStub blockingStub;

    public HelloClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }


    public HelloClient(ManagedChannel build) {
        this.channel = build;
        blockingStub = io.grpc.example.helloworld.GreeterGrpc.newBlockingStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void greet(String name) {
        io.grpc.example.helloworld.HelloRequest request = io.grpc.example.helloworld.HelloRequest.newBuilder().setName(name).build();
        HelloReply reply = blockingStub.sayHello(request);
        System.out.println(reply.getMessage());
    }

    public static void main(String[] args) throws InterruptedException {
//        HelloClient client = new HelloClient("localhost", 50051);

        HelloClient client = new HelloClient("0.tcp.ngrok.io", 12255);
        client.greet("lyly");
        client.shutdown();
    }
}
