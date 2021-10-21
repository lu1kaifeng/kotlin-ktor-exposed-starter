package service

import common.ServerTest
import kotlinx.coroutines.runBlocking
import model.Subject
import org.junit.jupiter.api.Test

class SubjectServiceTest : ServerTest() {
    private val subjectService: SubjectService by serverInject()
    @Test
    fun testRegister(): Unit = runBlocking {
        val subject = Subject(null,"penis","penis").run {
            subjectService.add(this)
        }
        assert(subjectService.getSubjectByNameAndPasswordOrNull(subject.username,subject.password)!=null)
    }
}