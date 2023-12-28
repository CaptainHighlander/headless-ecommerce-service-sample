package it.polito.wa2group8.catalog_service.domain

import org.springframework.data.annotation.Id
import org.springframework.data.util.ProxyUtils
import java.io.Serializable

abstract class EntityBase<T: Serializable>
{
    companion object
    {
        private val serialVersionUID = -5554308939380869754L
    }

    @Id
    private var id: T? = null

    //Subclasses can all read id but cannot modify it since we are not providing set method and id field is private.
    fun getId(): T? = id

    override fun equals(other: Any?): Boolean
    {
        other ?: return false
        if (this === other) return true
        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as EntityBase<*>
        return if (null == this.id) false else this.id == other.getId()
    }

    override fun hashCode(): Int = 8888
    override fun toString() = "@Entity ${this.javaClass.name} with id: $id"
}
