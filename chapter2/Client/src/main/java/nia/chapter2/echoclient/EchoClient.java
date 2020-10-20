package nia.chapter2.echoclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Listing 2.4 Main class for the client
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class EchoClient {
    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start()
        throws Exception {
        System.out.println("trying connection");
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = getBootstrap(group);
//        try {

            ChannelFuture f = b.connect().addListener((ChannelFuture cf) -> {
                if(!cf.isSuccess()) {

                    cf.channel().eventLoop().schedule(new ReconnectTask(cf),5, TimeUnit.SECONDS);
                    System.out.println("We failed to connect riop");
                    System.out.println("Trying connection again");
                }
                if(cf.isCancelled()) {
                    System.out.println("Cancelled");
                }
                if(cf.isVoid()){
                    System.out.println("void");
                }
            }).await();
        System.out.println("1");

//            f.channel().closeFuture().addListener((ChannelFuture cf) -> {
//                System.out.println("Blarg");
//                if (cf.isSuccess()) {
//                    cf.channel().eventLoop().schedule(new ReconnectTask(cf),1, TimeUnit.SECONDS);
//                }
//            }).
//                    sync();
            f.channel().closeFuture().sync();
        System.out.println("2");
  //      } finally {
            System.out.println("shutting down");

            //group.shutdownGracefully().sync();

        }
    //}

    private Bootstrap getBootstrap(EventLoopGroup group) {
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .remoteAddress(new InetSocketAddress(host, port))
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch)
                    throws Exception {
                    ch.pipeline().addLast(new ReadTimeoutHandler(10))
                            .addLast(new EchoClientHandler());
                }
            });
        return b;
    }

    public static void main(String[] args)
            throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: " + EchoClient.class.getSimpleName() +
                    " <host> <port>"
            );
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        new EchoClient(host, port).start();
    }

    class ReconnectTask implements Runnable {
        ChannelFuture cf;

        public ReconnectTask(ChannelFuture cf) {
            this.cf = cf;
        }

        @Override
        public void run() {
            cf.channel().eventLoop().schedule(() -> {
                System.out.println("start");
                try {
                    start();
                } catch (Exception e) {
                    System.out.println("Connection Failed Trying Reconnect");
                    e.printStackTrace();
                }
            },5, TimeUnit.SECONDS);
        }
    }
}

