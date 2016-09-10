package android.caseystalnaker.com.popinvideodemo.util;

/**
 * Created by Casey on 9/9/16.
 */
public class Util {
    private static Util mInstance = new Util();
    private int mVideoLimit = 30000;

    public static Util getInstance() {
        return mInstance;
    }

    private Util() {}

    public int getVideoLimit() {
        return mVideoLimit;
    }
}
