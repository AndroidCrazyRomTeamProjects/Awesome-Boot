package org.crazyromteam.qmgstore.qmg


object LibQmg {
    init {
        System.loadLibrary("qmgplayer")
    }

    /**
     * Creates an animation info structure from the given QMG data.
     *
     * @param qmgData The raw byte data of the QMG file.
     * @param flag A flag for initialization.
     * @return A pointer to the created animation info structure.
     */
    external fun CreateAniInfo(qmgData: ByteArray?, flag: Int): Long

    /**
     * Decodes a single frame of the animation.
     *
     * @param aniPtr A pointer to the animation info structure.
     * @param outBuf A pre-allocated buffer to store the decoded frame data.
     * @return The status or number of remaining frames.
     */
    external fun DecodeAniFrame(aniPtr: Long, outBuf: ByteArray): Int

    /**
     * Decodes a single frame of the animation directly into a native buffer and performs color format
     * conversion entirely in C++ before copying back to outBuf.
     *
     * @param aniPtr A pointer to the animation info structure.
     * @param outBuf A pre-allocated buffer to store the decoded frame data.
     * @param width The width of the frame.
     * @param height The height of the frame.
     * @param colorFormat The color format index (Color.bppType).
     * @return The status or number of remaining frames.
     */
    external fun DecodeAniFrameNative(aniPtr: Long, outBuf: ByteArray, width: Int, height: Int, colorFormat: Int): Int

    /**
     * Destroys the animation info structure and releases its resources.
     *
     * @param aniPtr A pointer to the animation info structure to be destroyed.
     */
    external fun DestroyAniInfo(aniPtr: Long)
}
