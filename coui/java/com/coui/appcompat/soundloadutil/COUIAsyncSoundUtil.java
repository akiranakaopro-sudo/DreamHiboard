package com.coui.appcompat.soundloadutil;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.provider.Settings;
import android.util.SparseIntArray;

public final class COUIAsyncSoundUtil {
    public static final int FLAG_BYPASS_MUTE = 128;
    private static COUIAsyncSoundUtil sInstance;

    private final Context mContext;
    private final SparseIntArray mSoundMap = new SparseIntArray();
    private SoundPool mSoundPool;

    private COUIAsyncSoundUtil(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized void register(Context context, int... soundResIds) {
        if (sInstance == null) {
            sInstance = new COUIAsyncSoundUtil(context);
        }
        sInstance.ensureSoundPool();
        for (int soundResId : soundResIds) {
            if (sInstance.mSoundMap.indexOfKey(soundResId) < 0) {
                sInstance.mSoundMap.put(soundResId, sInstance.mSoundPool.load(sInstance.mContext, soundResId, 0));
            }
        }
    }

    public static void play(Context context, int soundResId, float leftVolume, float rightVolume,
            int priority, int loop, float rate) {
        COUIAsyncSoundUtil instance = sInstance;
        if (instance == null || instance.mSoundPool == null || !querySoundEffectsEnabled(context)) {
            return;
        }
        int soundId = instance.mSoundMap.get(soundResId);
        if (soundId != 0) {
            instance.mSoundPool.play(soundId, leftVolume, rightVolume, priority, loop, rate);
        }
    }

    private void ensureSoundPool() {
        if (mSoundPool != null) {
            return;
        }
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setFlags(FLAG_BYPASS_MUTE)
                .setLegacyStreamType(android.media.AudioManager.STREAM_SYSTEM)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attributes)
                .build();
    }

    private static boolean querySoundEffectsEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "sound_effects_enabled", 0) != 0;
    }
}
