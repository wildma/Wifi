package com.wildma.wifi

class WifiScanResult {
    // 是否是 2.4G
    private var is24G = false

    // 是否是 5G
    private var is5G = false

    /**
     * 设置频率
     *
     * @param frequency 频率
     */
    fun setFrequency(frequency: Int) {
        if (frequency in 2401..2499) {
            is24G = true
        }
        if (frequency in 4901..5899) {
            is5G = true
        }
    }

    /**
     * 是否是 2.4G
     */
    fun is24G(): Boolean {
        return is24G && !is5G
    }

    /**
     * 是否是 5G
     */
    fun is5G(): Boolean {
        return !is24G && is5G
    }

    /**
     * 是否是 2.4G+2.5G 双频
     */
    fun is245G(): Boolean {
        return is24G && is5G
    }
}