package cn.hutool.core.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * 自动检测字符编码的Reader
 *
 * @author Fulai
 * @since 2024-09-25
 */
public class CharsetDetectReader extends Reader {
    private static final int DETECT_LENGTH = 8192;
    private final int detectLength;
    private final InputStream in;
    private final Charset[] probeCharsets;
    private InputStreamReader isr;

    /**
     * 创建字符自动检测Reader
     *
     * @param in 输入流
     */
    public CharsetDetectReader(InputStream in) {
        this(in, DETECT_LENGTH);
    }

    /**
     * 创建字符自动检测Reader
     *
     * @param in            输入流
     * @param probeCharsets 探测失败使用的字符编码，可指定为US-ASCII和UTF-8以外的编码
     */
    public CharsetDetectReader(InputStream in, Charset... probeCharsets) {
        this(in, DETECT_LENGTH, probeCharsets);
    }

    /**
     * 创建字符自动检测Reader
     *
     * @param in            输入流
     * @param detectLength  读取头部字节数，长度越大探测越准确，但需要考虑内存是否足够
     * @param probeCharsets 探测失败使用的字符编码，可指定为US-ASCII和UTF-8以外的编码
     */
    public CharsetDetectReader(InputStream in, int detectLength, Charset... probeCharsets) {
        this.in = in;
        this.detectLength = detectLength;
        this.probeCharsets = probeCharsets;
    }

    private void ensureReader() throws IOException {
        if (isr == null) {
            //支持标记
            InputStream markStream;
            if (in.markSupported()) {
                markStream = in;
            } else {
                markStream = new BufferedInputStream(in);
            }

            //标记位置
            markStream.mark(detectLength);
            //读取头部字节
            byte[] buffer = new byte[detectLength];
            //实际读取到的长度
            int len = markStream.read(buffer);
            //重置流读取位置
            markStream.reset();

            Charset charset = CharsetDetector.detect(buffer, len, probeCharsets);
            //未探测到编码，又未读取到完整的流内容，可能是中文字符被截断了，尝试缩短字节再次探测
            if (charset == null && len == detectLength) {
                for (int i = 1; i < 4; i++) {
                    charset = CharsetDetector.detect(buffer, len - i, probeCharsets);
                    if (charset != null) {
                        break;
                    }
                }
            }

            if (charset == null) {
                throw new NullPointerException("can't detect charset coding");
            }
            this.isr = new InputStreamReader(markStream, charset);
        }
    }

    @Override
    public int read() throws IOException {
        ensureReader();
        return isr.read();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        ensureReader();
        return isr.read(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        if (isr != null) {
            isr.close();
        } else {
            in.close();
        }
    }
}
