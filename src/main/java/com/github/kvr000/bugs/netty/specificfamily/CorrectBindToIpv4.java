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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolFamily;
import java.net.StandardProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

public class CorrectBindToIpv4
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
						return new NioServerSocketChannel(new SelectorProvider()
						{
							@Override
							public DatagramChannel openDatagramChannel() throws IOException
							{
								return null;
							}

							@Override
							public DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException
							{
								return null;
							}

							@Override
							public Pipe openPipe() throws IOException
							{
								return null;
							}

							@Override
							public AbstractSelector openSelector() throws IOException
							{
								return null;
							}

							@Override
							public ServerSocketChannel openServerSocketChannel() throws IOException
							{
								return SelectorProvider.provider().openServerSocketChannel(StandardProtocolFamily.INET);
							}

							@Override
							public SocketChannel openSocketChannel() throws IOException
							{
								return null;
							}
						});
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