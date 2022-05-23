package com.horus.vrmmobile.Utils
import com.horus.vrmmobile.Config.SharedConfig
import com.horus.vrmmobile.Models.RealmModelAudit
import java.util.*

/**
 * Created by USUARIO 004 on 5/9/2018.
 */
object ModelSyncBuilder {

    fun <T>create(item: T): T where T: RealmModelAudit {
        item.Id = if(item.Id.isEmpty()) UUID.randomUUID().toString() else item.Id
        item.IsDeleted = false
        item.RegisterBy =  SharedConfig.getUserId()
        item.RegisterDate = DateUtils.convertDateToString(Date())
        item.UpdatedBy = SharedConfig.getUserId()
        item.UpdatedDate = DateUtils.convertDateToString(Date())
        return item
    }

    fun <T>update(item: T): T where T: RealmModelAudit{
        item.UpdatedBy = SharedConfig.getUserId()
        item.UpdatedDate = DateUtils.convertDateToString(Date())
        return item
    }

    fun <T>delete(item: T): T where T: RealmModelAudit{
        item.IsDeleted = true
        item.DeletedBy = SharedConfig.getUserId()
        item.DeletedDate = DateUtils.convertDateToString(Date())
        return item
    }

    fun <T>undelete(item: T): T where T: RealmModelAudit{
        item.IsDeleted = false
        item.DeletedBy = ""
        item.DeletedDate = ""
        return item
    }
}