package com.wcx.nio.buffer;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * 项目: nio
 * <p>
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-03-27 10:53
 *
 *
 * 一、缓冲区（Buffer）：在 Java NIO 中负责数据的存取。缓冲区就是数组。用于存储不同数据类型的数据
 *  *
 *  * 根据数据类型不同（boolean 除外），提供了相应类型的缓冲区：
 *  * ByteBuffer
 *  * CharBuffer
 *  * ShortBuffer
 *  * IntBuffer
 *  * LongBuffer
 *  * FloatBuffer
 *  * DoubleBuffer
 *  *
 *  * 上述缓冲区的管理方式几乎一致，通过 allocate() 获取缓冲区
 *  *
 *  * 二、缓冲区存取数据的两个核心方法：
 *  * put() : 存入数据到缓冲区中
 *  * get() : 获取缓冲区中的数据
 *  *
 *  * 三、缓冲区中的四个核心属性：
 *  * capacity : 容量，表示缓冲区中最大存储数据的容量。一旦声明不能改变。
 *  * limit : 界限，表示缓冲区中可以操作数据的大小。（limit 后数据不能进行读写）
 *  * position : 位置，表示缓冲区中正在操作数据的位置。
 *  *
 *  * mark : 标记，表示记录当前 position 的位置。可以通过 reset() 恢复到 mark 的位置
 *  *
 *  * 0 <= mark <= position <= limit <= capacity
 *  *
 *  * 四、直接缓冲区与非直接缓冲区：
 *  * 非直接缓冲区：通过 allocate() 方法分配缓冲区，将缓冲区建立在 JVM 的内存中
 *  * 直接缓冲区：通过 allocateDirect() 方法分配直接缓冲区，将缓冲区建立在物理内存中。可以提高效率
 **/
public class BufferTest {

    /**
     * 直接缓存区
     */
    @Test
    public void bufferDirectTest(){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        System.out.println(byteBuffer.isDirect());
    }

    /**
     * 测试mark方法
     */
    @Test
    public void markTest(){
        String str = "abcde";
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(str.getBytes());

        byte[] dst = new byte[byteBuffer.limit()];
        byteBuffer.flip();
        byteBuffer.get(dst, 0, 2);
        // 2
        System.out.println(byteBuffer.position());

        // 标记position为2的位置
        byteBuffer.mark();
        byteBuffer.get(dst, 2, 2);
        // 4
        System.out.println(byteBuffer.position());

        // 重置到标记的位置
        byteBuffer.reset();
        // 2
        System.out.println(byteBuffer.position());

        // 判断缓存中是否还设有数据
        if (byteBuffer.hasRemaining()){
            System.out.println("缓存中剩余的数据数量：" + byteBuffer.remaining());
        }
    }

    /**
     * 测试position、capacity、limit三者值，在不同阶段的变化
     */
    @Test
    public void threeForPCLTest() {
        String str = "abcde";
        // 1. 分配一个指定的缓冲区大小
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        // 2. 刚初始化时：position、capacity、limit
        System.out.println("=========== 初始化时的三者值 ============");
        // position = 0
        System.out.println(byteBuffer.position());
        // capacity = 1024
        System.out.println(byteBuffer.capacity());
        // limit = 1024
        System.out.println(byteBuffer.limit());

        // 3. 添加数据到缓存区
        byteBuffer.put(str.getBytes());
        System.out.println("=========== 添加数据后的三者值 ============");
        // position = 5
        System.out.println(byteBuffer.position());
        // capacity = 1024
        System.out.println(byteBuffer.capacity());
        // limit = 1024
        System.out.println(byteBuffer.limit());

        // 4. 切换成读模式
        byteBuffer.flip();
        System.out.println("=========== 切换成读模式后的三者值 ============");
        // position = 0
        System.out.println(byteBuffer.position());
        // capacity = 1024
        System.out.println(byteBuffer.capacity());
        // limit = 5
        System.out.println(byteBuffer.limit());

        // 5. 利用 get() 读取缓冲区中的数据
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes);
        System.out.println("=========== 读取数据后的三者值 ============");
        // position = 5
        System.out.println(byteBuffer.position());
        // capacity = 1024
        System.out.println(byteBuffer.capacity());
        // limit = 5
        System.out.println(byteBuffer.limit());
        System.out.println(new String(bytes, 0, bytes.length));

        // 6. rewind() : 可重复读
        byteBuffer.rewind();
        System.out.println("=========== 设置成可重复读后的三者值 ============");
        // position = 0
        System.out.println(byteBuffer.position());
        // capacity = 1024
        System.out.println(byteBuffer.capacity());
        // limit = 5
        System.out.println(byteBuffer.limit());

        // 7. clear() : 清空缓冲区. 但是缓冲区中的数据依然存在，但是处于“被遗忘”状态
        byteBuffer.clear();
        System.out.println("=========== 清除缓存区后的三者值 ============");
        // position = 0
        System.out.println(byteBuffer.position());
        // capacity = 1024
        System.out.println(byteBuffer.capacity());
        // limit = 1024
        System.out.println(byteBuffer.limit());
        System.out.println((char) byteBuffer.get());
    }
}
