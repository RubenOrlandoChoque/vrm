package com.horus.vrmmobile.Models.Dtos

open class PhsDto{
    var Id = ""
    var FullId = ""
    var ParentId: String ? = null
    var Person: PersonPhsDto = PersonPhsDto()
    var Position: PositionDto = PositionDto()
}