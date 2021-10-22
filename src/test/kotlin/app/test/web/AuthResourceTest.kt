package app.test.web

import app.test.common.ServerTest
import io.restassured.RestAssured.given
import model.SubjectDto
import org.junit.jupiter.api.Test

internal class AuthResourceTest: ServerTest() {

    @Test
    fun testLogin() {
        given()
            .queryParam("username","app/test")
            .queryParam("password","app/test")
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