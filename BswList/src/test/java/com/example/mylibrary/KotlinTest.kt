package com.example.mylibrary

import org.junit.Test

public class KotlinTest {
    @Test
    public fun test() {
        val list: ArrayList<String> = ArrayList();
        list.add("ssss");
    }

    @Test
    fun string() {
        val words = "The quick brown fox jumps over the lazy dog".split(" ")
        val lengthsList = words.filter { println("filter: $it"); it.length > 3 }
                .map { println("length: ${it.length}"); it.length }
                .take(4)

        lengthsList.forEach {
            if (it == 4) return@forEach
            print(it)
        }

        println("Lengths of first 4 words longer than 3 chars:")
        println(lengthsList)
    }

    @Test
    fun bswListFilter() {
        val list: BswFilterList<Person> = BswFilterList(listOf(
                Person("john", 5, true)
                , Person("tony", 95, true)
                , Person("jerry", 20, false)
                , Person("lina", 26, false)
        ))

        println(list.query().sort("age", BswFilterList.DESC).getAll().toString())
        println(list.query().putParams("age", 5).putParams("sex", false).setQueryType(BswFilterList.OR).getAll().toString())
        println(list.query().putParams("age", 5).putParams("sex", false).setQueryType(BswFilterList.OR).getFirst().toString())
    }
}

class Person(private val name: String, private val age: Int, private val sex: Boolean) {
    override fun toString(): String {
        return "Person(name='$name', age=$age, sex=$sex)"
    }
}