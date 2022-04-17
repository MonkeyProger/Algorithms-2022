package lesson4

import java.lang.IllegalStateException
import java.util.*

/**
 * Префиксное дерево для строк
 */
class KtTrie : AbstractMutableSet<String>(), MutableSet<String> {

    private class Node {
        val children: SortedMap<Char, Node> = sortedMapOf()
    }

    private val root = Node()

    override var size: Int = 0
        private set

    override fun clear() {
        root.children.clear()
        size = 0
    }

    private fun String.withZero() = this + 0.toChar()

    private fun findNode(element: String): Node? {
        var current = root
        for (char in element) {
            current = current.children[char] ?: return null
        }
        return current
    }

    override fun contains(element: String): Boolean =
        findNode(element.withZero()) != null

    override fun add(element: String): Boolean {
        var current = root
        var modified = false
        for (char in element.withZero()) {
            val child = current.children[char]
            if (child != null) {
                current = child
            } else {
                modified = true
                val newChild = Node()
                current.children[char] = newChild
                current = newChild
            }
        }
        if (modified) {
            size++
        }
        return modified
    }

    override fun remove(element: String): Boolean {
        val current = findNode(element) ?: return false
        if (current.children.remove(0.toChar()) != null) {
            size--
            return true
        }
        return false
    }

    /**
     * Итератор для префиксного дерева
     *
     * Спецификация: [java.util.Iterator] (Ctrl+Click по Iterator)
     *
     * Сложная
     */
    override fun iterator(): MutableIterator<String> = TrieIterator()

    inner class TrieIterator internal constructor() : MutableIterator<String> {
        private var index = -1
        private var removeFlag = false
        private var previousPos = root
        private var curPos = root
        private var forkDeque = ArrayDeque<Pair<Node, Int>>()
        private var prefixDeque = ArrayDeque<String>()
        private var word = ""

        override fun hasNext(): Boolean {
            if (index + 1 >= size)
                return false
            return true
        }

        // * n - количество элементов типа Node
        // * T(n) = O(n) трудоемкость не больше O(longestWord.length)
        // * При полном прохождении дерева операция занимает O(n)
        // * Функция nextChildInNode выбирает следующую букву в узле, что
        // увеличивает количество итераций до 2n в худшем случае
        // ==========================================================
        // * По пути к следующему слову узлы и полученные префиксы сохраняются
        // соответсвенно в forkDeque(с указанием кода прошедшей буквы) и prefixDeque
        // * Максимальное значение стека равно n/2 в худшем случае
        // Худший случай ветка: root - a - b - c - d
        // R(n) = O(n)            |    |   |   |
        //                       (z)  (z) (z) (z)
        override fun next(): String {
            index++
            removeFlag = true
            if (index >= size) throw NoSuchElementException()
            previousPos = curPos
            if (index == 0) curPos = wordInBranchWithSave(curPos) else nextPos()
            return word
        }

        private fun nextPos() {
            word = prefixDeque.first
            val prev: Node
            if (forkDeque.isEmpty()) return
            if (curPos == forkDeque.first.first.children.values.first()) {
                prev = forkDeque.first.first
                val pair = nextChildInNode(prev, forkDeque.first.second)
                forkDeque.pop()
                forkDeque.push(prev to pair.second)
                word += pair.second.toChar()
                curPos = wordInBranchWithSave(pair.first)
            } else {
                val lastKey = forkDeque.first.first.children.lastKey().code
                if (forkDeque.first.second != lastKey) {
                    prev = forkDeque.first.first
                    val pair = nextChildInNode(prev, forkDeque.first.second)
                    forkDeque.pop()
                    forkDeque.push(prev to pair.second)
                    word += pair.second.toChar()
                    curPos = wordInBranchWithSave(pair.first)
                } else {
                    forkDeque.pop()
                    prefixDeque.pop()
                    nextPos()
                }
            }
        }

        private fun wordInBranchWithSave(node: Node): Node {
            var tempWord = ""
            var current = node
            var prev: Node
            while (!(current.children.size == 1 && current.children.firstKey().code == 0)) {
                prev = current
                if (prev.children.size > 1) prefixDeque.push(word + tempWord)
                if (current.children.firstKey().code != 0) tempWord += current.children.firstKey()
                val pair = nextChildInNode(current, -1)
                current = pair.first
                if (prev.children.size > 1) forkDeque.push(prev to pair.second)
                if (current.children.isEmpty()) {
                    current = prev
                    break
                }
            }
            word += tempWord
            return current
        }

        private fun nextChildInNode(el: Node, code: Int): Pair<Node, Int> {
            var res = el
            var new = code
            for (i in el.children) {
                if (i.key.code > code) {
                    res = i.value
                    new = i.key.code
                    break
                }
            }
            return res to new
        }

        private fun prevChildInNode(el: Node, code: Int): Int {
            var new = code
            var prev = code
            for (i in el.children) {
                new = i.key.code
                if (new == code) {
                    if (i.key != el.children.firstKey()) new = prev
                    break
                }
                prev = new
            }
            return new
        }

        // * T(n) = O(n), поскольку функция prevChildInNode аналогично nextChildInNode
        // в худшем случае проходит n элементов.
        //              root
        //     /      /   |   \      \
        //  1el     2el  ...  N-1el  Nel
        override fun remove() {
            if (!removeFlag) throw IllegalStateException()
            if (curPos == root) return
            curPos = if (forkDeque.isEmpty()) root
            else {
                val topNode = forkDeque.first.first
                val keyToRemove = forkDeque.first.second
                val keyToSwap = prevChildInNode(topNode, keyToRemove)
                topNode.children.remove(keyToRemove.toChar())
                forkDeque.pop()
                if (topNode.children.size > 1) forkDeque.push(topNode to keyToSwap)
                previousPos
            }
            index--
            size--
            removeFlag = false
        }
    }

}