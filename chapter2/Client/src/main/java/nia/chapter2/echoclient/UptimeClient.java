package nia.chapter2.echoclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public final class UptimeClient {
    static final String HOST = "127.0.0.1";
    static final int PORT = 8080;
    static final int RECONNECT_DELAY = 5;
    private static final int READ_TIMEOUT = 10;

    private static final UptimeClientHandler handler = new UptimeClientHandler();
    private static final SecondaryHandler secondHandler = new SecondaryHandler();
    private static Channel channel;
    private static EventLoopGroup group = new NioEventLoopGroup();

    private static final Bootstrap bs = new Bootstrap();

    public static void main(String[] args) throws InterruptedException {

        bs.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(HOST, PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(READ_TIMEOUT, 0, 0))
                                .addLast(handler)
                                .addLast(secondHandler);
                    }
                });
        channel = bs.connect().channel();
        System.out.println("done with initial connection attempt");
    }

    static void reconnect() throws InterruptedException {
        if (!ShutdownState.getInstance().shutdownEnabled) {
            bs.connect().sync();
        } else {
            System.out.println("Shutdown is good to go");
            channel.close().awaitUninterruptibly();
            group.shutdownGracefully();

        }

    }
}
