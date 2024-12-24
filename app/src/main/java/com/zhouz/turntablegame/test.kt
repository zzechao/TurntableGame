package com.zhouz.turntablegame

import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

/**
 * @Author: leavesCZY
 * @Date: 2021/08/24 10:46
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */

private const val XML_FILE_NAME = """dimens.xml"""
private const val XML_HEADER = """<?xml version="1.0" encoding="utf-8"?>"""
private const val XML_RESOURCE_START_TAG = """<resources>"""
private const val XML_RESOURCE_END_TAG = """</resources>"""

//private const val XML_DIMEN_WIDTH_TAG = """<dimen name="DIMEN_WIDTH">%ddp</dimen>"""
private const val XML_DIMEN_TEMPLATE_TO_DP = """<dimen name="dp%s">%.2fdp</dimen>"""
private const val XML_DIMEN_TEMPLATE_TO_PX = """<dimen name="DIMEN_%dpx">%.2fdp</dimen>"""

private const val DESIGN_WIDTH_DP = 375

private const val MAX_WIDTH_DP = 720

private const val DESIGN_WIDTH_PX = 1080

fun main() {
    val srcDirDp = File("./src/main/res")
    System.out.println(srcDirDp.absolutePath)
    srcDirDp.mkdirs()
    makeDimens(designWidth = DESIGN_WIDTH_DP, srcDir = srcDirDp, xmlTemplate = XML_DIMEN_TEMPLATE_TO_DP)
//    makeDimens(designWidth = DESIGN_WIDTH_PX, srcDir = srcDirDp, xmlTemplate = XML_DIMEN_TEMPLATE_TO_PX)
}

private fun makeDimens(designWidth: Int, srcDir: File, xmlTemplate: String) {
    val smallestWidthList = buildList {
        for (width in 320..420 step 10) {
            add(element = width)
        }
    }.toMutableList()
    smallestWidthList.add(375)
    for (width in smallestWidthList) {
        makeDimensFile(
            designWidth = designWidth,
            width = width,
            xmlTemplate = xmlTemplate,
            srcDir = srcDir
        )
    }
}

private fun makeDimensFile(designWidth: Int, width: Int, xmlTemplate: String, srcDir: File) {
    val folderName = "values-sw" + width + "dp"
    val file = File(srcDir, folderName)
    file.mkdirs()
    val fos = FileOutputStream(file.absolutePath + File.separator + XML_FILE_NAME)
    fos.write(generateDimens(designWidth = designWidth, width = width, xmlTemplate = xmlTemplate).toByteArray())
    fos.flush()
    fos.close()
}

private fun generateDimens(designWidth: Int, width: Int, xmlTemplate: String): String {
    val sb = StringBuilder()
    sb.append(XML_HEADER)
    sb.append("\n")
    sb.append(XML_RESOURCE_START_TAG)
    sb.append("\n")
    for (i in -100..MAX_WIDTH_DP) {
        val dpValue = i.toFloat() * width / designWidth
        sb.append("    ")
        if (i < 0) {
            sb.append(String.format(xmlTemplate, "${abs(i)}m", dpValue))
        } else {
            sb.append(String.format(xmlTemplate, i.toString(), dpValue))
        }
        sb.append("\n")

        val dp5value = (i.toFloat() + 0.5f) * width / designWidth
        sb.append("    ")
        if (i < 0) {
            sb.append(String.format(xmlTemplate, "${abs(i)}_5m", dp5value))
        } else {
            sb.append(String.format(xmlTemplate, "${i}_5", dp5value))
        }
        sb.append("\n")
    }
    sb.append(XML_RESOURCE_END_TAG)
    return sb.toString()
}