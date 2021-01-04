package me.glassbricks.sequencegen

open class SequenceGroupHolder {
    data class GroupKey(val name: String, val n: Int)

    private val groups = hashMapOf<GroupKey, PistonSequence>()
    private val nameCache = hashMapOf<Class<*>, String>()

    private fun getGroup(n: Int = -1, create: PistonSequenceBuilder.() -> Unit): PistonSequence {
        //jvm only "hack"
        val klass = create.javaClass
        val methodName = nameCache.getOrPut(klass) { klass.enclosingMethod.name }
        val key = GroupKey(methodName, n)
        return groups.getOrPut(key) {
            val name = if (n >= 0) "$methodName($n)" else methodName
            PistonSequenceBuilder(name).apply(create).build()
        }
    }

    fun PistonSequenceBuilder.group(n: Int, create: PistonSequenceBuilder.() -> Unit) {
        getGroup(n, create).invoke()
    }
}
