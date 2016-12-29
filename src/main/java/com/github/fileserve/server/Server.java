package com.github.fileserve.server;

import com.github.fileserve.FileRepository;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

public class Server {

    private static final Logger logger = LogManager.getLogger();
    FileRepository fileRepository;
    private int port;

    public Server(int port, String cachePath) throws Exception {
        logger.info("Server initializing...");
        this.port = port;
        fileRepository = new FileRepository(Paths.get(cachePath));
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2)
                throw new Exception("Invalid arguments, requires port:int and cachePath:String");
            else
                new Server(Integer.parseInt(args[0]), args[1]).init();
        } catch (Throwable t) {
            logger.error("Error while starting the server.", t);
        }
    }

    public void init() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerChannelInitializer(fileRepository))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            logger.info("Binding to port " + port);
            ChannelFuture f = bootstrap.bind(port).sync();
            logger.info("Server online and bound to port " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}