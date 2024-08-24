package com.zhouz.turntablegame

import org.junit.Test
import java.util.Scanner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        println("xxxxxxxxxxxxx")
        val node = Node(1)
        node.build { build { build { build { build {} } } } }
        println("node:$node")

        val nodeTree = Node(0)
        var whileNode: Node? = node
        while (whileNode != null) {
            val curNode = nodeTree.next
            val nextNode = whileNode.next
            whileNode.next = curNode
            nodeTree.next = whileNode
            whileNode = nextNode
        }

        println("node:${nodeTree.next}")
    }

    inner class Node(private val value: Int = -1) {
        var next: Node? = null

        fun build(builder: Node.() -> Unit) {
            val node = Node(value + 1)
            next = node
            builder.invoke(node)
        }

        override fun toString(): String {
            return "$value-$next"
        }
    }

    @Test
    fun addition_isCorrect2() {

    }

    inner class TreeNode
}

fun main() {
    val scanner = Scanner(System.`in`)
    while (scanner.hasNextInt()) {
        val n = scanner.nextInt()
        for (i in 0 until n) {
            val x = scanner.nextInt()
            val y = scanner.nextInt()
        }
    }
}