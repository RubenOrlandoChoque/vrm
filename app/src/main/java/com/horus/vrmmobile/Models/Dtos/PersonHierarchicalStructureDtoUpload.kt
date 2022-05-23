package com.horus.vrmmobile.Models.Dtos

import com.horus.vrmmobile.Models.PersonHierarchicalStructure

/**
 * Created by mparraga on 28/8/2018.
 */
open class PersonHierarchicalStructureDtoUpload(r: PersonHierarchicalStructure) {

    var Id: String = ""
    var PersonId: String = ""
    var PositionId: String? = ""
    var ZonePoliticalFrontId: String? = ""
    var ParentId: String? = null
    var FullId: String = ""

    init {
        Id = r.Id
        PersonId = r.PersonId
        PositionId = r.PositionId
        ZonePoliticalFrontId = r.ZonePoliticalFrontId
        ParentId = r.ParentId
        FullId = r.FullId
    }
}