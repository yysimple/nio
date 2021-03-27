package com.wcx.nio.channel;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

/**
 * 项目: nio
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-03-27 11:56
 **/
public class ChannelTest {

    /**
     * 字符集转换测试
     */
    @Test
    public void conversionCharsetTest() throws CharacterCodingException {
        Charset cs1 = Charset.forName("GBK");

        //获取编码器
        CharsetEncoder ce = cs1.newEncoder();

        //获取解码器
        CharsetDecoder cd = cs1.newDecoder();

        CharBuffer cBuf = CharBuffer.allocate(1024);
        cBuf.put("测试字符集，test charset！");
        cBuf.flip();

        //编码
        ByteBuffer bBuf = ce.encode(cBuf);

        for (int i = 0; i < 26; i++) {
            System.out.println(bBuf.get());
        }

        //解码
        bBuf.flip();
        CharBuffer cBuf2 = cd.decode(bBuf);
        System.out.println(cBuf2.toString());

        System.out.println("------------------------------------------------------");

        Charset cs2 = Charset.forName("UTF-8");
        bBuf.flip();
        CharBuffer cBuf3 = cs2.decode(bBuf);
        System.out.println(cBuf3.toString());
    }

    /**
     * 获取管道能支持的字符集
     */
    @Test
    public void channelCharsetTest() {
        Map<String, Charset> map = Charset.availableCharsets();

        Set<Map.Entry<String, Charset>> set = map.entrySet();

        for (Map.Entry<String, Charset> entry : set) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }

    /**
     * 分散(Scatter)与聚集(Gather)
     */
    @Test
    public void scatterGatherTest() throws IOException {
        RandomAccessFile raf1 = new RandomAccessFile("d:/1.txt", "rw");

        //1. 获取通道
        FileChannel channel1 = raf1.getChannel();

        //2. 分配指定大小的缓冲区
        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);

        //3. 分散读取
        ByteBuffer[] bufs = {buf1, buf2};
        channel1.read(bufs);

        for (ByteBuffer byteBuffer : bufs) {
            byteBuffer.flip();
        }

        System.out.println(new String(bufs[0].array(), 0, bufs[0].limit()));
        System.out.println("-----------------");
        System.out.println(new String(bufs[1].array(), 0, bufs[1].limit()));

        //4. 聚集写入
        RandomAccessFile raf2 = new RandomAccessFile("d:/1.txt", "rw");
        FileChannel channel2 = raf2.getChannel();

        channel2.write(bufs);
    }

    /**
     * 通道之间的数据传输(直接缓冲区)
     */
    @Test
    public void directBufferTest() throws IOException {
        long start = System.currentTimeMillis();
        FileChannel inChannel = FileChannel.open(Paths.get("d:/1.exe"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("d:/2.exe"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

        // inChannel.transferTo(0, inChannel.size(), outChannel);
        outChannel.transferFrom(inChannel, 0, inChannel.size());

        inChannel.close();
        outChannel.close();

        long end = System.currentTimeMillis();
        // 386
        System.out.println("耗费时间为：" + (end - start));
    }

    /**
     * 使用直接缓冲区完成文件的复制(内存映射文件)
     */
    @Test
    public void directMappedBufferTest() throws IOException {
        long start = System.currentTimeMillis();
        // 这里是另外一种获取通道的方式，并将文件直接读取到通道中，后面是以只读方式
        FileChannel inChannel = FileChannel.open(Paths.get("d:/1.exe"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("d:/2.exe"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
        // 配置内存映射文件
        MappedByteBuffer inMapped = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        MappedByteBuffer outMapped = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

        byte[] bytes = new byte[inMapped.limit()];
        // 直接对缓存区进行数据读取
        inMapped.get(bytes);
        outMapped.put(bytes);

        // 关闭资源流
        inChannel.close();
        outChannel.close();

        long end = System.currentTimeMillis();
        // 1056
        System.out.println("耗费时间为：" + (end - start));


    }

    /**
     * 利用通道完成文件的复制（非直接缓冲区）
     */
    @Test
    public void noDirectBufferTest() {
        long start = System.currentTimeMillis();
        // 1. 获取文件流
        FileInputStream in = null;
        FileOutputStream out = null;
        // 2. 获取管道
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            // 3. 读取和写出操作
            in = new FileInputStream("d:/1.exe");
            out = new FileOutputStream("d:/2.exe");

            // 4. 获取管道
            inChannel = in.getChannel();
            outChannel = out.getChannel();

            // 5. 初始化缓存区
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            // 6. 将通道中的数据存入到缓存区
            while (inChannel.read(byteBuffer) != -1) {
                // 切换成读模式
                byteBuffer.flip();
                // 将缓存中的数据写到通道中
                outChannel.write(byteBuffer);
                // 清空缓存
                byteBuffer.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源连接
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();
        // 5642
        System.out.println("耗费时间为：" + (end - start));
    }
}
