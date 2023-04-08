import scala.io.Source
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import scala.util.Try
import AffineCipher.readKey
import AffineCipher.encrypt
import AffineCipher.decrypt
import AffineCipher.crackWithPlainCeasar
import AffineCipher.crackWithPlainAffine
import AffineCipher.bruteCracking
import scala.util.Failure
import scala.util.Success

@main def cezar(cipher: String, mode: String): Unit =
  cipher match {
    case "-a" =>
      mode match {
        case "-e" =>
          val plainText     = readFile("plain.txt")
          val keys          = readKey(mode = CipherMode.Affine)
          val encryptedText = encrypt(plainText, keys(0), keys(1))
          writeFile("crypto.txt", encryptedText)
        case "-d" =>
          val encryptedText = readFile("crypto.txt")
          val keys          = readKey(mode = CipherMode.Affine)
          val decrypted     = decrypt(encryptedText, keys(0), keys(1))
          writeFile("decrypt.txt", decrypted)
        case "-j" =>
          val encryptedText = readFile("crypto.txt")
          val plainFragment = readFile("extra.txt")
          val keys          = crackWithPlainAffine(plainFragment, encryptedText)
          keys match
            case Right(a, b) =>
              writeFile("key-found.txt", s"${b} ${a}")
              val decrypted = decrypt(encryptedText, a, b)
              writeFile("decrypt.txt", decrypted)
            case Left(value) =>
              println(value)
              sys.exit(1)
        case "-k" =>
          val encryptedText = readFile("crypto.txt")
          val candidates    = bruteCracking(CipherMode.Affine, encryptedText)
          val bf            = new BufferedWriter(new FileWriter(new File("decrypt.txt")))
          candidates.foreach(candidate => bf.write(s"$candidate\n"))
          bf.close()
        case opt: String => println(s"Option \"$opt\" is not supported")
      }

    case "-c" =>
      mode match {
        case "-e" =>
          val plainText     = readFile("plain.txt")
          val keys          = readKey()
          val encryptedText = encrypt(plainText, b = keys(1))
          writeFile("crypto.txt", encryptedText)
        case "-d" =>
          val encryptedText = readFile("crypto.txt")
          val keys          = readKey()
          val decrypted     = decrypt(encryptedText, b = keys(1))
          writeFile("decrypt.txt", decrypted)
        case "-j" =>
          val encryptedText = readFile("crypto.txt")
          val plainFragment = readFile("extra.txt")
          val key           = crackWithPlainCeasar(plainFragment, encryptedText)
          key match
            case Left(value) =>
              println(value)
              sys.exit(1)
            case Right(value) =>
              val decrypted = decrypt(encryptedText, b = value)
              writeFile("decrypt.txt", decrypted)
              writeFile("key-found.txt", value.toString())
        case "-k" =>
          val encryptedText = readFile("crypto.txt")
          val candidates    = bruteCracking(CipherMode.Ceasar, encryptedText)
          val bf            = new BufferedWriter(new FileWriter(new File("decrypt.txt")))
          candidates.foreach(candidate => bf.write(s"$candidate\n"))
          bf.close()
        case opt: String => println(s"Option \"$opt\" is not supported")
      }

    case opt: String => println(s"Option \"$opt\" is not supported")
  }

enum CipherMode:
  case Ceasar, Affine

object AffineCipher {
  private val alphabetLower   = ('a' to 'z').toArray
  private val alphabetUpper   = ('A' to 'Z').toArray
  private val possibleAValues = List(1, 3, 5, 7, 9, 11, 15, 17, 19, 21, 23, 25)
  private val aPrimValues     = List(1, 9, 21, 15, 3, 19, 7, 23, 11, 5, 17, 25)

  def readKey(filename: String = "key.txt", mode: CipherMode = CipherMode.Ceasar): (Int, Int) = {
    val content = readFile(filename).trim().split(" ")
    val b       = content.lift(0).flatMap(c => Try(c.toInt).toOption)
    val a       = content.lift(1).flatMap(c => Try(c.toInt).toOption)
    if b.isEmpty then {
      println("Invalid b in a key")
      sys.exit(1)
    }
    if mode == CipherMode.Affine && (a.isEmpty || !possibleAValues.contains(a.get)) then {
      println("Invalid a in a key")
      sys.exit(1)
    }

    (a.getOrElse(1), b.get)
  }

  def encrypt(plainText: String, a: Int = 1, b: Int = 0): String =
    plainText
      .trim()
      .map(char =>
        char match
          case char if alphabetLower.contains(char) =>
            val i = alphabetLower.indexOf(char)
            alphabetLower((a * i + b) % 26)
          case char if alphabetUpper.contains(char) =>
            val i = alphabetUpper.indexOf(char)
            alphabetUpper((a * i + b) % 26)
          case char => char
      )

  def decrypt(encryptedText: String, a: Int = 1, b: Int = 0): String = {
    val aPrim = aPrimValues(possibleAValues.indexOf(a))

    encryptedText
      .trim()
      .map(char =>
        char match {
          case char if alphabetLower.contains(char) =>
            val i = alphabetLower.indexOf(char)
            alphabetLower(((((i - b) * aPrim) % 26) + 26) % 26)
          case char if alphabetUpper.contains(char) =>
            val i = alphabetUpper.indexOf(char)
            alphabetUpper(((((i - b) * aPrim) % 26) + 26) % 26)
          case char => char
        }
      )
  }

  def crackWithPlainCeasar(plainFragment: String, encryptedText: String): Either[String, Int] = {
    val plainFragmentLower = plainFragment.trim().toLowerCase()
    val encryptedTextLower = encryptedText.trim().toLowerCase()
    val firstLetter        = plainFragmentLower.find(_.isLetter)
    firstLetter match
      case None => Left("Cracking failed.")
      case Some(value) =>
        val i   = plainFragmentLower.indexOf(value)
        val key = (alphabetLower.indexOf(encryptedTextLower(i)) - alphabetLower.indexOf(value) + 26) % 26
        Right(key)
  }

  def crackWithPlainAffine(plainFragment: String, encryptedText: String): Either[String, (Int, Int)] = {
    val plainTextLower     = plainFragment.trim().toLowerCase()
    val encryptedTextLower = encryptedText.trim().toLowerCase()
    val keys = for {
      a <- possibleAValues
      b <- 0 to 25
      et = plainTextLower.map { char =>
        if !alphabetLower.contains(char) then char
        else {
          alphabetLower((a * alphabetLower.indexOf(char) + b) % 26)
        }
      }
      if encryptedTextLower.startsWith(et)
    } yield (a, b)

    if keys.length == 1 then Right(keys.head) else Left("Cracking failed.")
  }

  def bruteCracking(mode: CipherMode, encryptedText: String): List[String] = {
    val aList = mode match
      case CipherMode.Ceasar => List(1)
      case CipherMode.Affine => possibleAValues
    val bList = mode match
      case CipherMode.Ceasar => (1 to 25).toList
      case CipherMode.Affine => (0 to 25).toList

    val candidates = for {
      a <- aList
      b <- bList
    } yield decrypt(encryptedText, a, b)

    candidates
  }
}

def writeFile(filename: String, s: String): Unit =
  val bw = new BufferedWriter(new FileWriter(new File(filename)))
  bw.write(s)
  bw.close()

def readFile(filename: String): String =
  Try(Source.fromFile(filename).mkString) match
    case Failure(exception) =>
      println(s"File $filename not found.")
      sys.exit(1)
    case Success(value) => value
