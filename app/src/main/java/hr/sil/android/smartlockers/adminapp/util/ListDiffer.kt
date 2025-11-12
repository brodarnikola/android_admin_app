package hr.sil.android.smartlockers.adminapp.util

object ListDiffer {
    interface DiffElement<out E>
    class DiffChanged<out E>(val position: Int, val oldElement: E, val newElement: E) : DiffElement<E>
    class DiffInserted<out E>(val position: Int, val elements: List<E>) : DiffElement<E>
    class DiffRemoved<out E>(val position: Int, val count: Int) : DiffElement<E>

    fun <E : Any> getDiff(oldList: List<E>, newList: List<E>, compare: (E, E) -> Boolean): List<DiffElement<E>> {
        val result = mutableListOf<DiffElement<E>>()
        val minSize = Math.min(oldList.size, newList.size)

        //find changed elements
        (0..(minSize - 1)).mapNotNullTo(result) {
            val old = oldList[it]
            val new = newList[it]
            if (!compare(old, new)) {
                DiffChanged(it, old, new)
            } else {
                null
            }
        }

        if (oldList.size < newList.size) {
            //handle inserted
            val insertedItems = newList.subList(oldList.size, newList.size)
            result.add(DiffInserted(oldList.size, insertedItems))
        } else if (oldList.size > newList.size) {
            //handle removed
            val removedItems = oldList.subList(newList.size, oldList.size)
            result.add(DiffRemoved(newList.size, removedItems.size))
        }
        return result
    }
}