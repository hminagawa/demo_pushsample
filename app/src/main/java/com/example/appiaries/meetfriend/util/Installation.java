package com.example.appiaries.meetfriend.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.content.Context;

/**
 * アプリ内で固有IDを生成します。
 * 
 * @author yoshihide-sogawa
 * @see <a href="http://android-developers.blogspot.jp/2011/03/identifying-app-installations.html">Link</a>
 */
public final class Installation {

    /** 固有のID */
    private static String sID = null;
    /** ファイル名 */
    private static final String FILE_NAME = "meetfriend_uuid";

    /** コンストラクタを隠蔽 */
    private Installation() {
    }

    /**
     * UUIDを取得します。
     * 
     * @param context
     *            {@link Context}
     * @return UUID
     */
    public synchronized static String id(final Context context) {
        if (sID == null) {
            final File installation = new File(context.getFilesDir(), FILE_NAME);
            try {
                if (!installation.exists()) {
                    writeInstallationFile(installation);
                }
                sID = readInstallationFile(installation);
            } catch (IOException e) {
                // 処理なし
            }
        }
        return sID;
    }

    /**
     * アピアリーズデモ用のIDを生成します。<br/>
     * ハイフン不可、30文字制限のため。
     * 
     * @param context
     *            {@link Context}
     * @return UUID
     */
    public synchronized static String id4Ap(final Context context) {
        return id(context).replaceAll("-", "").substring(0, 30);
    }

    /**
     * ファイルからUUIDを読み込みます。
     * 
     * @param installation
     *            {@link File}
     * @return UUID
     * @throws IOException
     */
    private static String readInstallationFile(final File installation) throws IOException {
        final RandomAccessFile f = new RandomAccessFile(installation, "r");
        final byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes, "UTF-8");
    }

    /**
     * UUIDを書き込みます。
     * 
     * @param installation
     *            {@link File}
     * @throws IOException
     */
    private static void writeInstallationFile(final File installation) throws IOException {
        final FileOutputStream out = new FileOutputStream(installation);
        final String id = UUID.randomUUID().toString();
        out.write(id.getBytes("UTF-8"));
        out.close();
    }
}
