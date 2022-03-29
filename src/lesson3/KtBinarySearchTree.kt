package lesson3

import java.lang.IllegalStateException
import java.util.*
import kotlin.NoSuchElementException
import kotlin.math.max

// attention: Comparable is supported but Comparator is not
class KtBinarySearchTree<T : Comparable<T>> : AbstractMutableSet<T>(), CheckableSortedSet<T> {

    private class Node<T>(
        val value: T
    ) {
        var parent: Node<T>? = null
        var left: Node<T>? = null
        var right: Node<T>? = null
    }

    private var root: Node<T>? = null

    override var size = 0
        private set

    private fun find(value: T): Node<T>? =
        root?.let { find(it, value) }

    private fun find(start: Node<T>, value: T): Node<T> {
        val comparison = value.compareTo(start.value)
        return when {
            comparison == 0 -> start
            comparison < 0 -> {
                start.left?.parent = start
                start.left?.let { find(it, value) } ?: start
            }
            else -> {
                start.right?.parent = start
                start.right?.let { find(it, value) } ?: start
            }
        }
    }

    override operator fun contains(element: T): Boolean {
        val closest = find(element)
        return closest != null && element.compareTo(closest.value) == 0
    }

    /**
     * Добавление элемента в дерево
     *
     * Если элемента нет в множестве, функция добавляет его в дерево и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     *
     * Спецификация: [java.util.Set.add] (Ctrl+Click по add)
     *
     * Пример
     */
    override fun add(element: T): Boolean {
        val closest = find(element)
        val comparison = if (closest == null) -1 else element.compareTo(closest.value)
        if (comparison == 0) {
            return false
        }
        val newNode = Node(element)
        newNode.parent = closest
        when {
            closest == null -> root = newNode
            comparison < 0 -> {
                assert(closest.left == null)
                closest.left = newNode
            }
            else -> {
                assert(closest.right == null)
                closest.right = newNode
            }
        }
        size++
        return true
    }

    /**
     * Удаление элемента из дерева
     *
     * Если элемент есть в множестве, функция удаляет его из дерева и возвращает true.
     * В ином случае функция оставляет множество нетронутым и возвращает false.
     * Высота дерева не должна увеличиться в результате удаления.
     *
     * Спецификация: [java.util.Set.remove] (Ctrl+Click по remove)
     * (в Котлине тип параметера изменён с Object на тип хранимых в дереве данных)
     *
     * Средняя
     */
    override fun remove(element: T): Boolean {
        TODO()
    }

    override fun comparator(): Comparator<in T>? =
        null

    override fun iterator(): MutableIterator<T> =
        BinarySearchTreeIterator()

