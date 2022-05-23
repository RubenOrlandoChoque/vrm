package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Dtos.PersonHierarchicalStructureDtoUpload
import com.horus.vrmmobile.Models.PersonHierarchicalStructure
import com.horus.vrmmobile.Utils.Utils
import io.realm.Realm
import org.json.JSONObject

/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class PersonHierarchicalStructureRepository private constructor() : RepositoryInterface<PersonHierarchicalStructure>(PersonHierarchicalStructure::class.java) {

    init {
        //apiMethodDownload = "api/phs/GetByUserId/&user&"
    }

    private object Holder {
        val INSTANCE = PersonHierarchicalStructureRepository()
    }

    companion object {
        val instance: PersonHierarchicalStructureRepository by lazy { Holder.INSTANCE }
    }

    fun getByPersonId(personId: String): PersonHierarchicalStructure? {
        var item: PersonHierarchicalStructure? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(PersonHierarchicalStructure::class.java).equalTo("IsDeleted", false).equalTo("PersonId", personId).findFirst()
            if (result != null) {
                item = realm.copyFromRealm(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        } finally {
            if (realm != null && !realm.isClosed) {
                realm.close()
            }
        }
        return item
    }

    override fun getAll(): ArrayList<PersonHierarchicalStructure> {
        val items: ArrayList<PersonHierarchicalStructure> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(PersonHierarchicalStructure::class.java).equalTo("IsDeleted", false).findAll()
            if (results != null) {
                val phs = realm.copyFromRealm(results)
                phs.forEach { p -> p.Person = PersonRepository.instance.getById(p.PersonId) }
                items.addAll(phs)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        } finally {
            if (realm != null && !realm.isClosed) {
                realm.close()
            }
        }
        return items
    }

    override fun getByIdAsJSONObject(id: String): JSONObject? {
        var item: JSONObject? = null
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            val result = realm.where(getInterfaceClass()).equalTo("Id", id).findFirst()

            if (result != null) {
                val gson = Utils.gson()
                val dto = PersonHierarchicalStructureDtoUpload(realm.copyFromRealm(result))
                val resultString = gson.toJson(dto)
                item = JSONObject(resultString)
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        } finally {
            if (realm != null && !realm.isClosed) {
                realm.close()
            }
        }
        return item
    }

    fun getListByPersonId(personId: String): ArrayList<PersonHierarchicalStructure> {
        var items: ArrayList<PersonHierarchicalStructure> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val result = realm.where(PersonHierarchicalStructure::class.java).equalTo("IsDeleted", false).equalTo("PersonId", personId).findAll()
            if (result != null) {
                items.addAll(realm.copyFromRealm(result))
                for (p in items) {
                    if (!p.ZonePoliticalFrontId.isNullOrEmpty()) {
                        p.ZonePoliticalFront = ZonePoliticalFrontRepository.instance.getById(p.ZonePoliticalFrontId!!)
                        if (p.ZonePoliticalFront != null) {
                            p.ZonePoliticalFront!!.Zone = ZoneRepository.instance.getById(p.ZonePoliticalFront!!.ZoneId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        } finally {
            if (realm != null && !realm.isClosed) {
                realm.close()
            }
        }
        return items
    }
}