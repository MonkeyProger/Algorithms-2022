@file:Suppress("UNUSED_PARAMETER", "unused")

package lesson6

import java.lang.IllegalArgumentException

/**
 * Эйлеров цикл.
 * Средняя
 *
 * Дан граф (получатель). Найти по нему любой Эйлеров цикл.
 * Если в графе нет Эйлеровых циклов, вернуть пустой список.
 * Соседние дуги в списке-результате должны быть инцидентны друг другу,
 * а первая дуга в списке инцидентна последней.
 * Длина списка, если он не пуст, должна быть равна количеству дуг в графе.
 * Веса дуг никак не учитываются.
 *
 * Пример:
 *
 *      G -- H
 *      |    |
 * A -- B -- C -- D
 * |    |    |    |
 * E    F -- I    |
 * |              |
 * J ------------ K
 *
 * Вариант ответа: A, E, J, K, D, C, H, G, B, C, I, F, B, A
 *
 * Справка: Эйлеров цикл -- это цикл, проходящий через все рёбра
 * связного графа ровно по одному разу
 */
fun Graph.findEulerLoop(): List<Graph.Edge> {
    TODO()
}

/**
 * Минимальное остовное дерево.
 * Средняя
 *
 * Дан связный граф (получатель). Найти по нему минимальное остовное дерево.
 * Если есть несколько минимальных остовных деревьев с одинаковым числом дуг,
 * вернуть любое из них. Веса дуг не учитывать.
 *
 * Пример:
 *
 *      G -- H
 *      |    |
 * A -- B -- C -- D
 * |    |    |    |
 * E    F -- I    |
 * |              |
 * J ------------ K
 *
 * Ответ:
 *
 *      G    H
 *      |    |
 * A -- B -- C -- D
 * |    |    |
 * E    F    I
 * |
 * J ------------ K
 */
fun Graph.minimumSpanningTree(): Graph {
    TODO()
}

/**
 * Максимальное независимое множество вершин в графе без циклов.
 * Сложная
 *
 * Дан граф без циклов (получатель), например
 *
 *      G -- H -- J
 *      |
 * A -- B -- D
 * |         |
 * C -- F    I
 * |
 * E
 *
 * Найти в нём самое большое независимое множество вершин и вернуть его.
 * Никакая пара вершин в независимом множестве не должна быть связана ребром.
 *
 * Если самых больших множеств несколько, приоритет имеет то из них,
 * в котором вершины расположены раньше во множестве this.vertices (начиная с первых).
 *
 * В данном случае ответ (A, E, F, D, G, J)
 *
 * Если на входе граф с циклами, бросить IllegalArgumentException
 */

// * T(n) = O(V)
// * Первый цикл по вершинам проверяет наличие цикла, а также разбивает граф,
//   если он имеет отдельные несвязанные ветки, результат деления находится в parts. Операция занимает O(n)
//   Получив ветку, её узлы отбрасываются и дальше проверяются узлы из следующей части(если имеются).
// * Второй цикл по полученным частям находит для каждой максимальное нез. множество:
//   достаточно проверить в ветке множества относительно двух соседних элементов, поэтому при проходе
//   составляются два множества для первого и второго соответсвенно, после чего берется большее из них.
//   Такой поиск основан на поиске в глубину, трудоемкость составляет O(n).
// * R(n) = O(V)
fun Graph.largestIndependentVertexSet(): Set<Graph.Vertex> {
    val parts = mutableListOf<HashSet<Graph.Vertex>>()
    val res = mutableSetOf<Graph.Vertex>()

    if (this.vertices.isEmpty()) return setOf()
    val nVisited = this.vertices as HashSet<Graph.Vertex>
    val parDeque = ArrayDeque<Graph.Vertex>()
    val visited = HashSet<Graph.Vertex>()
    for (i in this.vertices) {
        if (!nVisited.contains(i)) continue
        nVisited.remove(i)
        visited.add(i)
        parDeque.addFirst(i)
        val cycleRes = dfCycleSearch(this, this.getConnections(i), nVisited, visited, parDeque)
        if (cycleRes.second) throw IllegalArgumentException() else {
            parts.add(visited.clone() as HashSet<Graph.Vertex>)
            visited.clear()
            parDeque.clear()
        }
    }

    for (i in parts) {
        val set = mutableSetOf<Graph.Vertex>()
        val invSet = mutableSetOf<Graph.Vertex>()
        val el = i.first()
        val connections = this.getConnections(el)
        val flag = true

        set.add(el)
        val parDeque = ArrayDeque<Graph.Vertex>()
        parDeque.add(el)
        buildSets(this, connections, set, invSet, parDeque, flag)
        res += if (set.size >= invSet.size) set else invSet
    }
    return res
}

