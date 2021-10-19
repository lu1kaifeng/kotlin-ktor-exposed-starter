package web

import common.ServerTest
import io.restassured.RestAssured.given
import io.restassured.RestAssured.post
import io.restassured.http.ContentType
import model.Widget
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class AuthResourceTest: ServerTest() {

    @Test
    fun auth() {
        val response = given()
            .queryParam("username","test")
            .queryParam("password","test")
            .post("/login")
            .then()
            .statusCode(200)
            .extract()
            .to<Map<String,String>>()

        print(response)
    }
}