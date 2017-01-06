package com.github.fileserve.client;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

public class FileClient {

    private final String host;
    private final int port;
    private final String cachePath;
    private static final Logger logger = LogManager.getLogger();


    public FileClient(String host, int port, String cachePath) {
        logger.info("Initializing client...");
        this.host = host;
        this.port = port;
        this.cachePath = cachePath;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ClientChannelInitializer(cachePath));
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) {
        try {
            Preconditions.checkArgument(args.length == 3, "Invalid arguments, requires host:String, port:int, and, cachePath:String");
            new FileClient(args[0], Integer.parseInt(args[1]), args[2]).start();
        } catch (Throwable t) {
            logger.error("Error while starting the server.", t);
        }
    }

}