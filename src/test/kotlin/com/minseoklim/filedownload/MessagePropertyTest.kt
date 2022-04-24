package com.minseoklim.filedownload

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.core.io.ClassPathResource
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

@SpringBootTest
internal class MessagePropertyTest {
    @Autowired
    private lateinit var messageSource: ResourceBundleMessageSource

    @Test
    internal fun 메세지_프로퍼티_비교() {
        val basename = messageSource.basenameSet.single()
        val messageDirectory = ClassPathResource(basename.substringBefore(File.separator)).file

        // 기준이 되는 디폴트 메세지 프로퍼티 파일
        val baseMessageFile = ClassPathResource("$basename.properties").file
        // 디폴트 메세지 프로퍼티를 제외한 나머지 파일들
        val otherMessageFiles = messageDirectory.listFiles().filter { it != baseMessageFile }

        // 기준이 되는 디폴트 메세지 프로퍼티
        val baseMessageProperties = extractSortedProperties(baseMessageFile)

        val badMessagePropertyMap = getBadMessagePropertyMap(otherMessageFiles, baseMessageProperties)

        badMessagePropertyMap.forEach {
            println("${baseMessageFile.name}에만 있고 ${it.key} 에는 없는 프로퍼티 : ${it.value.first}")
            println("${it.key}에만 있고 ${baseMessageFile.name} 에는 없는 프로퍼티 : ${it.value.second}")
        }

        assert(badMessagePropertyMap.values.all { it.first.isEmpty() && it.second.isEmpty() })
    }

    // 투포인터 알고리즘을 통해 디폴트 메세지에만 있거나, 언어별 메세지에만 있는 프로퍼티를 찾는다.
    private fun getBadMessagePropertyMap(
        otherMessageFiles: List<File>,
        baseMessageProperties: List<String>
    ): Map<String, Pair<Set<String>, Set<String>>> {
        val badMessagePropertyMap =
            otherMessageFiles.associate { it.name to (mutableSetOf<String>() to mutableSetOf<String>()) }

        otherMessageFiles.forEach {
            val messageProperties = extractSortedProperties(it)

            var baseIdx = 0 // 기준 메세지 프로퍼티의 인덱스
            var idx = 0 // 비교 대상이 될 메세지 프로퍼티의 인덱스

            while (baseIdx < baseMessageProperties.size && idx < messageProperties.size) {
                val baseProperty = baseMessageProperties[baseIdx]
                val property = messageProperties[idx]

                if (baseProperty == property) {
                    baseIdx++
                    idx++
                } else {
                    if (baseProperty < property) {
                        badMessagePropertyMap[it.name]?.first?.add(baseProperty)
                        baseIdx++
                    } else {
                        badMessagePropertyMap[it.name]?.second?.add(property)
                        idx++
                    }
                }
            }
            if (baseIdx < baseMessageProperties.size) {
                for (i in baseIdx until baseMessageProperties.size) {
                    badMessagePropertyMap[it.name]?.first?.add(baseMessageProperties[i])
                }
            }
            if (idx < messageProperties.size) {
                for (i in idx until messageProperties.size) {
                    badMessagePropertyMap[it.name]?.second?.add(messageProperties[i])
                }
            }
        }
        return badMessagePropertyMap
    }

    private fun extractSortedProperties(file: File): List<String> {
        val result = mutableListOf<String>()

        BufferedReader(FileReader(file)).use {
            while (true) {
                val line = it.readLine() ?: break
                val (propertyName, value) = line.split('=')
                result.add(propertyName)
            }
        }

        return result.sorted()
    }
}
