package com.minseoklim.filedownload.domain

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.OutputStream
import java.net.URL

class FileDownloader(
    private var url: URL,
    private val recreateFunction: (() -> URL)
) {
    fun execute(outputStream: OutputStream, retryLimit: Int, nThRequest: Int = 1) {
        try {
            url.openStream().use { IOUtils.copy(it, outputStream) }
        } catch (e: IOException) {
            if (nThRequest <= retryLimit) {
                LoggerFactory.getLogger(this.javaClass).info("다운로드에 실패하여 파일 재생성을 시도합니다 - ${nThRequest}번째 시도")
                url = recreateFunction()
                execute(outputStream, retryLimit, nThRequest + 1)
            } else {
                LoggerFactory.getLogger(this.javaClass).error("파일 다운로드 및 재생성 실패")
                throw RuntimeException(e)
            }
        }
    }
}
