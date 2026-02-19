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
     */
    external fun DecodeAniFrame(aniPtr: Long, outBuf: ByteArray)

    /**
     * Destroys the animation info structure and releases its resources.
     *
     * @param aniPtr A pointer to the animation info structure to be destroyed.
     */
    external fun DestroyAniInfo(aniPtr: Long)
}
