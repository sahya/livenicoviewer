package jp.android.sahya.NicoLiveViewer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class NicoCrypt {
	/*
	 * http://java.sun.com/j2se/1.4/ja/docs/ja/guide/security/jce/JCERefGuide.html#AppA
 	Cipher のインスタンスを要求する場合、次の名前を変換内の algorithm コンポーネントとして指定できます。
	AES - Advanced Encryption Standard として、NIST によって FIPS ドラフトに指定されました。 Joan Daemen、Vincent Rijmen 両氏による Rijndael アルゴリズムに基づいた 128 ビットのブロック暗号であり、128 ビット、192 ビット、256 ビットの鍵をサポートします。
	Blowfish - Bruce Schneier 氏の設計によるブロック暗号です。
	DES - データ暗号化規格です (FIPS PUB 46-2 で定義)。
	DESede - トリプル DES 暗号化です (DES-EDE)。
	PBEWith<digest>And<encryption> または PBEWith<prf>And<encryption> - パスワードベースの暗号化アルゴリズム (PKCS #5) です。指定されたメッセージダイジェスト (<digest>) または擬似ランダム関数 (<prf>) と暗号化アルゴリズム (<encryption>) を使用します。次に例を示します。
	PBEWithMD5AndDES - 1993 年 11 月、RSA Laboratories の「PKCS #5: Password-Based Encryption Standard」バージョン 1.5 に定義されたパスワードベースの暗号化アルゴリズムです。 このアルゴリズムでは、CBC は暗号モード、PKCS5Padding はパディング方式とされています。他の暗号モードやパディング方式で使用することはできません。
	PBEWithHmacSHA1AndDESede - 1999 年 3 月、RSA Laboratories の「PKCS #5: Password-Based Encryption Standard」バージョン 2.0 に定義されたパスワードベースの暗号化アルゴリズムです。
	RC2、RC4、および RC5 - RSA Data Security, Inc の Ron Rivest により開発された可変キーサイズ暗号化アルゴリズムです。
	RSA - PKCS #1 に定義されている RSA 暗号化アルゴリズムです。
	 */
	private static String algorithm = "AES";

	public static byte[] encrypt(String key, String text)
	{
		try {
			SecretKeySpec sksSpec =  new SecretKeySpec(key.getBytes(), algorithm);
			Cipher cipher =   Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, sksSpec);
			return cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			return "".getBytes();
		}
	}

	public static String decrypt(String key, byte[] encrypted)
	{
		try {
			SecretKeySpec sksSpec =  new SecretKeySpec(key.getBytes(), algorithm);
			Cipher cipher =  Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, sksSpec);
			return new String(cipher.doFinal(encrypted));
		} catch (Exception e) {
			return null;
		}
	}
}
