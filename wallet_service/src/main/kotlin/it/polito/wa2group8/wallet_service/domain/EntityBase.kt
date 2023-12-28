package it.polito.wa2group8.wallet_service.domain

import org.springframework.data.util.ProxyUtils
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import java.io.Serializable

//This class provides abstract JPA persistable
@MappedSuperclass
abstract class EntityBase<T: Serializable>
{
    companion object
    {
        private val serialVersionUID = -5554308939380869754L
    }

    @Id
    @GeneratedValue
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
