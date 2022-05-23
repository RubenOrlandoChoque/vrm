package com.horus.vrmmobile.Models.migrations

import android.util.Log
import io.realm.DynamicRealm
import io.realm.RealmMigration

/**
 * Example of migrating a Realm file from version 0 (initial version) to its last version (version 3).
 */
class Migration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion
        // During a migration, a DynamicRealm is exposed. A DynamicRealm is an untyped variant of a normal Realm, but
        // with the same object creation and query capabilities.
        // A DynamicRealm uses Strings instead of Class references because the Classes might not even exist or have been
        // renamed.

        // Access the Realm schema in order to create, modify or delete classes and their fields.
        val schema = realm.schema
        // Migrate from version 0 to version 1
        Log.e("oldversion", oldVersion.toString())
        if (oldVersion == 0L) {
            val messageSchema = schema.get("Message")
            messageSchema?.addField("FromMobile", Boolean::class.java)
            oldVersion++
        }
    }
}