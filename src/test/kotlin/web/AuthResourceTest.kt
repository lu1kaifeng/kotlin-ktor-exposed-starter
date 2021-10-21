package web

import common.ServerTest
import io.restassured.RestAssured.given
import io.restassured.RestAssured.post
import io.restassured.http.ContentType
import kotlinx.serialization.encodeToString
import model.SubjectDto
import model.Widget
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*import util.JsonMapper.defaultMapper

internal class AuthResourceTest: ServerTest() {

    @Test
    fun testLogin() {
        given()
            .queryParam("username","test")
            .queryParam("password","test")
            .post("/login")
            .then()
            .statusCode(200)
            .extract()
            .to<Map<String,String>>()
    }

    @Test
    fun testRegister(){
        given()
            .body("cum")
            .post("/register")
            .then()
            .statusCode(400)

        given()
            .bodyJson(SubjectDto(null,"penis",null))
            .post("/register")
            .then()
            .statusCode(400)

        val newSubject = given()
            .bodyJson(SubjectDto("penis","penis"))
            .post("/register")
            .then()
            .statusCode(200)
            .extract()
            .to<SubjectDto>()
        assert(newSubject.username == "penis")
    }
}