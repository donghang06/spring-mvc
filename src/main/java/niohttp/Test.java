package niohttp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Test {

    private static void httpHandle(SelectionKey key) throws Exception {
        if(key.isAcceptable()){
            acceptHandle(key);
        }else if(key.isReadable()){
            requestHandle(key);
        }
    }

    private static void acceptHandle(SelectionKey key) throws IOException {
        SocketChannel sc = ((ServerSocketChannel)key.channel()).accept();
        sc.configureBlocking(false);
        sc.register(key.selector(),SelectionKey.OP_READ, ByteBuffer.allocate(1024));//注册为可读
    }

    private static void requestHandle(SelectionKey key) throws Exception {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = (ByteBuffer)key.attachment();
        byteBuffer.clear();
        if(sc.read(byteBuffer) == -1){
            sc.close();
            return;
        }
        byteBuffer.flip();
        String requestMsg = new String(byteBuffer.array());
        String url = requestMsg.split("\r\n")[0].split(" ")[1];
        System.out.println(requestMsg);
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\r\n");
        sb.append("Content-Type:text/html;charset=utf-8\r\n");
        sb.append("\r\n");
        sb.append("<html><head><title>HttpTest</title></head><body>");

        String content = Server.praseUrl(url);
        sb.append(content !=null?content:"404");
        sb.append("</body></html>");

        sc.write(ByteBuffer.wrap(sb.toString().getBytes()));
        sc.close();
    }


    public static void main(String[] args) throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(80));//绑定端口
        ssc.configureBlocking(false);//设置为非阻塞方式
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);//注册ssc到selector，监听accept请求
        System.out.println("Http Server Started");
        Server.scanpkg("niohttp");
        while(true){
            if(selector.select(3000)==0){//每3s检测一次，若无请求
                continue;
            }

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();//遍历有请求的socket
            while(keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                httpHandle(key);
                keyIterator.remove();
            }
        }


    }
}
