package co.winda.common.security

import spray.json.JsObject
import co.winda.models.User
import com.typesafe.config.Config
import java.security.{PrivateKey, SecureRandom}

import cats.effect.IO
import co.winda.common.security.enums.Algorithm
import tsec.passwordhashers._
import tsec.passwordhashers.jca._
import tsec.passwordhashers.PasswordHash

/**
* @author David Karigithu
* @since 26-09-2016
*/
trait Security {

  implicit val config: Config

  /**
  * Generate Salt
  * @return
  */
  def generateSecureRandom(): SecureRandom = {
    new SecureRandom(config.getString("sec.key").toCharArray
      .map(_.toByte))
  }

  /**
  * Salts the given text using the salt
  * @param random
  * @param text
  * @param algorithm
  * @return
  */
  def hash(random: SecureRandom, text: String, algorithm: Algorithm.Value): IO[PasswordHash[BCrypt]] = {
    BCrypt.hashpw[IO](text)
  }

  def check(hash: PasswordHash[BCrypt], text: String): Boolean = {
    BCrypt.checkpwBool[IO](text, hash).unsafeRunSync()
  }

}
