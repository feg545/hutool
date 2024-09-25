package cn.hutool.core.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.CharsetUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CharsetDetectorTest {

    @Test
    public void detectTest() {
        // 测试多个Charset对同一个流的处理是否有问题
        final Charset detect = CharsetDetector.detect(ResourceUtil.getStream("test.xml"),
                CharsetUtil.CHARSET_GBK, CharsetUtil.CHARSET_UTF_8);
        assertEquals(CharsetUtil.CHARSET_UTF_8, detect);
    }

    @Test
    @Disabled
    public void issue2547() {
        final Charset detect = CharsetDetector.detect(IoUtil.DEFAULT_LARGE_BUFFER_SIZE,
                ResourceUtil.getStream("d:/test/default.txt"));
        assertEquals(CharsetUtil.CHARSET_UTF_8, detect);
    }

    @Test
    public void detectBytes() {
        String text =
                "`Hutool`是一个功能丰富且易用的Java工具库，通过诸多实用工具类的使用，旨在帮助开发者快速、便捷地完成各类开发任务。";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        Charset charset = CharsetDetector.detect(bytes, bytes.length);
        assertEquals(StandardCharsets.UTF_8, charset);

        bytes = text.getBytes(Charset.forName("GBK"));
        charset = CharsetDetector.detect(bytes, bytes.length);
        assertEquals(Charset.forName("GBK"), charset);
    }
}