fun dfCycleSearch(
    graph: Graph,
    connections: Map<Graph.Vertex, Graph.Edge>,
    nVisited: HashSet<Graph.Vertex>,
    visited: HashSet<Graph.Vertex>,
    parDeque: ArrayDeque<Graph.Vertex>
): Pair<HashSet<Graph.Vertex>, Boolean> {
    var flag = false
    val newDeque = ArrayDeque<Graph.Vertex>()
    newDeque.addAll(parDeque)
    for ((vertex, _) in connections) {
        if (newDeque.first() == vertex) {
            newDeque.removeFirst()
            continue
        }
        if (!nVisited.contains(vertex)) return visited to true
        nVisited.remove(vertex)
        visited.add(vertex)
        newDeque.addLast(vertex)
        flag = dfCycleSearch(graph, graph.getConnections(vertex), nVisited, visited, newDeque).second
        newDeque.removeLast()
        if (flag) break
    }
    return visited to flag
}

fun buildSets(
    graph: Graph,
    connections: Map<Graph.Vertex, Graph.Edge>,
    set: MutableSet<Graph.Vertex>,
    invSet: MutableSet<Graph.Vertex>,
    parDeque: ArrayDeque<Graph.Vertex>,
    flag: Boolean
) {
    val newDeque = ArrayDeque<Graph.Vertex>()
    newDeque.addAll(parDeque)
    for ((vertex, _) in connections) {
        if (newDeque.first() == vertex) {
            newDeque.removeFirst()
            continue
        }
        if (flag) invSet.add(vertex) else
            set.add(vertex)
        newDeque.addLast(vertex)
        buildSets(graph, graph.getConnections(vertex), set, invSet, newDeque, flag.not())
        newDeque.removeLast()
    }
}

/**
 * Наидлиннейший простой путь.
 * Сложная
 *
 * Дан граф (получатель). Найти в нём простой путь, включающий максимальное количество рёбер.
 * Простым считается путь, вершины в котором не повторяются.
 * Если таких путей несколько, вернуть любой из них.
 *
 * Пример:
 *
 *      G -- H
 *      |    |
 * A -- B -- C -- D
 * |    |    |    |
 * E    F -- I    |
 * |              |
 * J ------------ K
 *
 * Ответ: A, E, J, K, D, C, H, G, B, F, I
 */

// * T(n) = O(V*(V+E))
// * Каждый узел проверяется на длиннейший путь,
//   проверка осуществляется поиском в глубину(время выполнения которого O(V+E))
// * R(n) = O(V)
fun Graph.longestSimplePath(): Path {
    val listOfVertix = this.vertices
    var maxSimplePath = listOf<Graph.Vertex>()
    for (i in listOfVertix) {
        var tempPath = mutableListOf<Graph.Vertex>()
        tempPath.add(i)
        val connections = this.getConnections(i)
        tempPath = recursiveSearch(this, connections, tempPath)
        if (maxSimplePath.size < tempPath.size)
            maxSimplePath = tempPath
    }
    return if (maxSimplePath.size <= 1) Path() else
        Path(maxSimplePath, maxSimplePath.size - 1)
}

fun recursiveSearch(
    graph: Graph,
    connections: Map<Graph.Vertex, Graph.Edge>,
    tempPath: MutableList<Graph.Vertex>
): MutableList<Graph.Vertex> {
    var maxSimplePath = mutableListOf<Graph.Vertex>()
    var res = mutableListOf<Graph.Vertex>()
    for ((vertex, _) in connections) {
        res.clear()
        res.addAll(tempPath)
        if (res.contains(vertex)) continue
        res.add(vertex)
        val check = res.size
        res = recursiveSearch(graph, graph.getConnections(vertex), res)
        if (maxSimplePath.size < res.size) {
            maxSimplePath.clear()
            maxSimplePath.addAll(res)
        }
        if (check == res.size) {
            res.remove(vertex)
            continue
        }
    }
    if (maxSimplePath.size < res.size) {
        maxSimplePath.clear()
        maxSimplePath.addAll(res)
    }
    return maxSimplePath
}

/**
 * Балда
 * Сложная
 *
 * Задача хоть и не использует граф напрямую, но решение базируется на тех же алгоритмах -
 * поэтому задача присутствует в этом разделе
 *
 * В файле с именем inputName задана матрица из букв в следующем формате
 * (отдельные буквы в ряду разделены пробелами):
 *
 * И Т Ы Н
 * К Р А Н
 * А К В А
 *
 * В аргументе words содержится множество слов для поиска, например,
 * ТРАВА, КРАН, АКВА, НАРТЫ, РАК.
 *
 * Попытаться найти каждое из слов в матрице букв, используя правила игры БАЛДА,
 * и вернуть множество найденных слов. В данном случае:
 * ТРАВА, КРАН, АКВА, НАРТЫ
 *
 * И т Ы Н     И т ы Н
 * К р а Н     К р а н
 * А К в а     А К В А
 *
 * Все слова и буквы -- русские или английские, прописные.
 * В файле буквы разделены пробелами, строки -- переносами строк.
 * Остальные символы ни в файле, ни в словах не допускаются.
 */
fun baldaSearcher(inputName: String, words: Set<String>): Set<String> {
    TODO()
}
