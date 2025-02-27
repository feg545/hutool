package cn.hutool.crypto.symmetric;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PBKDF2Test {

	@Test
	public void encryptTest(){
		final String s = SecureUtil.pbkdf2("123456".toCharArray(), RandomUtil.randomBytes(16));
		assertEquals(128, s.length());
	}
}
