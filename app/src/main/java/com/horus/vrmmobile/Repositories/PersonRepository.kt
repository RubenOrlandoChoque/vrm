package com.horus.vrmmobile.Repositories

import android.util.Log
import com.horus.vrmmobile.Models.Person
import io.realm.Case
import io.realm.Realm

/**
 * Created by USUARIO 004 on 27/8/2018.
 */
open class PersonRepository private constructor(): RepositoryInterface<Person>(Person::class.java) {

    private object Holder { val INSTANCE = PersonRepository() }
    companion object {
        val instance: PersonRepository by lazy { Holder.INSTANCE }
    }

    fun getBySearchString(searchString: String): ArrayList<Person> {
        val items: ArrayList<Person> = ArrayList()
        var realm: Realm? = null
        try {
            realm = Realm.getDefaultInstance()
            realm.refresh()
            val results = realm.where(Person::class.java)
                    .contains("FirstSurname", searchString, Case.INSENSITIVE)
                    .or()
                    .contains("SecondSurname", searchString, Case.INSENSITIVE)
                    .or()
                    .contains("FirstName", searchString, Case.INSENSITIVE)
                    .or()
                    .contains("SecondName", searchString, Case.INSENSITIVE)
                    .or()
                    .contains("DocumentationNumber", searchString, Case.INSENSITIVE)
                    .findAll()
            if(results != null){
                items.addAll(realm.copyFromRealm(results))
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