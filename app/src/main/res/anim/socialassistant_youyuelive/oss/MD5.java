package com.socialassistant_youyuelive.oss;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * Created by Administrator on 2017/5/10.
 */
public class MD5 {
    // 第一次加密后，结果转换成小写，对结果再加密一次
    private static final byte PADDING[] = { -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0 };

    private static long state[];
    private static long count[];
    private static byte buffer[];
    private static byte digest[];

    public synchronized static byte[] MD5(byte[] src, int len) {
        md5Init();
        md5Update(src, len);
        md5Final();

        return digest;
    }

    public synchronized static String MD5(String str) {
        md5Init();
        byte[] src = null;
        try {
            src = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        md5Update(src, src.length);
        md5Final();

        return byte2HexString(digest);
    }

    public static String byte2HexString(byte[] md5) {
        StringBuffer sb = new StringBuffer(64);
        for (int i = 0; i < md5.length; i++) {
            String tmp = Integer.toHexString(md5[i] & 0xff);
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            sb.append(tmp);
        }
        return sb.toString();
    }

    public static byte[] hexString2Byte(String hex) {
        if (hex == null) {
            return null;
        }
        byte[] buf = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            buf[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return buf;
    }

    private static void md5Init() {
        state = new long[4];
        count = new long[2];
        buffer = new byte[64];
        digest = new byte[16];

        // 初始化部分变量；
        count[0] = 0L;
        count[1] = 0L;
        state[0] = 0x67452301L;
        state[1] = 0xefcdab89L;
        state[2] = 0x98badcfeL;
        state[3] = 0x10325476L;
    }

    private static long F(long l, long l1, long l2) {
        return l & l1 | ~l & l2;
    }

    private static long G(long l, long l1, long l2) {
        return l & l2 | l1 & ~l2;
    }

    private static long H(long l, long l1, long l2) {
        return l ^ l1 ^ l2;
    }

    private static long I(long l, long l1, long l2) {
        return l1 ^ (l | ~l2);
    }

    private static long FF(long l, long l1, long l2, long l3, long l4, long l5, long l6) {
        l += F(l1, l2, l3) + l4 + l6;
        l = (int) l << (int) l5 | (int) l >>> (int) (32L - l5);
        l += l1;
        return l;
    }

    private static long GG(long l, long l1, long l2, long l3, long l4, long l5, long l6) {
        l += G(l1, l2, l3) + l4 + l6;
        l = (int) l << (int) l5 | (int) l >>> (int) (32L - l5);
        l += l1;
        return l;
    }

    private static long HH(long l, long l1, long l2, long l3, long l4, long l5, long l6) {
        l += H(l1, l2, l3) + l4 + l6;
        l = (int) l << (int) l5 | (int) l >>> (int) (32L - l5);
        l += l1;
        return l;
    }

    private static long II(long l, long l1, long l2, long l3, long l4, long l5, long l6) {
        l += I(l1, l2, l3) + l4 + l6;
        l = (int) l << (int) l5 | (int) l >>> (int) (32L - l5);
        l += l1;
        return l;
    }

    /**
     * 补位操作，abyte0为需要进行MD5加密的字符串，i为字符串长度；
     *
     * @param abyte0
     *            byte[]
     * @param i
     *            int
     */
    private static void md5Update(byte abyte0[], int i) {
        byte abyte1[] = new byte[64];
        int k = (int) (count[0] >>> 3) & 0x3f;
        if ((count[0] += i << 3) < (long) (i << 3)) {
            count[1]++;
        }

        count[1] += i >>> 29;
        int l = 64 - k;
        int j;
        if (i >= l) {
            md5Memcpy(buffer, abyte0, k, 0, l);
            md5Transform(buffer);
            for (j = l; j + 63 < i; j += 64) {
                md5Memcpy(abyte1, abyte0, 0, j, 64);
                md5Transform(abyte1);
            }
            k = 0;
        } else {
            j = 0;
        }
        md5Memcpy(buffer, abyte0, k, j, i - j);
    }

    private static void md5Final() {
        // 最终处理，将得到的128位（16字节）MD5码存放在digest数组中
        byte abyte0[] = new byte[8];
        Encode(abyte0, count, 8);
        int i = (int) (count[0] >>> 3) & 0x3f;
        int j = (i >= 56) ? 120 - i : 56 - i;
        md5Update(PADDING, j);
        md5Update(abyte0, 8);
        Encode(digest, state, 16);
    }

    private static void md5Memcpy(byte abyte0[], byte abyte1[], int i, int j, int k) {
        for (int l = 0; l < k; l++) {
            abyte0[i + l] = abyte1[j + l];
        }
    }

    private static void md5Transform(byte abyte0[]) {
        long l = state[0];
        long l1 = state[1];
        long l2 = state[2];
        long l3 = state[3];
        long al[] = new long[16];
        Decode(al, abyte0, 64);
        l = FF(l, l1, l2, l3, al[0], 7L, 0xd76aa478L);
        l3 = FF(l3, l, l1, l2, al[1], 12L, 0xe8c7b756L);
        l2 = FF(l2, l3, l, l1, al[2], 17L, 0x242070dbL);
        l1 = FF(l1, l2, l3, l, al[3], 22L, 0xc1bdceeeL);
        l = FF(l, l1, l2, l3, al[4], 7L, 0xf57c0fafL);
        l3 = FF(l3, l, l1, l2, al[5], 12L, 0x4787c62aL);
        l2 = FF(l2, l3, l, l1, al[6], 17L, 0xa8304613L);
        l1 = FF(l1, l2, l3, l, al[7], 22L, 0xfd469501L);
        l = FF(l, l1, l2, l3, al[8], 7L, 0x698098d8L);
        l3 = FF(l3, l, l1, l2, al[9], 12L, 0x8b44f7afL);
        l2 = FF(l2, l3, l, l1, al[10], 17L, 0xffff5bb1L);
        l1 = FF(l1, l2, l3, l, al[11], 22L, 0x895cd7beL);
        l = FF(l, l1, l2, l3, al[12], 7L, 0x6b901122L);
        l3 = FF(l3, l, l1, l2, al[13], 12L, 0xfd987193L);
        l2 = FF(l2, l3, l, l1, al[14], 17L, 0xa679438eL);
        l1 = FF(l1, l2, l3, l, al[15], 22L, 0x49b40821L);
        l = GG(l, l1, l2, l3, al[1], 5L, 0xf61e2562L);
        l3 = GG(l3, l, l1, l2, al[6], 9L, 0xc040b340L);
        l2 = GG(l2, l3, l, l1, al[11], 14L, 0x265e5a51L);
        l1 = GG(l1, l2, l3, l, al[0], 20L, 0xe9b6c7aaL);
        l = GG(l, l1, l2, l3, al[5], 5L, 0xd62f105dL);
        l3 = GG(l3, l, l1, l2, al[10], 9L, 0x2441453L);
        l2 = GG(l2, l3, l, l1, al[15], 14L, 0xd8a1e681L);
        l1 = GG(l1, l2, l3, l, al[4], 20L, 0xe7d3fbc8L);
        l = GG(l, l1, l2, l3, al[9], 5L, 0x21e1cde6L);
        l3 = GG(l3, l, l1, l2, al[14], 9L, 0xc33707d6L);
        l2 = GG(l2, l3, l, l1, al[3], 14L, 0xf4d50d87L);
        l1 = GG(l1, l2, l3, l, al[8], 20L, 0x455a14edL);
        l = GG(l, l1, l2, l3, al[13], 5L, 0xa9e3e905L);
        l3 = GG(l3, l, l1, l2, al[2], 9L, 0xfcefa3f8L);
        l2 = GG(l2, l3, l, l1, al[7], 14L, 0x676f02d9L);
        l1 = GG(l1, l2, l3, l, al[12], 20L, 0x8d2a4c8aL);
        l = HH(l, l1, l2, l3, al[5], 4L, 0xfffa3942L);
        l3 = HH(l3, l, l1, l2, al[8], 11L, 0x8771f681L);
        l2 = HH(l2, l3, l, l1, al[11], 16L, 0x6d9d6122L);
        l1 = HH(l1, l2, l3, l, al[14], 23L, 0xfde5380cL);
        l = HH(l, l1, l2, l3, al[1], 4L, 0xa4beea44L);
        l3 = HH(l3, l, l1, l2, al[4], 11L, 0x4bdecfa9L);
        l2 = HH(l2, l3, l, l1, al[7], 16L, 0xf6bb4b60L);
        l1 = HH(l1, l2, l3, l, al[10], 23L, 0xbebfbc70L);
        l = HH(l, l1, l2, l3, al[13], 4L, 0x289b7ec6L);
        l3 = HH(l3, l, l1, l2, al[0], 11L, 0xeaa127faL);
        l2 = HH(l2, l3, l, l1, al[3], 16L, 0xd4ef3085L);
        l1 = HH(l1, l2, l3, l, al[6], 23L, 0x4881d05L);
        l = HH(l, l1, l2, l3, al[9], 4L, 0xd9d4d039L);
        l3 = HH(l3, l, l1, l2, al[12], 11L, 0xe6db99e5L);
        l2 = HH(l2, l3, l, l1, al[15], 16L, 0x1fa27cf8L);
        l1 = HH(l1, l2, l3, l, al[2], 23L, 0xc4ac5665L);
        l = II(l, l1, l2, l3, al[0], 6L, 0xf4292244L);
        l3 = II(l3, l, l1, l2, al[7], 10L, 0x432aff97L);
        l2 = II(l2, l3, l, l1, al[14], 15L, 0xab9423a7L);
        l1 = II(l1, l2, l3, l, al[5], 21L, 0xfc93a039L);
        l = II(l, l1, l2, l3, al[12], 6L, 0x655b59c3L);
        l3 = II(l3, l, l1, l2, al[3], 10L, 0x8f0ccc92L);
        l2 = II(l2, l3, l, l1, al[10], 15L, 0xffeff47dL);
        l1 = II(l1, l2, l3, l, al[1], 21L, 0x85845dd1L);
        l = II(l, l1, l2, l3, al[8], 6L, 0x6fa87e4fL);
        l3 = II(l3, l, l1, l2, al[15], 10L, 0xfe2ce6e0L);
        l2 = II(l2, l3, l, l1, al[6], 15L, 0xa3014314L);
        l1 = II(l1, l2, l3, l, al[13], 21L, 0x4e0811a1L);
        l = II(l, l1, l2, l3, al[4], 6L, 0xf7537e82L);
        l3 = II(l3, l, l1, l2, al[11], 10L, 0xbd3af235L);
        l2 = II(l2, l3, l, l1, al[2], 15L, 0x2ad7d2bbL);
        l1 = II(l1, l2, l3, l, al[9], 21L, 0xeb86d391L);
        state[0] += l;
        state[1] += l1;
        state[2] += l2;
        state[3] += l3;
    }

    private static void Encode(byte abyte0[], long al[], int i) { // 转换函数，将al中long型的变量输出到byte型的数组abyte0中，
        // 低位字节在前，高位字节在后；
        int j = 0;
        for (int k = 0; k < i; k += 4) {
            abyte0[k] = (byte) (int) (al[j] & 255L);
            abyte0[k + 1] = (byte) (int) (al[j] >>> 8 & 255L);
            abyte0[k + 2] = (byte) (int) (al[j] >>> 16 & 255L);
            abyte0[k + 3] = (byte) (int) (al[j] >>> 24 & 255L);
            j++;
        }
    }

    private static void Decode(long al[], byte abyte0[], int i) {
        int j = 0;
        for (int k = 0; k < i; k += 4) {
            al[j] = b2iu(abyte0[k]) | b2iu(abyte0[k + 1]) << 8 | b2iu(abyte0[k + 2]) << 16 | b2iu(abyte0[k + 3]) << 24;
            j++;
        }
    }

    private static long b2iu(byte byte0) {
        return (byte0 >= 0) ? byte0 : byte0 & 0xff;
    }

    // 标准的MD5算法
    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f" };

    /**
     * 转换字节数组为16进制字串
     *
     * @param b
     *            字节数组
     * @return 16进制字串
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte aB : b) {
            resultSb.append(byteToHexString(aB));
        }
        return resultSb.toString();
    }

    /**
     * 转换byte到16进制
     *
     * @param b
     *            要转换的byte
     * @return 16进制格式
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * MD5编码
     *
     * @param origin
     *            原始字符串
     * @return 经过MD5加密之后的结果
     */
    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            resultString = origin;
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(resultString.getBytes("UTF-8"));
            resultString = byteArrayToHexString(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultString;
    }

    public static void main(String args[]) {
        String aString="nickName=测&time=1499072408870&userId=179&key=NDIxY2M3MzRhZTViNDQyZGEzNjM1ZjZiYzQ3YjI5M2M=";

        String aaaString=MD5(aString);
        System.out.println(aaaString);


    }
}

