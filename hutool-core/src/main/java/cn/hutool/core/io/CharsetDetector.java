package cn.hutool.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;

/**
 * 编码探测器
 *
 * @author looly
 * @since 5.4.7
 */
public class CharsetDetector {

    /**
     * 默认的参与测试的编码
     */
    private static final Charset[] DEFAULT_CHARSETS;

    static {
        String[] names =
                {"UTF-8", "GBK", "GB2312", "GB18030", "UTF-16BE", "UTF-16LE", "UTF-16", "BIG5", "UNICODE", "US-ASCII"};
        DEFAULT_CHARSETS = Convert.convert(Charset[].class, names);
    }

    /**
     * 探测文件编码
     *
     * @param file     文件
     * @param charsets 需要测试用的编码，null或空使用默认的编码数组
     * @return 编码
     * @since 5.6.7
     */
    public static Charset detect(File file, Charset... charsets) {
        return detect(FileUtil.getInputStream(file), charsets);
    }

    /**
     * 探测编码<br>
     * 注意：此方法会读取流的一部分，然后关闭流，如重复使用流，请使用支持reset方法的流
     *
     * @param in       流，使用后关闭此流
     * @param charsets 需要测试用的编码，null或空使用默认的编码数组
     * @return 编码
     */
    public static Charset detect(InputStream in, Charset... charsets) {
        return detect(IoUtil.DEFAULT_LARGE_BUFFER_SIZE, in, charsets);
    }

    /**
     * 探测编码<br>
     * 注意：此方法会读取流的一部分，然后关闭流，如重复使用流，请使用支持reset方法的流
     *
     * @param bufferSize 自定义缓存大小，即每次检查的长度
     * @param in         流，使用后关闭此流
     * @param charsets   需要测试用的编码，null或空使用默认的编码数组
     * @return 编码
     * @since 5.7.10
     */
    public static Charset detect(int bufferSize, InputStream in, Charset... charsets) {
        if (ArrayUtil.isEmpty(charsets)) {
            charsets = DEFAULT_CHARSETS;
        }

        int len;
        final byte[] buffer = new byte[bufferSize];
        try {
            while ((len = in.read(buffer)) > -1) {
                for (Charset charset : charsets) {
                    final CharsetDecoder decoder = charset.newDecoder();
                    if (identify(buffer, len, decoder)) {
                        return charset;
                    }
                }
            }
        } catch (IOException e) {
            throw new IORuntimeException(e);
        } finally {
            IoUtil.close(in);
        }
        return null;
    }

    /**
     * 探测byte数组可能的编码，由于存在中文被截断的情况，因此调用者应当尝试使用不同的len来进行探测。
     * 例如：先探测bytes.length个字节，如果返回null，再探测bytes.length-1个字节，以此类推，目前最长的字符编码是4个字节，
     * 因此推算到bytes.length-3个字节即可停止。
     *
     * @param bytes 待探测的byte数组，长度越大探测越准确
     * @param len 要探测的byte数组长度
     * @param probeCharsets  需要测试用的编码，尽量使用自己指定的编码，本类的默认UTF-16容易误判
     * @return 编码，探测失败返回null
     */
    public static Charset detect(byte[] bytes, int len, Charset... probeCharsets) {
        if (ArrayUtil.isEmpty(probeCharsets)) {
            probeCharsets = DEFAULT_CHARSETS;
        }
        for (Charset charset : probeCharsets) {
            final CharsetDecoder decoder = charset.newDecoder();
            if (identify(bytes, len, decoder)) {
                return charset;
            }
        }
        return null;
    }

    /**
     * 通过try的方式测试指定bytes是否可以被解码，从而判断是否为指定编码
     *
     * @param bytes   测试的bytes
     * @param decoder 解码器
     * @return 是否是指定编码
     */
    private static boolean identify(byte[] bytes, int len, CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes, 0, len));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }
}
