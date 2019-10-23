package hello;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.example.helloworld.GreeterGrpc;
import io.grpc.example.helloworld.HelloReply;
import io.grpc.example.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class HelloServer {
    private int port = 50051;
    private Server server;

    private void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("shutting down");
                HelloServer.this.stop();
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        HelloServer server = new HelloServer();
        server.start();
        server.blockUntilShutdown();
    }

}
