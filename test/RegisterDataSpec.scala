import controllers.RegisterData
import models.Dao
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play._

import org.mockito.Matchers._
import org.mockito.Mockito._

class RegisterDataSpec extends PlaySpec with MockitoSugar with OneAppPerSuite {
  val dao = mock[Dao]

  when(dao.getUser(anyString)) thenAnswer new Answer[Option[models.User]] {
    override def answer(invocation: InvocationOnMock) = Some(models.User(0L, 0L, invocation.getArguments()(0).asInstanceOf[String], ""))
  }
  when(dao.getUser("c")) thenReturn None

  val data0 = RegisterData("a", "b", "c")
  val data1 = RegisterData("c", "c", "c")
  val data2 = RegisterData("b", "c", "d")

  "RegisterData" should {
    "return a map" in {
      val map = data0.toMap
      map must equal(Map("name" -> "a", "pass" -> "b", "pass2" -> "c"))
    }

    "detect existing users" in {
      data0.validate.size must be(1)
    }

    "validate correct data" in {
      data1.validate.size must be(0)
    }

    "detect mistakes in password" in {
      data2.validate.size must be(1)
    }
  }
}
