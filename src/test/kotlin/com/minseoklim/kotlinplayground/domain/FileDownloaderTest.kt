package com.minseoklim.kotlinplayground.domain

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileOutputStream
import java.net.URL

private const val TARGET_FILE_NAME = "google.png"

internal class FileDownloaderTest {
    private val wrongUrl = URL("https://wrong-url.com")
    private val properUrl = URL("https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")

    @BeforeEach
    internal fun setUp() {
        val targetFile = File(TARGET_FILE_NAME)
        if (targetFile.isFile) {
            targetFile.delete()
        }
    }

    @Test
    @DisplayName("정상적인 recreateFunction이 주어졌을 경우 파일 다운로드에 성공")
    internal fun execute() {
        // given
        val targetFile = File(TARGET_FILE_NAME)
        val outputStream = FileOutputStream(targetFile)

        // when
        val fileDownloader = FileDownloader(wrongUrl) { properUrl }
        fileDownloader.execute(outputStream, 2)

        // then
        assert(targetFile.isFile)
    }

    @Test
    @DisplayName("파일 재생성이 계속해서 실패하면 결국 예외 발생")
    internal fun executeByWrongFunction() {
        // given
        val targetFile = File(TARGET_FILE_NAME)
        val outputStream = FileOutputStream(targetFile)

        // when, then
        assertThrows<RuntimeException> {
            val fileDownloader = FileDownloader(wrongUrl) { wrongUrl }
            fileDownloader.execute(outputStream, 10)
        }
    }
}
