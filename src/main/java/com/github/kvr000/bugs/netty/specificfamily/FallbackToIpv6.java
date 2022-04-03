package com.github.kvr000.bugs.netty.specificfamily;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DuplexChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.spi.SelectorProvider;

public class FallbackToIpv6
{
	public static void main(String[] args) throws Exception
	{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
				.channelFactory(new ChannelFactory<ServerChannel>() {
					@Override
					public ServerChannel newChannel()
					{
						return new NioServerSocketChannel(SelectorProvider.provider());
					}
				})
				.childHandler(new ChannelInitializer<DuplexChannel>() {
					@Override
					protected void initChannel(DuplexChannel ch) throws Exception
					{
					}
				})
				.option(ChannelOption.SO_BACKLOG, Integer.MAX_VALUE)
				.childOption(ChannelOption.SO_KEEPALIVE, true);

		ChannelFuture bindFuture = b.bind(new InetSocketAddress(InetAddress.getByAddress(new byte[4]), 56789));
		bindFuture.sync();
		new ProcessBuilder("sh", "-c", "netstat -na | grep 56789")
				.redirectInput(ProcessBuilder.Redirect.INHERIT)
				.redirectOutput(ProcessBuilder.Redirect.INHERIT)
				.redirectError(ProcessBuilder.Redirect.INHERIT)
				.start()
				.waitFor();
	}
}