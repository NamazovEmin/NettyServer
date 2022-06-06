package com.geekbrains.cloud.netty.handler;

import com.geekbrains.cloud.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;
    private Path serverPath;

    public CloudFileHandler() {
        currentDir = Path.of("server_files");
        serverPath = Path.of("server_files").toAbsolutePath();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListFiles(currentDir));
        System.out.println(serverPath);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof FileRequest fileRequest) {
            ctx.writeAndFlush(new FileMessage(serverPath.resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage fileMessage) {
            Files.write(serverPath.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(serverPath));
        }
        else if (cloudMessage instanceof PathUpRequest) {
           if (serverPath.getParent() != null) {
               serverPath = serverPath.getParent();
               ctx.writeAndFlush(new ListFiles(serverPath));
           }
        } else if (cloudMessage instanceof PathInRequest pathInRequest) {
               if (Files.isDirectory(serverPath.resolve(pathInRequest.getFileName()))) {
                   serverPath = serverPath.resolve(pathInRequest.getFileName());
                   ctx.writeAndFlush(new ListFiles(serverPath));
               }
        } else if (cloudMessage instanceof PathFindRequest pathFindRequest) {
            if (Files.isDirectory(Path.of(pathFindRequest.getFileName()))) {
                serverPath = Path.of(pathFindRequest.getFileName());
                ctx.writeAndFlush(new ListFiles(serverPath));
            }
        }
    }
}