    inner class BinarySearchTreeIterator internal constructor() : MutableIterator<T> {
        private var index = -1
        private var removeCounter = false
        private var curPos = root
        private var parentDeque = ArrayDeque<Node<T>>()

        /**
         * Проверка наличия следующего элемента
         *
         * Функция возвращает true, если итерация по множеству ещё не окончена (то есть, если вызов next() вернёт
         * следующий элемент множества, а не бросит исключение); иначе возвращает false.
         *
         * Спецификация: [java.util.Iterator.hasNext] (Ctrl+Click по hasNext)
         *
         * Средняя
         */
        override fun hasNext(): Boolean {
            if (index + 1 >= size)
                return false
            return true
        }

        /**
         * Получение следующего элемента
         *
         * Функция возвращает следующий элемент множества.
         * Так как BinarySearchTree реализует интерфейс SortedSet, последовательные
         * вызовы next() должны возвращать элементы в порядке возрастания.
         *
         * Бросает NoSuchElementException, если все элементы уже были возвращены.
         *
         * Спецификация: [java.util.Iterator.next] (Ctrl+Click по next)
         *
         * Средняя
         */

        override fun next(): T {
            index++
            removeCounter = true
            if (index >= size) throw NoSuchElementException()
            if (curPos != null) {
                return if (index == 0) {
                    curPos = minInBranchWithSave(curPos!!)
                    curPos!!.value
                } else nextPos()
            } else throw NoSuchElementException()
        }
        // * T(n) = O(n) трудоемкость не больше O(height),
        // где height в худшем случае равна n. Худший случай - дерево из всех левых элементов.
        // * При полном прохождении дерева операция занимает O(n)
        // ======================================================
        // * По пути к минимальному элементу промежуточные левые узлы сохраняются в стек parentDeque,
        // для быстрого возврата при достижении максимального значения в конкретной ветке.
        // * Максимальное значение этого стека равно n в худшем случае.
        // Худший случай - дерево из всех левых элементов. Аналогично R(n) = O(n)

        private fun nextPos(): T {
            if (curPos!!.right != null) {
                curPos = minInBranchWithSave(curPos!!.right!!)
            } else if (parentDeque.isNotEmpty()) curPos = parentDeque.pop()
            return curPos!!.value
        }

        private fun minInBranchWithSave(node: Node<T>): Node<T> {
            var current = node
            while (current.left != null) {
                parentDeque.push(current)
                current = current.left!!
            }
            return current
        }

        /**
         * Удаление предыдущего элемента
         *
         * Функция удаляет из множества элемент, возвращённый крайним вызовом функции next().
         *
         * Бросает IllegalStateException, если функция была вызвана до первого вызова next() или же была вызвана
         * более одного раза после любого вызова next().
         *
         * Спецификация: [java.util.Iterator.remove] (Ctrl+Click по remove)
         *
         * Сложная
         */
        override fun remove() {
            curPos?.let { remove(it) }
        }
        // * T(n)=O(n) худший случай: root->root.left->root.left((.right)*(n-2))

        private fun remove(node: Node<T>) {
            if (!removeCounter) throw IllegalStateException()
            val parent = node.parent
            removeCounter = false
            index--
            size--
            when {
                hasNoChildren(node) -> if (node == root) delNode(node, null, null) else
                    delNode(node, null, parent)
                node.left == null -> delNode(node, node.right, parent)
                node.right == null -> delNode(node, node.left, parent)
                else -> {
                    val maxInLeftChild = maxInBranch(node.left!!)

                    if (maxInLeftChild != node.left) {
                        maxInLeftChild.parent?.right = maxInLeftChild.left
                        if (maxInLeftChild.left != null)
                            maxInLeftChild.parent = maxInLeftChild.parent
                        node.left!!.parent = maxInLeftChild
                        maxInLeftChild.left = node.left!!
                    }
                    maxInLeftChild.right = node.right!!
                    node.right!!.parent = maxInLeftChild

                    maxInLeftChild.parent = parent
                    if (parent != null) {
                        if (parent.value < maxInLeftChild.value) parent.right = maxInLeftChild
                        else parent.left = maxInLeftChild
                    } else root = maxInLeftChild

                    node.parent = null
                    node.left = null
                    node.right = null
                    curPos = maxInLeftChild
                }
            }
            if (parentDeque.isNotEmpty() && curPos!! == parentDeque.first) parentDeque.removeFirst()
        }

        private fun hasNoChildren(node: Node<T>) = node.left == null && node.right == null

        private fun maxInBranch(node: Node<T>): Node<T> {
            var current = node
            while (current.right != null) {
                current = current.right!!
            }
            return current
        }

        private fun delNode(node: Node<T>, to: Node<T>?, parent: Node<T>?) {
            if (parent == null) {
                root = to
                curPos = to
                return
            } else {
                to?.parent = parent
                if (parent.value > node.value) {
                    parent.left = to
                    curPos = parent
                } else {
                    parent.right = to
                    curPos = parent
                }
            }
        }
    }


    /**
     * Подмножество всех элементов в диапазоне [fromElement, toElement)
     *
     * Функция возвращает множество, содержащее в себе все элементы дерева, которые
     * больше или равны fromElement и строго меньше toElement.
     * При равенстве fromElement и toElement возвращается пустое множество.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     *
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     *
     * Спецификация: [java.util.SortedSet.subSet] (Ctrl+Click по subSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     *
     * Очень сложная (в том случае, если спецификация реализуется в полном объёме)
     */
    override fun subSet(fromElement: T, toElement: T): SortedSet<T> {
        TODO()
    }

    /**
     * Подмножество всех элементов строго меньше заданного
     *
     * Функция возвращает множество, содержащее в себе все элементы дерева строго меньше toElement.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     *
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     *
     * Спецификация: [java.util.SortedSet.headSet] (Ctrl+Click по headSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     *
     * Сложная
     */
    override fun headSet(toElement: T): SortedSet<T> {
        TODO()
    }

    /**
     * Подмножество всех элементов нестрого больше заданного
     *
     * Функция возвращает множество, содержащее в себе все элементы дерева нестрого больше toElement.
     * Изменения в дереве должны отображаться в полученном подмножестве, и наоборот.
     *
     * При попытке добавить в подмножество элемент за пределами указанного диапазона
     * должен быть брошен IllegalArgumentException.
     *
     * Спецификация: [java.util.SortedSet.tailSet] (Ctrl+Click по tailSet)
     * (настоятельно рекомендуется прочитать и понять спецификацию перед выполнением задачи)
     *
     * Сложная
     */
    override fun tailSet(fromElement: T): SortedSet<T> {
        TODO()
    }

    override fun first(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.left != null) {
            current = current.left!!
        }
        return current.value
    }

    override fun last(): T {
        var current: Node<T> = root ?: throw NoSuchElementException()
        while (current.right != null) {
            current = current.right!!
        }
        return current.value
    }

    override fun height(): Int =
        height(root)

    private fun height(node: Node<T>?): Int {
        if (node == null) return 0
        return 1 + max(height(node.left), height(node.right))
    }

    override fun checkInvariant(): Boolean =
        root?.let { checkInvariant(it) } ?: true

    private fun checkInvariant(node: Node<T>): Boolean {
        val left = node.left
        if (left != null && (left.value >= node.value || !checkInvariant(left))) return false
        val right = node.right
        return right == null || right.value > node.value && checkInvariant(right)
    }

}