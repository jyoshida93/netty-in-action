package nia.chapter2.echoclient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class UptimeClientHandler extends SimpleChannelInboundHandler<Object> {
    long startTime = -1;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (startTime < 0) {
            startTime = System.currentTimeMillis();
        }
        ctx.write("hi");
        System.out.println("active");

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("from UpTimeClientHandler");
        ctx.write(in);
        ctx.fireChannelRead(in);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        System.out.println("from channelRead0");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }
        IdleStateEvent e = (IdleStateEvent) evt;
        if (e.state() == IdleState.READER_IDLE) {
            System.out.println("no inbound traffic");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Inactive bois");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//        if (!ShutdownState.getInstance().shutdownEnabled) {
        System.out.println("unregistered trying to reconnect");
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("Running reconnect task");
                try {
                    UptimeClient.reconnect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ;
            }
        }, UptimeClient.RECONNECT_DELAY, TimeUnit.SECONDS);
    }

//        else {
//            System.out.println("special place");
//            ctx.channel().close().awaitUninterruptibly();
//            ctx.channel().eventLoop().shutdownGracefully();
//        }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
